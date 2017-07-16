package hatespeech.detection.twittercrawler;

import hatespeech.detection.dao.JDBCTwitterDAO;
import hatespeech.detection.model.Hashtag;
import hatespeech.detection.model.Tweet;
import hatespeech.detection.model.TweetImage;
import hatespeech.detection.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.HashtagEntity;
import twitter4j.IDs;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterCrawler {

	private JDBCTwitterDAO twDao=new JDBCTwitterDAO();

	private ConfigurationBuilder cb;
	private Twitter twitter;
	private boolean authenticated = false;
	

	private Map<String, RateLimitUnit> rateLimitMap = new HashMap<String, RateLimitUnit>();

	public void insertUserByUsername(String username) {

		List<String> users = new ArrayList<String>();
		users.add(username);

		insertUsersByUsernames(users);

	}

	public void insertUsersByUsernames(Collection<String> usernames) {

		checkAuthenticated();

		ResponseList<twitter4j.User> users;

		try {

			requestRateLimitAction("users", "/users/lookup");
			users = twitter.lookupUsers(usernames.toArray(new String[0]));
			System.out.println("alle: " + (usernames.size() == users.size())
					+ "  " + usernames.size() + " " + users.size());
			for (twitter4j.User u : users) {

				System.out.println("Insert User ID " + u.getId());
				User newUser = new User(u.getId(), u.getScreenName(),
						u.getName(), new Date(), u.getFriendsCount(),
						u.getFollowersCount(), u.getListedCount(),
						u.getFavouritesCount(), u.getStatusesCount());
				twDao.insertUser(newUser);
				System.out.println("Successful");
			}

		} catch (TwitterException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void insertUsersByUserIDs(long[] userIDs) {

		checkAuthenticated();

		ResponseList<twitter4j.User> users;

		try {

			requestRateLimitAction("users", "/users/lookup");
			users = twitter.lookupUsers(userIDs);

			for (twitter4j.User u : users) {

				//System.out.println("Insert User ID " + u.getId());
				User newUser = new User(u.getId(), u.getScreenName(),
						u.getName(), new Date(), u.getFriendsCount(),
						u.getFollowersCount(), u.getListedCount(),
						u.getFavouritesCount(), u.getStatusesCount());
				twDao.insertUser(newUser);
				//System.out.println("Success");
			}

		} catch (TwitterException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public Set<Tweet> insertTweetsByQuery(String queryString, int numberOfTweets) {

		checkAuthenticated();

		Query query = new Query(queryString);

		QueryResult result;

		Queue<String> usernamesInsertQueue = new LinkedList<String>();
		Set<Tweet> tweetInsertSet = new HashSet<Tweet>();

		try {

			int receivedTweets = 0;

			long lastID = Long.MAX_VALUE;

			while (receivedTweets < numberOfTweets) {
				if (numberOfTweets - receivedTweets > 100) {
					query.setCount(100);
				} else {
					query.setCount(numberOfTweets - receivedTweets);
				}

				requestRateLimitAction("search", "/search/tweets");
				result = twitter.search(query);

				receivedTweets += result.getCount();

				System.out.println("Gathered " + result.getCount()
						+ " new tweets");

				for (Status t : result.getTweets()) {
					if (t.getId() < lastID) {
						lastID = t.getId();
					}

					twitter4j.User newUser = t.getUser();

					User u1 = new User(newUser.getId(),
							newUser.getScreenName(), newUser.getName(),
							newUser.getCreatedAt(), newUser.getFriendsCount(),
							newUser.getFollowersCount(),
							newUser.getListedCount(),
							newUser.getFavouritesCount(),
							newUser.getStatusesCount());

					if (!usernamesInsertQueue.contains(u1.getUsername())) {
						usernamesInsertQueue.add(u1.getUsername());
					}

					Tweet tweet = new Tweet();

					tweet.setTweetid(t.getId());

					tweet.setUser(u1);

					// remove unprintable characters (Emoji)
					String utf8tweet = "";
					try {
						byte[] utf8Bytes = t.getText().getBytes("UTF-8");

						utf8tweet = new String(utf8Bytes, "UTF-8");

					} catch (UnsupportedEncodingException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}

					Pattern unicodeOutliers = Pattern.compile("[^\\x00-\\x7F]",
							Pattern.UNICODE_CASE | Pattern.CANON_EQ
									| Pattern.CASE_INSENSITIVE);
					Matcher unicodeOutlierMatcher = unicodeOutliers
							.matcher(utf8tweet);
					//utf8tweet = unicodeOutlierMatcher.replaceAll(" ");

					tweet.setMessage(utf8tweet);

					tweet.setCreatedat(t.getCreatedAt());

					// tweet.setRetweet(t.isRetweet());
					tweet.setRetweet(false);

					tweet.setRetweetcount(t.getRetweetCount());

					if (t.getHashtagEntities().length > 0) {

						Set<Hashtag> hashtags = new HashSet<Hashtag>();

						for (HashtagEntity hte : t.getHashtagEntities()) {
							Hashtag h = new Hashtag();
							h.setTag(hte.getText());
							h.setTweets(tweet);
							hashtags.add(h);
						}

						tweet.setHashtags(hashtags);
					}

					if (t.getUserMentionEntities().length > 0) {

						Set<String> mentionedUsernames = new HashSet<String>();
						Set<User> mentionedUsers = new HashSet<User>();

						for (UserMentionEntity ume : t.getUserMentionEntities()) {
							mentionedUsernames.add(ume.getScreenName());
							User u = new User();
							u.setUserid(ume.getId());
							mentionedUsers.add(u);

							if (!usernamesInsertQueue.contains(ume
									.getScreenName())) {
								usernamesInsertQueue.add(ume.getScreenName());
							}
						}

						// insertOrUpdateUsers(mentionedUsernames);

						tweet.setMentionUsers(mentionedUsers);
					}

					// MySQLAdapter.getInstance().insertTweet(tweet);
					tweetInsertSet.add(tweet);

				}

				query.setMaxId(lastID - 1);

				while (usernamesInsertQueue.size() >= 100) {

					Set<String> tempUsernames = new HashSet<String>();

					for (int i = 0; i < 100; i++) {
						tempUsernames.add(usernamesInsertQueue.poll());
					}

					insertUsersByUsernames(tempUsernames);

					System.out.println("Added " + tempUsernames.size()
							+ " Users");
				}
			}

			Set<String> tempUsernames = new HashSet<String>();

			while (usernamesInsertQueue.size() > 0) {
				tempUsernames.add(usernamesInsertQueue.poll());
			}

			insertUsersByUsernames(tempUsernames);
			System.out.println("Added remaining " + tempUsernames.size()
					+ " Users");

			System.out.println("Adding " + tweetInsertSet.size() + " Tweets");

			for (Tweet t : tweetInsertSet) {
				twDao.insertTweet(t);
			}

			return tweetInsertSet;

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public void insertRetweetingUsersByTweetIDs(Collection<Long> tweetIDs) {

		checkAuthenticated();

		IDs usersResponse = null;
		Tweet retweetedTweet;

		List<Long> retweetingUserIDs;
		List<Long> tempUserIDs = null;

		List<Long> helper1 = new ArrayList<Long>();
		List<Long> helper2 = new ArrayList<Long>();

		for (Long tweetID : tweetIDs) {

			try {

				long nextCursor = -1;

				retweetedTweet = new Tweet();
				retweetedTweet.setTweetid(tweetID);

				do {

					requestRateLimitAction("statuses",
							"/statuses/retweeters/ids");
					usersResponse = twitter.getRetweeterIds(tweetID, 100,
							nextCursor);

					System.out.println("Retweets of Tweet " + tweetID + ": "
							+ usersResponse.getIDs().length);

					if (usersResponse.getIDs().length == 0) {
						break;
					}

					retweetingUserIDs = new ArrayList<Long>();
					tempUserIDs = new ArrayList<Long>();

					for (long l : usersResponse.getIDs()) {
						retweetingUserIDs.add(l);
					}

					for (Long retweeterUserID : retweetingUserIDs) {

						tempUserIDs.add(retweeterUserID);
						helper1.add(retweeterUserID);

						User tempFollowerUser = new User();
						tempFollowerUser.setUserid(retweeterUserID);
						retweetedTweet.getRetweetUsers().add(tempFollowerUser);

						if (tempUserIDs.size() == 100) {
							insertUsersByUserIDs(TwitterCrawler
									.toPrimitives(tempUserIDs));

							helper2.addAll(tempUserIDs);

							System.out.println("Added " + tempUserIDs.size()
									+ " Users");
							tempUserIDs.clear();
						}

					}

					if (tempUserIDs.size() != 0) {
						insertUsersByUserIDs(TwitterCrawler
								.toPrimitives(tempUserIDs));

						helper2.addAll(tempUserIDs);

						System.out.println("Added remaining "
								+ tempUserIDs.size() + " Users");
						tempUserIDs.clear();
					}

					nextCursor = usersResponse.getNextCursor();

				} while (nextCursor > 0);

				if (retweetedTweet.getRetweetUsers().size() != 0) {

					Thread.sleep(5000);
					System.out.println(retweetedTweet.getRetweetUsers().size());

					System.out.println("contains all: "
							+ helper2.containsAll(helper1));

					twDao.insertTweetRetweetedByUsers(
							retweetedTweet, retweetedTweet.getRetweetUsers());
				}

			} catch (TwitterException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void insertFollowingUsersByUserIDs(List<Long> userIDs) {

		checkAuthenticated();

		IDs usersResponse = null;

		List<Long> followingUserIDs=null;
		List<Long> tempUserIDs = null;

		for (Long userID : userIDs) {
			
			System.out.println("Extract Followers for UserID: "+userID);
			try {

				long nextCursor = -1;

				do {

					requestRateLimitAction("followers", "/followers/ids");
					usersResponse = twitter.getFollowersIDs(userID, nextCursor,
							5000);

					System.out.println("size() of iteration " + userID + ": "
							+ usersResponse.getIDs().length);

					followingUserIDs = new ArrayList<Long>();
					tempUserIDs = new ArrayList<Long>();
					tempUserIDs.add(userID);
					
					if (usersResponse.getIDs().length == 0) {
						break;
					}

					for (long l : usersResponse.getIDs()) {
						followingUserIDs.add(l);
						
						tempUserIDs.add(l);


						if (tempUserIDs.size() == 100) {
							insertUsersByUserIDs(TwitterCrawler
									.toPrimitives(tempUserIDs));

							System.out.println("Added " + tempUserIDs.size()
									+ " Users");
							tempUserIDs.clear();
						}
					}

					if (tempUserIDs.size() != 0) {
						insertUsersByUserIDs(TwitterCrawler
								.toPrimitives(tempUserIDs));

						System.out.println("Added remaining "
								+ tempUserIDs.size() + " Users");
						tempUserIDs.clear();
					}


					nextCursor = usersResponse.getNextCursor();

				} while (nextCursor > 0);

				if (followingUserIDs.size() != 0) {

					Thread.sleep(20000);
					System.out.println(followingUserIDs.size());
					
					twDao.insertUserFollowedByUsers(
							userID,
							followingUserIDs);
					
					 System.out.println("Total: "+followingUserIDs.size());
				}

			} catch (TwitterException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} 
		}
	}

	public void insertFollowedUsersByUserIDs(List<Long> userIDs) {

		checkAuthenticated();

		IDs usersResponse = null;

		List<Long> followedUserIDs=null;
		List<Long> tempUserIDs = null;

		for (Long userID : userIDs) {

			try {

				long nextCursor = -1;

				do {

					requestRateLimitAction("friends", "/friends/ids");
					usersResponse = twitter.getFriendsIDs(userID, nextCursor,
							5000);

					System.out.println("size() of iteration " + userID + ": "
							+ usersResponse.getIDs().length);
					
					followedUserIDs = new ArrayList<Long>();
					tempUserIDs=new ArrayList<Long>();
					tempUserIDs.add(userID);
					
					if (usersResponse.getIDs().length == 0) {
						break;
					}

					for (long l : usersResponse.getIDs()) {
						followedUserIDs.add(l);
						
						tempUserIDs.add(l);


						if (tempUserIDs.size() == 100) {
							insertUsersByUserIDs(TwitterCrawler
									.toPrimitives(tempUserIDs));

							System.out.println("Added " + tempUserIDs.size()
									+ " Users");
							tempUserIDs.clear();
						}
					}
					
					if (tempUserIDs.size() != 0) {
						insertUsersByUserIDs(TwitterCrawler
								.toPrimitives(tempUserIDs));

						System.out.println("Added remaining "
								+ tempUserIDs.size() + " Users");
						tempUserIDs.clear();
					}

					nextCursor = usersResponse.getNextCursor();

				} while (nextCursor > 0);

				if (followedUserIDs.size() != 0) {

					Thread.sleep(20000);
					System.out.println(followedUserIDs.size());

					twDao.insertUserFollowsUsers(
							userID,
							followedUserIDs);
				}

			} catch (TwitterException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	public void insertAllTweetsFromUser(Long userid)
	{
		checkAuthenticated();
		
		int pageno = 1;
	    List<Status> statuses = new ArrayList<Status>();

	    while (true) {

	      try {
	    	requestRateLimitAction("statuses", "/statuses/user_timeline");
	        int size = statuses.size(); 
	        Paging page = new Paging(pageno++, 100);
	        statuses.addAll(twitter.getUserTimeline(userid, page));
	        if (statuses.size() == size)
	          break;
	      }
	      catch(TwitterException e) {

	        e.printStackTrace();
	      }
	    }
	    for(Status s:statuses)
	    {
	    	Tweet tweet=fillTweet(s);
	    	insertTweet(tweet);
	    }
	    System.out.println("Total: "+statuses.size());
	}

	public void insertLikesRetweetsFromTweets(List<Long> tweetsToUpdate)
	{
		List<Long> subItems;
		
		while(tweetsToUpdate.size()>0)
		{
			if(tweetsToUpdate.size()>100)
				subItems = new ArrayList<Long>(tweetsToUpdate.subList(0, 99));
			else
				subItems = new ArrayList<Long>(tweetsToUpdate.subList(0, tweetsToUpdate.size()));

			tweetsToUpdate.removeAll(subItems);
			
			checkAuthenticated();

			List<Status> statuses = new ArrayList<Status>();

				try {
					requestRateLimitAction("statuses", "/statuses/lookup");
					statuses.addAll(twitter.lookup(subItems.stream()
							.mapToLong(l -> l).toArray()));

				} catch (TwitterException e) {

					e.printStackTrace();
				}
			for (Status s : statuses) {
				Tweet tweet = new Tweet();
				tweet.setTweetid(s.getId());
				tweet.setRetweetcount(s.getRetweetCount());
				tweet.setFavouritecount(s.getFavoriteCount());
				twDao.updateRetweetsAndLikes(tweet);
			}
			System.out.println("Total: " + statuses.size());
		}
	}
	
	public void insertTweet(Tweet tweet) {

		try {
			twDao.insertUser(tweet.getUser());
			if (tweet.getMentionUsers() != null)
				for (User u : tweet.getMentionUsers())
					twDao.insertUser(u);
			if (tweet.getRetweetedTweet() != null) {
				insertTweet(tweet.getRetweetedTweet());
			}
			twDao.insertTweet(tweet);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Tweet fillTweet(Status s) {
		Tweet tweet = new Tweet();
		tweet.setTweetid(s.getId());
		twitter4j.User newUser = s.getUser();
		User u1 = new User(newUser.getId(), newUser.getScreenName(),
				newUser.getName(), s.getCreatedAt(),
				newUser.getFriendsCount(), newUser.getFollowersCount(),
				newUser.getListedCount(), newUser.getFavouritesCount(),
				newUser.getStatusesCount());

		tweet.setUser(u1);
		u1.setRetweetcount(s.getRetweetCount());
		// remove unprintable characters (Emoji)
		String utf8tweet = "";
		try {
			byte[] utf8Bytes = s.getText().getBytes("UTF-8");

			utf8tweet = new String(utf8Bytes, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		Pattern unicodeOutliers = Pattern.compile("[^\\x00-\\x7F]",
				Pattern.UNICODE_CASE | Pattern.CANON_EQ
						| Pattern.CASE_INSENSITIVE);
		Matcher unicodeOutlierMatcher = unicodeOutliers
				.matcher(utf8tweet);
		//utf8tweet = unicodeOutlierMatcher.replaceAll(" ");

		tweet.setMessage(utf8tweet);
		System.out.println(s.getText());
		
		MediaEntity[] media = s.getMediaEntities(); 
		Set<TweetImage> tw_images = new HashSet<TweetImage>();
		
		for(MediaEntity m : media){ 
			if(m.getType().equals("photo"))
			{
				TweetImage twi = new TweetImage();
				try {
					String[] splitUrl=m.getMediaURL().split("/");
					twi.setUrl(new File("..").getCanonicalPath()+""+"/TwitterImages/"+splitUrl[splitUrl.length-1]);
					System.out.println(m.getMediaURL()+" "+new File("..").getCanonicalPath()+""+"/TwitterImages/"+splitUrl[splitUrl.length-1]);
					
				
					saveImage(m.getMediaURL(),"../TwitterImages/"+splitUrl[splitUrl.length-1]);
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				twi.setTweet(tweet);
				tw_images.add(twi);

				tweet.setTwImages(tw_images);
			}
		}
		
		tweet.setCreatedat(s.getCreatedAt());
		if (s.getInReplyToStatusId() != -1) {
			tweet.setReplyTo(new Tweet() {
				{
					setTweetid(s.getInReplyToStatusId());
					setUser(new User() {
						{
							setUserid(s.getInReplyToUserId());
						}
					});
				}
			});
		}
		
		tweet.setRetweetcount(s.getRetweetCount());

		if (s.getHashtagEntities().length > 0) {

			Set<Hashtag> hashtags = new HashSet<Hashtag>();

			for (HashtagEntity hte : s.getHashtagEntities()) {
				Hashtag h = new Hashtag();
				h.setTag(hte.getText());
				h.setTweets(tweet);
				hashtags.add(h);
			}

			tweet.setHashtags(hashtags);
		}


		Status mentioned;
		if (s.isRetweet()) {
			mentioned = s.getRetweetedStatus();
			Tweet retweetedTweet = fillTweet(s.getRetweetedStatus());
			System.out.println(s.getRetweetedStatus().getCreatedAt()+" "+s.getRetweetedStatus().getText());
			retweetedTweet.setRetweet(true);
			tweet.setRetweetedTweet(retweetedTweet);
		} else {
			mentioned = s;
		}

		if (mentioned.getUserMentionEntities().length > 0) {
			Set<User> mentionedUsers = new HashSet<User>();

			for (UserMentionEntity ume : mentioned
					.getUserMentionEntities()) {
				if (mentioned.getInReplyToUserId() == -1
						|| mentioned.getInReplyToUserId() != ume
								.getId()) {
					User u = new User();
					u.setUserid(ume.getId());
					u.setUsername(ume.getScreenName());
					u.setName(ume.getName());
					u.setLastupdate(s.getCreatedAt());
					mentionedUsers.add(u);
				}
			}
			tweet.setMentionUsers(mentionedUsers);
		}
		return tweet;
	}
	
	public static void saveImage(String imageUrl, String destinationFile) throws IOException {
		URL url = new URL(imageUrl);
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(destinationFile);

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
	}

	public static long[] toPrimitives(Collection<Long> objects) {

		long[] primitives = new long[objects.size()];

		int i = 0;

		for (Iterator<Long> it = objects.iterator(); it.hasNext(); i++) {
			primitives[i] = it.next();
		}

		return primitives;
	}

	public void setupApplicationOnlyAuthentication() {

		cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey("3s25r5XVoUVwa8PRpr6qUCllC");
		cb.setOAuthConsumerSecret("wjAKhXTU8XuRUbc5Y4Od574Q0j6i9dtBJ3rGKJdVPFFA6kkSqH");

		twitter = new TwitterFactory(cb.build()).getInstance();

		try {

			twitter.getOAuth2Token();

		} catch (TwitterException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		authenticated = true;
	}

	public void setupUserAuthentication() {

		cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("sGHln59z8uDnNbOExZpihqSWA");
		cb.setOAuthConsumerSecret("j5uquWuzrZ66srXhatPCnhZJHgvReZgmcjGkXYug8UtqwQ3agp");
		cb.setOAuthAccessToken("2846620079-0kOUwMHsdR6ubYvtKB6UGrfqukNOVXS150Gfsum");
		cb.setOAuthAccessTokenSecret("s9QMgq9on5lkwGmQjEeYDz5nppKqtAwtYptDbH0r0Cx0e");

		twitter = new TwitterFactory(cb.build()).getInstance();

		authenticated = true;
	}

	public void checkAuthenticated() {
		if (!authenticated) {
			this.setupUserAuthentication();
			authenticated = true;
		}
	}


	public void requestRateLimitAction(String family, String resource) {

		checkAuthenticated();

		try {

			if (!rateLimitMap.containsKey(resource)) {

				if (!family.equals("application")
						&& !resource.equals("/application/rate_limit_status")) {
					requestRateLimitAction("application",
							"/application/rate_limit_status");
				}

				RateLimitUnit rlu = new RateLimitUnit();
				Map<String, RateLimitStatus> tempRateLimitStatusMap = twitter
						.getRateLimitStatus(family);
				RateLimitStatus tempRateLimitStatus = tempRateLimitStatusMap
						.get(resource);

				rlu.setFamily(family);
				rlu.setResource(resource);
				rlu.setRemainingRequests(tempRateLimitStatus.getRemaining());
				rlu.setResetTime(System.currentTimeMillis()
						+ tempRateLimitStatus.getSecondsUntilReset() * 1000);

				rateLimitMap.put(resource, rlu);

				requestRateLimitAction(family, resource);

				System.out.println(resource + " RateLimit initialized");
			} else {
				
				RateLimitUnit rlu = rateLimitMap.get(resource);
				System.out.println("Remaining Requests: "+rlu.getRemainingRequests());
				
				if (rlu.getResetTime() < System.currentTimeMillis()) {
					rateLimitMap.remove(resource);
					requestRateLimitAction(family, resource);
				}

				if (rlu.getRemainingRequests() > 0) {
					rlu.setRemainingRequests(rlu.getRemainingRequests() - 1);
				} else {
					System.out
							.println("RateLimit "
									+ rlu.getResource()
									+ ": sleeping for "
									+ ((int) ((rlu.getResetTime()
											- System.currentTimeMillis() + 10000) / 1000))
									+ " seconds");
					Thread.sleep(rlu.getResetTime()
							- System.currentTimeMillis() + 10000);

					rateLimitMap.remove(resource);
					requestRateLimitAction(family, resource);
				}

			}

		} catch (TwitterException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}

	public void test() {

		for (int i = 0; i < 400; i++) {

			Map<String, RateLimitStatus> tempRateLimitStatusMap;
			try {
				requestRateLimitAction("application",
						"/application/rate_limit_status");
				tempRateLimitStatusMap = twitter.getRateLimitStatus("users");
				RateLimitStatus tempRateLimitStatus = tempRateLimitStatusMap
						.get("/users/lookup");
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
