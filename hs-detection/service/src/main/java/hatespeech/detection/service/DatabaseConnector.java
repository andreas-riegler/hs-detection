package hatespeech.detection.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.Encoding;

public class DatabaseConnector {
	private static final String URL = "jdbc:sqlite:../hs.sqlite";
	//private static final String USER = "SA";
	//private static final String PASSWORD = "";
	private static final String DRIVER = "org.sqlite.JDBC";

	private static Connection connection = null;
	
	private static SQLiteConfig c = new SQLiteConfig();

	private DatabaseConnector(){}

	public static Connection getConnection(){
		if(connection == null){
			openConnection();
		}
		return DatabaseConnector.connection;
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

		DatabaseConnector.connection = con;
		
		createDB();
	}

	public static void closeConnection() {
		try {
			DatabaseConnector.connection.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void createDB(){
		try {

			String createHPTableSQL = "CREATE TABLE if not exists HatePost "
					+ "(ID INTEGER PRIMARY KEY  AUTOINCREMENT,"
					+ " InternID   TEXT   UNIQUE, "
					+ " Post       TEXT NOT NULL UNIQUE, "
					+ " Link      TEXT,"
					+ " Result INTEGER);";

			PreparedStatement  preStat = DatabaseConnector.getConnection().prepareStatement(createHPTableSQL);
			preStat.executeUpdate();

			String createFBPTableSQL="CREATE TABLE if not exists FBPost (id TEXT PRIMARY KEY, commentsCount INTEGER, createdTime TEXT, fromId TEXT, likesCount INTEGER, message TEXT, sharesCount INTEGER, type TEXT);";
			preStat = DatabaseConnector.getConnection().prepareStatement(createFBPTableSQL);
			preStat.executeUpdate();

			String createFBCTableSQL="CREATE TABLE if not exists FBComment (id TEXT PRIMARY KEY, postId TEXT, createdTime TEXT, commentCount INTEGER, fromId TEXT, likeCount INTEGER, message TEXT, parentId TEXT, typedDependencies TEXT, result INTEGER, FOREIGN KEY (postId) REFERENCES FBPost (id), FOREIGN KEY (parentId) REFERENCES FBComment (id));";
			preStat = DatabaseConnector.getConnection().prepareStatement(createFBCTableSQL);
			preStat.executeUpdate();

			String activateFKSQL = "PRAGMA foreign_keys = ON;";
			preStat = DatabaseConnector.getConnection().prepareStatement(activateFKSQL);
			preStat.executeUpdate();
			
			preStat.close();
			System.out.println("Table created successfully");

		} catch (SQLException e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
}
