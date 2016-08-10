package hatespeech.detection.model;

// Generated Nov 6, 2014 9:08:37 PM by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Tweet generated by hbm2java
 */
public class Tweet implements java.io.Serializable,IPosting {

	private static final long serialVersionUID = 4171552682746310915L;
	private long tweetid;
	private User user;
	private Tweet repliedTweet;
	private Tweet retweetedTweet;
	private String message;
	private Date createdat;
	private int retweetcount;
	private int favouritecount;
	private boolean retweet,reply;
	private Set<TweetImage> twImages=new HashSet<TweetImage>();
	private Set<Tweet> answers = new HashSet<Tweet>();
	private Set<Hashtag> hashtags = new HashSet<Hashtag>();
	private Set<User> retweetUsers = new HashSet<User>();
	private Set<User> mentionUsers = new HashSet<User>();
	private Set<User> favoriteUsers = new HashSet<User>();
	private String typedDependencies;
	private int result;

	public Tweet() {
	}

	public Tweet(long tweetid, String message) {
		this.tweetid = tweetid;
		this.message = message;
		
	}
	public Tweet(long tweetid, User user, String message,
			int retweetcount, int favouritecount, boolean retweet,boolean reply,Set<TweetImage> twImages,Set<User> mentionUsers,int result) {
		this.tweetid = tweetid;
		this.user = user;
		this.message = message;
		this.retweetcount = retweetcount;
		this.favouritecount = favouritecount;
		this.retweet = retweet;
		this.reply = reply;
		this.twImages=twImages;
		this.mentionUsers=mentionUsers;
		this.result=result;
	}

	public Tweet(long tweetid, User user, Tweet repliedTweet, String message,
			Date createdat, int retweetcount, boolean retweet,
			Set<Tweet> answers, Set<Hashtag> hashtags, Set<User> retweetUsers,
			Set<User> mentionUsers, Set<User> favoriteUsers,String typedDependencies,int result) {
		this.tweetid = tweetid;
		this.user = user;
		this.repliedTweet = repliedTweet;
		this.message = message;
		this.createdat = createdat;
		this.retweetcount = retweetcount;
		this.retweet = retweet;
		this.answers = answers;
		this.hashtags = hashtags;
		this.retweetUsers = retweetUsers;
		this.mentionUsers = mentionUsers;
		this.favoriteUsers = favoriteUsers;
		this.typedDependencies=typedDependencies;
		this.result=result;
	}

	public long getTweetid() {
		return this.tweetid;
	}

	public void setTweetid(long tweetid) {
		this.tweetid = tweetid;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Tweet getReplyTo() {
		return this.repliedTweet;
	}

	public void setReplyTo(Tweet repliedTweet) {
		this.repliedTweet = repliedTweet;
	}

	public Tweet getRetweetedTweet() {
		return this.retweetedTweet;
	}

	public void setRetweetedTweet(Tweet retweet) {
		this.retweetedTweet = retweet;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getCreatedat() {
		return this.createdat;
	}

	public void setCreatedat(Date createdat) {
		this.createdat = createdat;
	}

	public int getRetweetcount() {
		return this.retweetcount;
	}

	public void setRetweetcount(int retweetcount) {
		this.retweetcount = retweetcount;
	}
	
	
	public int getFavouritecount() {
		return favouritecount;
	}

	public void setFavouritecount(int favouritecount) {
		this.favouritecount = favouritecount;
	}

	public boolean isRetweet() {
		return retweet;
	}

	public void setRetweet(boolean retweet) {
		this.retweet = retweet;
	}

	public Set<Tweet> getTweets() {
		return this.answers;
	}

	public void setTweets(Set<Tweet> answers) {
		this.answers = answers;
	}

	public Set<Hashtag> getHashtags() {
		return this.hashtags;
	}

	public void setHashtags(Set<Hashtag> hashtags) {
		this.hashtags = hashtags;
	}

	public Set<User> getRetweetUsers() {
		return this.retweetUsers;
	}

	public void setUsers(Set<User> retweetUsers) {
		this.retweetUsers = retweetUsers;
	}

	public Set<User> getMentionUsers() {
		return this.mentionUsers;
	}

	public void setMentionUsers(Set<User> mentionUsers) {
		this.mentionUsers = mentionUsers;
	}

	public Set<User> getFavoriteUsers() {
		return this.favoriteUsers;
	}

	public void setFavoriteUsers(Set<User> favoriteUsers) {
		this.favoriteUsers = favoriteUsers;
	}

	public Set<TweetImage> getTwImages() {
		return twImages;
	}

	public void setTwImages(Set<TweetImage> twImages) {
		this.twImages = twImages;
	}

	public String getTypedDependencies() {
		return typedDependencies;
	}

	public void setTypedDependencies(String typedDependencies) {
		this.typedDependencies = typedDependencies;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public boolean isReply() {
		return reply;
	}

	public void setReply(boolean reply) {
		this.reply = reply;
	}

	@Override
	public PostType getPostType() {
		switch(getResult()){
		case 0: return PostType.NEGATIVE;
		case 1: return PostType.POSITIVE;
		case 2: return PostType.POSITIVE;
		case 3: return PostType.POSITIVE;
		default: return null;
		}
	}
	
}
