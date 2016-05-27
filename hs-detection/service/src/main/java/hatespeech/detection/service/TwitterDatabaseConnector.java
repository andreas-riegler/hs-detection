package hatespeech.detection.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.Encoding;

public class TwitterDatabaseConnector {
	
	public static void main(String[] args) {
		getConnection();

	}
	private static final String URL = "jdbc:sqlite:../tw.sqlite";
	//private static final String USER = "SA";
	//private static final String PASSWORD = "";
	private static final String DRIVER = "org.sqlite.JDBC";

	private static Connection connection = null;
	
	private static SQLiteConfig c = new SQLiteConfig();

	private TwitterDatabaseConnector(){}

	public static Connection getConnection(){
		if(connection == null){
			openConnection();
		}
		return TwitterDatabaseConnector.connection;
	}

	public static void openConnection() {
		Connection con = null;
		try {
			Class.forName(DRIVER);
			c.setEncoding(Encoding.UTF8);
			con = DriverManager.getConnection(URL, c.toProperties());			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}

		TwitterDatabaseConnector.connection = con;
		
		createDB();
	}

	public static void closeConnection() {
		try {
			TwitterDatabaseConnector.connection.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void createDB(){
		try {
			
			String createUserTableSQL = "CREATE TABLE IF NOT EXISTS User("
					+ "userid INTEGER PRIMARY KEY,"
					+ "username TEXT,"
					+ "name TEXT ,"
					+ "lastupdate TEXT,"
					+ "friendscount INTEGER ,"
					+ "followerscount INTEGER ,"
					+ "listedcount INTEGER ,"
					+ "favoritecount INTEGER ,"
					+ "tweetcount INTEGER ,"
					+ "retweets INTEGER );";

			PreparedStatement  preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createUserTableSQL);
			preStat.executeUpdate();

			String createTweetTableSQL="CREATE TABLE IF NOT EXISTS Tweet ("
					+ "tweetid INTEGER PRIMARY KEY,"
					+ "creator_userid INTEGER,"
					+ "content TEXT,"
					+ "createdat TEXT,"
					+ "reply_tweetid INTEGER NULL,"
					+ "retweetcount INTEGER,"
					+ "retweet_tweetid INTEGER NULL,"
					+ "FOREIGN KEY(creator_userid)"
					+ "REFERENCES User(userid)"
					+ "ON DELETE NO ACTION"
					+ " ON UPDATE NO ACTION,"
					+ "FOREIGN KEY(reply_tweetid)"
					+ "REFERENCES Tweet(tweetid)"
					+ "ON DELETE NO ACTION"
					+ " ON UPDATE NO ACTION,"
					+ "FOREIGN KEY(retweet_tweetid)"
					+ "REFERENCES Tweet(tweetid)"
					+ "ON DELETE NO ACTION"
					+ " ON UPDATE NO ACTION)";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createTweetTableSQL);
			preStat.executeUpdate();

			String createUserFollowsUserTableSQL="CREATE TABLE IF NOT EXISTS UserFollowsUser ("
					+ "follower_userid INTEGER NOT NULL,"
					+ "followed_userid INTEGER NOT NULL,"
					+ "PRIMARY KEY(follower_userid,followed_userid),"
					+ "FOREIGN KEY (follower_userid)"
					+ "REFERENCES User (userid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION,"
					+ "FOREIGN KEY (followed_userid)"
					+ "REFERENCES User (userid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION)";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createUserFollowsUserTableSQL);
			preStat.executeUpdate();

			String createUserBlocksUserTableSQL="CREATE TABLE IF NOT EXISTS UserBlocksUser ("
					+ "blocker_userid INTEGER NOT NULL,"
					+ "blocked_userid INTEGER NOT NULL,"
					+ "PRIMARY KEY(blocker_userid,blocked_userid),"
					+ "FOREIGN KEY (blocker_userid)"
					+ "REFERENCES User (userid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION,"
					+ "FOREIGN KEY (blocked_userid)"
					+ "REFERENCES User (userid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION)";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createUserBlocksUserTableSQL);
			preStat.executeUpdate();
			
			String createHashtagTableSQL="CREATE TABLE IF NOT EXISTS Hashtag ("
					+ "hashtagid INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "tag TEXT NOT NULL,"
					+ "Tweet_tweetid INTEGER NOT NULL,"
					+ "FOREIGN KEY (Tweet_tweetid)"
					+ "REFERENCES Tweet (tweetid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION)";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createHashtagTableSQL);
			preStat.executeUpdate();
			
			String createImagesTableSQL="CREATE TABLE IF NOT EXISTS Images ("
					+ "imagesid INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "url TEXT NOT NULL,"
					+ "Tweet_tweetid INTEGER NOT NULL,"
					+ "FOREIGN KEY (Tweet_tweetid)"
					+ "REFERENCES Tweet (tweetid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION)";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createImagesTableSQL);
			preStat.executeUpdate();
			
			String createRetweetTableSQL="CREATE TABLE IF NOT EXISTS Retweet ("
					+ "Tweet_tweetid INTEGER NOT NULL,"
					+ "User_userid INTEGER NOT NULL,"
					+ "PRIMARY KEY(Tweet_tweetid,User_userid),"
					+ "FOREIGN KEY (Tweet_tweetid)"
					+ "REFERENCES Tweet (tweetid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION,"
					+ "FOREIGN KEY (User_userid)"
					+ "REFERENCES User (userid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION)";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createRetweetTableSQL);
			preStat.executeUpdate();
			
			String createUserFavoritesTweetTableSQL="CREATE TABLE IF NOT EXISTS UserFavoritesTweet ("
					+ "User_userid INTEGER NOT NULL,"
					+ "Tweet_tweetid INTEGER NOT NULL,"
					+ "PRIMARY KEY(User_userid,Tweet_tweetid),"
					+ "FOREIGN KEY (User_userid)"
					+ "REFERENCES User (userid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION,"
					+ "FOREIGN KEY (Tweet_tweetid)"
					+ "REFERENCES Tweet (tweetid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION)";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createUserFavoritesTweetTableSQL);
			preStat.executeUpdate();
			
			String createTweetMentionsUserTableSQL="CREATE TABLE IF NOT EXISTS TweetMentionsUser ("
					+ "Tweet_tweetid INTEGER NOT NULL,"
					+ "User_userid INTEGER NOT NULL,"
					+ "PRIMARY KEY(Tweet_tweetid,User_userid),"
					+ "FOREIGN KEY (Tweet_tweetid)"
					+ "REFERENCES Tweet (tweetid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION,"
					+ "FOREIGN KEY (User_userid)"
					+ "REFERENCES User (userid)"
					+ "ON DELETE NO ACTION "
					+ "ON UPDATE NO ACTION)";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(createTweetMentionsUserTableSQL);
			preStat.executeUpdate();
			
			String activateFKSQL = "PRAGMA foreign_keys = ON;";
			preStat = TwitterDatabaseConnector.getConnection().prepareStatement(activateFKSQL);
			preStat.executeUpdate();
			
			preStat.close();
			System.out.println("Table created successfully");

		} catch (SQLException e ) {
			e.printStackTrace();
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
}
