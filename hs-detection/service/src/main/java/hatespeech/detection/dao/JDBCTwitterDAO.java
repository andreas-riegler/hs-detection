package hatespeech.detection.dao;

import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.Hashtag;
import hatespeech.detection.model.SocialInteraction;
import hatespeech.detection.model.Tweet;
import hatespeech.detection.model.TweetImage;
import hatespeech.detection.model.User;
import hatespeech.detection.model.UserPagingInfo;
import hatespeech.detection.service.DatabaseConnector;
import hatespeech.detection.service.TwitterDatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.restfb.util.CachedDateFormatStrategy;




public class JDBCTwitterDAO {
	
	private CachedDateFormatStrategy cdfs = new CachedDateFormatStrategy();
	private DateFormat df = cdfs.formatFor("dd.MM.yyyy HH:mm:ss");
	private Connection conn = null;
	private static long COMMENTS_COUNT=739029786018893800L;
	
	public String insertUser(User model) throws SQLException {
		
		if (model == null) {
			throw new IllegalArgumentException("model must not be null");
		}
		
		String result = null;
		int rows = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			//if (model.getTweetcount() > 0)
			{
				try (PreparedStatement stmtCreateUser = conn
						.prepareStatement("INSERT OR IGNORE INTO User VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");) {
					stmtCreateUser.setLong(1, model.getUserid());
					stmtCreateUser.setString(2, model.getUsername());
					stmtCreateUser.setString(3, model.getName());
					stmtCreateUser.setString(4, df.format(model
							.getLastupdate().getTime()));
					stmtCreateUser.setInt(5, model.getFriendscount());
					stmtCreateUser.setInt(6, model.getFollowerscount());
					stmtCreateUser.setInt(7, model.getListedcount());
					stmtCreateUser.setInt(8, model.getFavoritecount());
					stmtCreateUser.setInt(9, model.getTweetcount());
					stmtCreateUser.setInt(10, model.getRetweetcount());

					rows = stmtCreateUser.executeUpdate();

				}
				try(PreparedStatement stmtCreateUser = conn
						.prepareStatement("UPDATE User Set username=?, name=?, lastupdate=?, friendscount=?, followerscount=?, listedcount=?, favoritecount=?, tweetcount=?, retweets=MAX(retweets,?) where userid=?;");)
					{
					stmtCreateUser.setString(1, model.getUsername());
					stmtCreateUser.setString(2, model.getName());
					stmtCreateUser.setString(3, df.format(model
							.getLastupdate().getTime()));
					stmtCreateUser.setInt(4, model.getFriendscount());
					stmtCreateUser.setInt(5, model.getFollowerscount());
					stmtCreateUser.setInt(6, model.getListedcount());
					stmtCreateUser.setInt(7, model.getFavoritecount());
					stmtCreateUser.setInt(8, model.getTweetcount());
					stmtCreateUser.setInt(9, model.getRetweetcount());
					stmtCreateUser.setLong(10, model.getUserid());

					rows = stmtCreateUser.executeUpdate();

					if (rows == 0)
						throw new SQLException(
								"Create user failed, no rows affected");
					conn.commit();
					result = rows + " rows affected";
				}
			}
//			else
//				try (PreparedStatement stmtCreateUser = conn
//						.prepareStatement("INSERT OR IGNORE INTO User VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");) {
//					stmtCreateUser.setLong(1, model.getUserid());
//					stmtCreateUser.setString(2, model.getUsername());
//					stmtCreateUser.setString(3, model.getName());
//					stmtCreateUser.setString(4, df.format(model
//							.getLastupdate().getTime()));
//					stmtCreateUser.setInt(5, model.getFriendscount());
//					stmtCreateUser.setInt(6, model.getFollowerscount());
//					stmtCreateUser.setInt(7, model.getListedcount());
//					stmtCreateUser.setInt(8, model.getFavoritecount());
//					stmtCreateUser.setInt(9, model.getTweetcount());
//					stmtCreateUser.setInt(10, model.getRetweetcount());
//
//					rows = stmtCreateUser.executeUpdate();
////
////					if (rows == 0)
////						throw new SQLException(
////								"Create user failed, no rows affected");
//					conn.commit();
//					result = rows + " rows affected";
//				}
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} 
		return result;
	}

	public String insertTweet(Tweet model) throws SQLException {
		
		
		String result = null;
		int rows = 0, totalrows = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			if (model.isRetweet()) {
				try (PreparedStatement stmtUpdateRetweet = conn
						.prepareStatement("UPDATE tweet SET retweetcount=? WHERE tweetid=?;")) {
					stmtUpdateRetweet.setInt(1, model.getRetweetcount());
					stmtUpdateRetweet.setLong(2, model.getTweetid());
					totalrows += rows = stmtUpdateRetweet.executeUpdate();

					if (rows == 0) {
						try (PreparedStatement stmtCreateRetweet = conn
								.prepareStatement("INSERT INTO tweet(tweetid,creator_userid,retweetcount) VALUES(?, ?, ?);")) {
							stmtCreateRetweet.setLong(1, model.getTweetid());
							stmtCreateRetweet.setLong(2, model.getUser()
									.getUserid());
							stmtCreateRetweet
									.setInt(3, model.getRetweetcount());
							totalrows += rows = stmtCreateRetweet
									.executeUpdate();

							if (rows == 0)
								throw new SQLException(
										"Create retweeted tweet failed, no rows affected");
						}
					}
				}
			} else {
				if (model.getReplyTo() != null) {
					try (PreparedStatement stmtCreateReplyUser = conn
							.prepareStatement("INSERT OR IGNORE INTO User VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");) {
						stmtCreateReplyUser.setLong(1, model.getReplyTo()
								.getUser().getUserid());
						stmtCreateReplyUser.setString(2, model.getReplyTo()
								.getUser().getUsername());
						stmtCreateReplyUser.setString(3, model.getReplyTo()
								.getUser().getName());
						if (model.getReplyTo().getUser().getLastupdate() != null)
							stmtCreateReplyUser.setString(4, df.format(
									model.getReplyTo().getUser()
											.getLastupdate().getTime()));
						else
							stmtCreateReplyUser.setNull(4, Types.NULL);
						stmtCreateReplyUser.setInt(5, model.getReplyTo()
								.getUser().getFriendscount());
						stmtCreateReplyUser.setInt(6, model.getReplyTo()
								.getUser().getFollowerscount());
						stmtCreateReplyUser.setInt(7, model.getReplyTo()
								.getUser().getListedcount());
						stmtCreateReplyUser.setInt(8, model.getReplyTo()
								.getUser().getFavoritecount());
						stmtCreateReplyUser.setInt(9, model.getReplyTo()
								.getUser().getTweetcount());
						stmtCreateReplyUser.setInt(10, model.getReplyTo()
								.getUser().getRetweetcount());

						totalrows += rows = stmtCreateReplyUser.executeUpdate();
						
						if (rows == 0)
							throw new SQLException(
									"Create reply user failed, no rows affected "+model.getReplyTo()
									.getUser().getUserid()+ " UN:"+model.getReplyTo()
									.getUser().getUsername()+" N"+model.getReplyTo()
									.getUser().getName()+" CT"+(model.getReplyTo().getUser().getLastupdate() != null?model.getReplyTo().getUser()
									.getLastupdate().getTime():"null")+" FC"+model.getReplyTo()
									.getUser().getFriendscount()+ " FoC"+model.getReplyTo()
									.getUser().getListedcount()+" LC"+model.getReplyTo()
									.getUser().getFavoritecount()+" FaC"+model.getReplyTo()
									.getUser().getTweetcount()+" TC"+model.getReplyTo()
									.getUser().getTweetcount()+" RC"+model.getReplyTo()
									.getUser().getRetweetcount());
					}
						try (PreparedStatement stmtCreateReplyTweet = conn
								.prepareStatement("INSERT OR IGNORE INTO Tweet(tweetid, creator_userid) VALUES (?, ?)");) {

							stmtCreateReplyTweet.setLong(1, model.getReplyTo()
									.getTweetid());
							stmtCreateReplyTweet.setLong(2, model.getReplyTo()
									.getUser().getUserid());

							totalrows += rows = stmtCreateReplyTweet.executeUpdate();

							if (rows == 0)
								throw new SQLException(
										"Create reply tweet failed, no rows affected");
						}
					
				}

				try (PreparedStatement stmtCreateTweet = conn
						.prepareStatement("INSERT OR IGNORE INTO Tweet VALUES (?, ?, ?, ?, ?, ?, ?,?,?)");) {
					stmtCreateTweet.setLong(1, model.getTweetid());
					stmtCreateTweet.setLong(2, model.getUser().getUserid());
					stmtCreateTweet.setString(3, model.getMessage());
					stmtCreateTweet.setString(4, df.format(model
							.getCreatedat().getTime()));
					if (model.getReplyTo() != null) {
						stmtCreateTweet.setLong(5, model.getReplyTo()
								.getTweetid());
					} else {
						stmtCreateTweet.setNull(5, Types.BIGINT);
					}
					stmtCreateTweet.setInt(6, model.getRetweetcount());
					if (model.getRetweetedTweet() != null) {
						stmtCreateTweet.setLong(7, model.getRetweetedTweet()
								.getTweetid());
					} else {
						stmtCreateTweet.setNull(7, Types.BIGINT);
					}
					stmtCreateTweet.setNull(8, Types.VARCHAR);
					stmtCreateTweet.setInt(9, -1);
					
					rows=stmtCreateTweet.executeUpdate();
					
					if (rows == 0)
						throw new SQLException(
								"Create tweet failed, no rows affected");
				}
				try (PreparedStatement stmtCreateTweet = conn
							.prepareStatement("UPDATE Tweet Set content=?, createdate=?, reply_tweetid=?, retweetcount=?, retweet_tweetid=? where tweetid=?");){
					
					stmtCreateTweet.setLong(6, model.getTweetid());
					stmtCreateTweet.setString(1, model.getMessage());
					stmtCreateTweet.setString(2, df.format(model
							.getCreatedat().getTime()));
					if (model.getReplyTo() != null) {
						stmtCreateTweet.setLong(3, model.getReplyTo()
								.getTweetid());
					} else {
						stmtCreateTweet.setNull(3, Types.BIGINT);
					}
					stmtCreateTweet.setInt(4, model.getRetweetcount());
					if (model.getRetweetedTweet() != null) {
						stmtCreateTweet.setLong(5, model.getRetweetedTweet()
								.getTweetid());

					} else {
						stmtCreateTweet.setNull(5, Types.BIGINT);
					}

					totalrows += rows = stmtCreateTweet.executeUpdate();
					
					if (rows == 0)
						throw new SQLException(
								"Create tweet failed, no rows affected");
				}
				if(model.getRetweetedTweet()!=null)
				{
					try (PreparedStatement stmtCreateTweet_RetweetUser = conn
							.prepareStatement("INSERT OR IGNORE INTO Retweet VALUES (?, ?) ");) {
						stmtCreateTweet_RetweetUser.setLong(1, model.getRetweetedTweet().getTweetid());
						stmtCreateTweet_RetweetUser.setLong(2, model.getUser().getUserid());
						
						totalrows += rows = stmtCreateTweet_RetweetUser.executeUpdate();
						
						if (rows == 0)
							throw new SQLException(
									"Create Tweet_RetweetUser in Retweet failed, no rows affected");
					}
				}
				if (model.getMentionUsers() != null) {
					try (PreparedStatement stmtCreateMention = conn
							.prepareStatement("INSERT OR IGNORE INTO TweetMentionsUser VALUES (?, ?) ");) {
						for (User user : model.getMentionUsers()) {
							stmtCreateMention.setLong(1, model.getTweetid());
							stmtCreateMention.setLong(2, user.getUserid());
							totalrows += rows = stmtCreateMention
									.executeUpdate();
							/*
							if (rows == 0)
								throw new SQLException(
										"Create mention failed, no rows affected");*/
						}
					}
				}
				if (model.getHashtags() != null) {
					try (PreparedStatement stmtCreateHashtag = conn
							.prepareStatement("INSERT INTO Hashtag VALUES (NULL,?, ?)");) {
						for (Hashtag hashtag : model.getHashtags()) {
							stmtCreateHashtag.setString(1, hashtag.getTag());
							stmtCreateHashtag.setLong(2, model.getTweetid());
							totalrows += rows = stmtCreateHashtag
									.executeUpdate();

							if (rows == 0)
								throw new SQLException(
										"Create hashtag failed, no rows affected");
						}
					}
				}
				if (model.getTwImages() != null) {
					try (PreparedStatement stmtCreateImage = conn
							.prepareStatement("INSERT INTO Images VALUES (NULL,?, ?)");) {
						for (TweetImage image : model.getTwImages()) {
							stmtCreateImage.setString(1, image.getUrl());
							stmtCreateImage.setLong(2, model.getTweetid());
							totalrows += rows = stmtCreateImage
									.executeUpdate();

							if (rows == 0)
								throw new SQLException(
										"Create hashtag failed, no rows affected");
						}
					}
				}
			}

			conn.commit();
			result = totalrows + " rows affected";
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} 
		return result;
	}

	
	public String insertTweetRetweetedByUsers(Tweet tweet,
			Collection<User> retweeters) throws SQLException {

		String result = null;
		int totalrows = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement stmtCreateFollow = conn
					.prepareStatement(
							// Duplikate performant ignorieren:
							"INSERT OR IGNORE INTO Retweet VALUES (?, ?)",
							Statement.RETURN_GENERATED_KEYS);) {
				for (User retweeter : retweeters) {
					stmtCreateFollow.setLong(1, tweet.getTweetid());
					stmtCreateFollow.setLong(2, retweeter.getUserid());
					totalrows += stmtCreateFollow.executeUpdate();
				}
			}

			conn.commit();
			result = totalrows + " rows affected";
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} finally {
			safeClose(conn);
		}
		return result;
	}

	
	public String insertUserFollowsUsers(User follower, Collection<User> follows)
			throws SQLException {

		String result = null;
		int totalrows = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement stmtCreateFollow = conn
					.prepareStatement(
							// Duplikate performant ignorieren:
							"INSERT OR IGNORE INTO UserFollowsUser VALUES (?, ?)");) {
				for (User followed : follows) {
					stmtCreateFollow.setLong(1, follower.getUserid());
					stmtCreateFollow.setLong(2, followed.getUserid());
					totalrows += stmtCreateFollow.executeUpdate();
				}
			}

			conn.commit();
			result = totalrows + " rows affected";
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} finally {
			safeClose(conn);
		}
		return result;
	}

	
	public String insertUserFollowedByUsers(User followedUser,
			Collection<User> followers) throws SQLException {
	
		String result = null;
		int totalrows = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement stmtCreateFollow = conn
					.prepareStatement(
							// Duplikate performant ignorieren:
							"INSERT OR IGNORE INTO UserFollowsUser VALUES (?, ?);")) {
				for (User follower : followers) {
					stmtCreateFollow.setLong(1, follower.getUserid());
					stmtCreateFollow.setLong(2, followedUser.getUserid());
					totalrows += stmtCreateFollow.executeUpdate();
				}
			}

			conn.commit();
			result = totalrows + " rows affected";
		} catch (SQLException e) {
			try {
				followers.stream().forEach(
						u -> System.out.println(u.getUserid()));
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} finally {
			safeClose(conn);
		}
		return result;
	}


	public String insertUserBlocksUsers(User blocker, Collection<User> blocks)
			throws SQLException {

		String result = null;
		int totalrows = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement stmtCreateBlock = conn
					.prepareStatement(
							// Duplikate performant ignorieren:
							"INSERT OR IGNORE INTO UserBlocksUser VALUES (?, ?)");) {
				for (User blocked : blocks) {
					stmtCreateBlock.setLong(1, blocker.getUserid());
					stmtCreateBlock.setLong(2, blocked.getUserid());
					totalrows += stmtCreateBlock.executeUpdate();
				}
			}

			conn.commit();
			result = totalrows + " rows affected";
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} finally {
			safeClose(conn);
		}
		return result;
	}


	public String insertUserFavoritesTweet(User user,
			Collection<Tweet> favoritedTweets) throws SQLException {

		String result = null;
		int totalrows = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement stmtCreateFavorite = conn
					.prepareStatement(
							// Duplikate performant ignorieren:
							"INSERT INTO UserFavoritesTweet VALUES (?, ?)")) {
				for (Tweet tweet : favoritedTweets) {
					stmtCreateFavorite.setLong(1, user.getUserid());
					stmtCreateFavorite.setLong(2, tweet.getTweetid());
					totalrows += stmtCreateFavorite.executeUpdate();
				}
			}

			conn.commit();
			result = totalrows + " rows affected";
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} finally {
			safeClose(conn);
		}
		return result;
	}


	public List<User> selectUsers(int start, int rows, String orderby,
			String ordertype, String query) throws SQLException {
		List<User> users = new LinkedList<User>();
	
		if (!(ordertype.equalsIgnoreCase("ASC") || ordertype
				.equalsIgnoreCase("DESC"))
				|| !(orderby.equalsIgnoreCase("userid")
						|| orderby.equalsIgnoreCase("username")
						|| orderby.equalsIgnoreCase("name")
						|| orderby.equalsIgnoreCase("lastupdate")
						|| orderby.equalsIgnoreCase("friendscount")
						|| orderby.equalsIgnoreCase("followerscount")
						|| orderby.equalsIgnoreCase("listedcount")
						|| orderby.equalsIgnoreCase("favoritecount")
						|| orderby.equalsIgnoreCase("tweetcount")
						|| orderby.equalsIgnoreCase("retweets") || orderby
							.equalsIgnoreCase("influence")))
			throw new SQLException("potential SQL Injection");

		try {
			conn = TwitterDatabaseConnector.getConnection();

			try (PreparedStatement stmtSelect = conn
					.prepareStatement(query == null ? "SELECT *"
							+ " FROM user WHERE username IS NOT NULL AND tweetcount > 0 ORDER BY "
							+ orderby + " " + ordertype + " LIMIT ?,?;"
							: "SELECT *"
									+ " FROM user WHERE username IS NOT NULL AND tweetcount > 0 AND (username LIKE ? OR name LIKE ?) ORDER BY "
									+ orderby + " " + ordertype + " LIMIT ?,?;")) {
					if (query == null) {
						stmtSelect.setInt(7, start);
						stmtSelect.setInt(8, rows);
					} else {
						stmtSelect.setString(7, "%" + query + "%");
						stmtSelect.setString(8, "%" + query + "%");
						stmtSelect.setInt(9, start);
						stmtSelect.setInt(10, rows);
					}
				
				try (ResultSet rs = stmtSelect.executeQuery()) {
					while (rs.next()) {
						User u1 = new User();
						u1.setUserid(rs.getLong("userid"));
						u1.setUsername(rs.getString("username"));
						u1.setName(rs.getString("name"));
						u1.setLastupdate(rs.getDate("lastupdate"));
						u1.setFriendscount(rs.getInt("friendscount"));
						u1.setFollowerscount(rs.getInt("followerscount"));
						u1.setListedcount(rs.getInt("listedcount"));
						u1.setFavoritecount(rs.getInt("favoritecount"));
						u1.setTweetcount(rs.getInt("tweetcount"));
						u1.setRetweetcount(rs.getInt("retweets"));
						users.add(u1);
					}
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			safeClose(conn);
		}
		return users;
	}


	public UserPagingInfo selectUsersInfo(int start, int rows, String query)
			throws SQLException {
		UserPagingInfo result = new UserPagingInfo();

		try {
			conn = TwitterDatabaseConnector.getConnection();

			try (PreparedStatement stmtSelect = conn
					.prepareStatement(query == null ? "SELECT COUNT(*) FROM user WHERE username IS NOT NULL AND tweetcount > 0;"
							: "SELECT COUNT(*) FROM user WHERE username IS NOT NULL AND tweetcount > 0 AND (username LIKE ? OR name LIKE ?);")) {
				if (query != null) {
					stmtSelect.setString(1, "%" + query + "%");
					stmtSelect.setString(2, "%" + query + "%");
				}
				try (ResultSet rs = stmtSelect.executeQuery()) {
					while (rs.next()) {
						int count = rs.getInt(1);
						result.setCount(count);
						result.setPages((int) Math.ceil((double) count
								/ (double) rows));
						result.setPage((int) Math.ceil((double) start
								/ (double) rows) + 1);
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			safeClose(conn);
		}
		return result;
	}

	

	

	
	public Map<User, Map<User, SocialInteraction>> extractSocialInteraction(
	// double factor_retweet, double factor_follows, double factor_blocks,
	// double factor_replies, double factor_mentions,
	// double factor_favorites, double factor_listed, double factor_friends
	) throws SQLException {

		Map<User, Map<User, SocialInteraction>> result = new HashMap<User, Map<User, SocialInteraction>>();
		Map<Long, User> users = new HashMap<Long, User>();
		try {
			conn = TwitterDatabaseConnector.getConnection();

			try (PreparedStatement stmtSelect = conn
					.prepareStatement("SELECT u1uid, u1.username u1name, u2uid, u2.username u2name, u1.name u1fullname, u2.name u2fullname, "
							+ "u1.friendscount u1friends, u1.followerscount u1followers, u1.listedcount u1listed, u1.favoritecount u1favorite, u1.tweetcount u1tweets, u1retweets, "
							+ "u2.friendscount u2friends, u2.followerscount u2followers, u2.listedcount u2listed, u2.favoritecount u2favorite, u2.tweetcount u2tweets, u2retweets, "
							+ " SUM(retweets) retweets, SUM(follows) follows, SUM(blocks) blocks, SUM(replies) replies, SUM(mentions) mentions, SUM(favorites) favorites FROM "
							+ "(SELECT r.creator_userid u1uid, t.creator_userid u2uid, COUNT(distinct r.Retweet_tweetid) retweets, 0 follows, 0 blocks, 0 replies, 0 mentions, 0 favorites "
							+ "FROM tweet r "
							+ "INNER JOIN tweet t ON r.Retweet_tweetid = t.tweetid "
							+ "GROUP BY r.creator_userid, t.creator_userid "
							+ "UNION "
							+ "SELECT f.follower_userid u1uid, f.followed_userid u2uid, 0, COUNT(distinct f.followed_userid) follows, 0, 0, 0, 0 "
							+ "FROM UserFollowsUser f "
							+ "GROUP BY f.follower_userid, f.followed_userid "
							+ "UNION "
							+ "SELECT b.blocker_userid u1uid, b.blocked_userid, 0, 0, COUNT(distinct b.blocked_userid) blocks, 0, 0, 0 "
							+ "FROM UserBlocksUser b "
							+ "GROUP BY b.blocker_userid, b.blocked_userid "
							+ "UNION "
							+ "SELECT t.creator_userid u1uid, t2.creator_userid u2uid, 0, 0, 0, COUNT(distinct t.tweetid) replies, 0, 0 "
							+ "FROM  tweet t "
							+ "INNER JOIN tweet t2 ON t.reply_tweetid = t2.tweetid "
							+ "GROUP BY t.creator_userid, t2.creator_userid "
							+ "UNION "
							+ "SELECT t.creator_userid u1uid, tmu.User_userid u2uid, 0, 0, 0, 0, COUNT(distinct t.tweetid) mentions, 0 "
							+ "FROM tweet t "
							+ "INNER JOIN tweetmentionsuser tmu ON t.tweetid = tmu.Tweet_tweetid "
							+ "GROUP BY t.creator_userid, tmu.User_userid "
							+ "UNION "
							+ "SELECT uft.User_userid u1uid, t.creator_userid u2uid, 0, 0, 0, 0, 0, COUNT(distinct t.tweetid) favorites "
							+ "FROM userfavoritestweet uft "
							+ "INNER JOIN tweet t ON t.tweetid = uft.Tweet_tweetid "
							+ "GROUP BY uft.User_userid, t.creator_userid) firstquery "
							+ "INNER JOIN user u2 ON u2.userid = u2uid "
							+ "INNER JOIN user u1 ON u1.userid = u1uid AND u1uid != u2uid "
							+ "INNER JOIN (SELECT userid subid1,MAX(IFNULL(retweetcount,0)) u1retweets FROM user LEFT OUTER JOIN tweet ON creator_userid = userid GROUP BY userid) secondquery "
							+ "ON subid1=u1uid "
							+ "INNER JOIN (SELECT userid subid2,MAX(IFNULL(retweetcount,0)) u2retweets FROM user LEFT OUTER JOIN tweet ON creator_userid = userid GROUP BY userid) thirdquery "
							+ "ON subid2=u2uid " + "GROUP BY u1uid, u2uid;");) {

				try (ResultSet rs = stmtSelect.executeQuery()) {
					while (rs.next()) {
						long uid1 = rs.getLong("u1uid");
						User u1 = users.get(uid1);
						if (u1 == null) {
							u1 = new User();
							u1.setUserid(uid1);
							u1.setUsername(rs.getString("u1name"));
							u1.setName(rs.getString("u1fullname"));
							u1.setFriendscount(rs.getInt("u1friends"));
							u1.setFollowerscount(rs.getInt("u1followers"));
							u1.setListedcount(rs.getInt("u1listed"));
							u1.setFavoritecount(rs.getInt("u1favorite"));
							u1.setTweetcount(rs.getInt("u1tweets"));
							u1.setRetweetcount(rs.getInt("u1retweets"));
							users.put(uid1, u1);
							result.put(u1,
									new HashMap<User, SocialInteraction>());
						}
						long uid2 = rs.getLong("u2uid");
						User u2 = users.get(uid2);
						if (u2 == null) {
							u2 = new User();
							u2.setUserid(uid2);
							u2.setUsername(rs.getString("u2name"));
							u2.setName(rs.getString("u2fullname"));
							u2.setFriendscount(rs.getInt("u2friends"));
							u2.setFollowerscount(rs.getInt("u2followers"));
							u2.setListedcount(rs.getInt("u2listed"));
							u2.setFavoritecount(rs.getInt("u2favorite"));
							u2.setTweetcount(rs.getInt("u2tweets"));
							u2.setRetweetcount(rs.getInt("u2retweets"));
							users.put(uid2, u2);
							result.put(u2,
									new HashMap<User, SocialInteraction>());
						}
						// double interaction = rs.getInt("retweets")
						// * factor_retweet + rs.getInt("follows")
						// * factor_follows + rs.getInt("blocks")
						// * factor_blocks + rs.getInt("replies")
						// * factor_replies + rs.getInt("mentions")
						// * factor_mentions + rs.getInt("favorites")
						// * factor_favorites;
						// if (interaction > 0) {
						result.get(u1).put(
								u2,
								new SocialInteraction(rs.getInt("retweets"), rs
										.getInt("follows"),
										rs.getInt("blocks"), rs
												.getInt("replies"), rs
												.getInt("mentions"), rs
												.getInt("favorites")));
						// u2.setInfluence(u2.getInfluence()
						// + Math.max(0,
						// interaction - rs.getInt("follows")
						// * factor_follows));
						// }
					}
				}
			}
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			safeClose(conn);
		}
		return result;
	}

	public void updateUser(User u) {

		String result = null;
		int rows = 0, totalrows = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement stmt = conn
					.prepareStatement("UPDATE user SET retweets = GREATEST(retweets, ?) WHERE userid=?;")) {
				stmt.setInt(1, u.getRetweetcount());
				stmt.setLong(2, u.getUserid());
				rows = stmt.executeUpdate();
			}
			if (rows == 0)
				System.out.println("No Rows affected");
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(conn);
		}
	}

	public String clearTempRows() throws SQLException {

		String result = null;
		long rows = 0, totalrows = 0, tweets = 0, mentions = 0, follows = 0, blocks = 0, favorites = 0, hashtags = 0;
		try {
			conn = TwitterDatabaseConnector.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement stmtDelete = conn
					.prepareStatement("DELETE FROM tweetmentionsuser;")) {
				totalrows += mentions = stmtDelete.executeUpdate();
			}
			try (PreparedStatement stmtDelete = conn
					.prepareStatement("DELETE FROM userfavoritestweet;")) {
				totalrows += favorites = stmtDelete.executeUpdate();
			}
			try (PreparedStatement stmtDelete = conn
					.prepareStatement("DELETE FROM userfollowsuser;")) {
				totalrows += follows = stmtDelete.executeUpdate();
			}
			try (PreparedStatement stmtDelete = conn
					.prepareStatement("DELETE FROM userblocksuser;")) {
				totalrows += blocks = stmtDelete.executeUpdate();
			}
			try (PreparedStatement stmtDelete = conn
					.prepareStatement("DELETE FROM hashtag;")) {
				totalrows += hashtags = stmtDelete.executeUpdate();
			}
			try (PreparedStatement stmtDelete = conn
					.prepareStatement("UPDATE tweet SET reply_tweetid = NULL WHERE reply_tweetid IS NOT NULL;")) {
				stmtDelete.executeUpdate();
			}
			try (PreparedStatement stmtDelete = conn
					.prepareStatement("UPDATE tweet SET retweet_tweetid = NULL WHERE retweet_tweetid IS NOT NULL;")) {
				stmtDelete.executeUpdate();
			}
			try (PreparedStatement stmtDelete = conn
					.prepareStatement("DELETE FROM tweet;")) {
				totalrows += tweets = stmtDelete.executeUpdate();
			}

			
			if (totalrows == 0)
					throw new SQLException(
							"Clear temp records failed, no rows affected");
			

			conn.commit();
			result = totalrows + " rows affected";
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw e;
		} finally {
			safeClose(conn);
		}
		return result;
	}

	private void safeClose(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public List<Tweet> getUnclassifiedTweetsRange(String min, String max)
	{
		List<Tweet> tweetList = new ArrayList<Tweet>();
		
		String sql="select * from Tweet t Inner Join User u on t.creator_userid=u.userid where content is not null and Result = -1 and rowid between ? and ?";

		try {
			PreparedStatement ps = TwitterDatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, min);
			ps.setString(2, max);
			ResultSet rs = ps.executeQuery();
			
			tweetList=extractTweetFromResultSet(rs);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 

		return tweetList;
	}
	public List<Tweet> getRandomUnclassifiedTweetsByCount(int count){
		List<Tweet> tweetList = new ArrayList<Tweet>();
		
		String sql="select * from Tweet t Inner Join User u on t.creator_userid=u.userid where content is not null and Result = -1 and rowid > ? LIMIT ?";


		try {
			PreparedStatement ps = TwitterDatabaseConnector.getConnection().prepareStatement(sql);
			ps.setLong(1, ThreadLocalRandom.current().nextLong(735939772760928800L, COMMENTS_COUNT));
			ps.setInt(2, count);
			ResultSet rs = ps.executeQuery();
			
			tweetList=extractTweetFromResultSet(rs);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 

		return tweetList;
	}
	
	public List<Tweet> getRandomUnclassifiedTweetsContainingWordByCount(int count, String word){
		
		List<Tweet> tweetList = new ArrayList<Tweet>();
		
		String sql="select * from Tweet t Inner Join User u on t.creator_userid=u.userid where content like ? and Result = -1 LIMIT ?";
		

		try {
			PreparedStatement ps = TwitterDatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, "%"+word+"%");
			//ps.setLong(2, 738760752698429440L);
			ps.setInt(2, count);
			ResultSet rs = ps.executeQuery();
			
			tweetList=extractTweetFromResultSet(rs);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 

		return tweetList;
	}
	public List<Tweet> getClassifiedTweets(){
		
		List<Tweet> tweetList = new ArrayList<Tweet>();
		
		String sql="select * from Tweet t Inner Join User u on t.creator_userid=u.userid where Result>-1 and result!=3";
		

		try {
			PreparedStatement ps = TwitterDatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			tweetList=extractTweetFromResultSet(rs);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 

		return tweetList;
	}
	private List<Tweet> extractTweetFromResultSet(ResultSet rs) throws SQLException
	{
		List<Tweet> tweetList = new ArrayList<Tweet>();
		long repliedId,retweetedId;
		boolean reply, retweet;
		Set<TweetImage> twImages;
		Set<User> twMentions;
		
		while (rs.next()) 
		{
			repliedId=rs.getLong("reply_tweetid");
			if(!rs.wasNull())
				reply=true;
			else
				reply=false;
			
			retweetedId=rs.getLong("retweet_tweetid");
			if(!rs.wasNull())
				retweet=true;
			else
				retweet=false;
			
			twImages=getImagesFromTweetId(rs.getLong("tweetid"));
			twMentions=getMentionedUsersFromTweetId(rs.getLong("tweetid"));
			
			User user=new User(rs.getLong("userid"),rs.getString("username"),rs.getString("name"),null,rs.getInt("friendscount"),rs.getInt("followerscount"),rs.getInt("listedcount"),rs.getInt("favoritecount"),rs.getInt("tweetcount"));
			tweetList.add(new Tweet(rs.getLong("tweetid"),user,rs.getString("content"),rs.getInt("retweetcount"),retweet,reply,twImages,twMentions,rs.getInt("result")));
		}
		return tweetList;
	}
	private Set<TweetImage> getImagesFromTweetId(long tweetid) throws SQLException
	{
		String sqlImages="select * from Image where Tweet_tweetid=?";
		
		PreparedStatement psImage = TwitterDatabaseConnector.getConnection().prepareStatement(sqlImages);
		psImage.setLong(1, tweetid);
		ResultSet images = psImage.executeQuery();
		
		Set<TweetImage> twImages=new HashSet<TweetImage>();
		
		while(images.next())
		{
			twImages.add(new TweetImage(images.getInt("imageid"),images.getString("url"),null));
		}
		
		return twImages;
	}
	private Set<User> getMentionedUsersFromTweetId(long tweetid) throws SQLException
	{
		String sqlImages="select * from TweetMentionsUser where Tweet_tweetid=?";
		
		PreparedStatement psImage = TwitterDatabaseConnector.getConnection().prepareStatement(sqlImages);
		psImage.setLong(1, tweetid);
		ResultSet mentions = psImage.executeQuery();
		
		Set<User> twMentions=new HashSet<User>();
		
		while(mentions.next())
		{
			twMentions.add(new User(mentions.getLong("User_userid")));
		}
		
		return twMentions;
	}
	public void updateResult(long id,int result) 
	{
		String sql="UPDATE Tweet SET result = ? WHERE tweetid=?";
				
		try {
			PreparedStatement ps = TwitterDatabaseConnector.getConnection().prepareStatement(sql);
			ps.setInt(1, result);
			ps.setLong(2,id);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
}
