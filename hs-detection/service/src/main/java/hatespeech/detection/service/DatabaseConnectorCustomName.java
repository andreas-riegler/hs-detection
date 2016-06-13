package hatespeech.detection.service;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.FBComment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.Encoding;

public class DatabaseConnectorCustomName{
	private static final String DRIVER = "org.sqlite.JDBC";

	private String url = "jdbc:sqlite:cq.sqlite";
	private Connection connection = null;
	private SQLiteConfig c = new SQLiteConfig();

	public DatabaseConnectorCustomName(String url) {
		super();
		this.url = url;
	}

	public Connection getConnection(){
		if(connection == null){
			openConnection();
		}
		return connection;
	}

	public void openConnection() {
		Connection con = null;
		try {
			Class.forName(DRIVER);
			c.setEncoding(Encoding.UTF8);
			con = DriverManager.getConnection(url, c.toProperties());			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}

		connection = con;

		createDB();
	}

	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void createDB(){
		try {

			String createHPTableSQL = "CREATE TABLE if not exists HatePost "
					+ "(ID INTEGER PRIMARY KEY  AUTOINCREMENT,"
					+ " InternID   TEXT   UNIQUE, "
					+ " Post       TEXT NOT NULL UNIQUE, "
					+ " Link      TEXT,"
					+ " typedDependencies TEXT,"
					+ " Result INTEGER);";

			PreparedStatement  preStat = DatabaseConnector.getConnection().prepareStatement(createHPTableSQL);
			preStat.executeUpdate();

			String createFBPTableSQL="CREATE TABLE if not exists FBPost (id TEXT PRIMARY KEY, commentsCount INTEGER, createdTime TEXT, "
					+ "fromId TEXT, likesCount INTEGER, message TEXT, sharesCount INTEGER, type TEXT, description TEXT, caption TEXT, "
					+ "fullPicture TEXT, isExpired BOOLEAN, isHidden BOOLEAN, isPublished BOOLEAN, link TEXT, name TEXT, permalinkUrl TEXT, "
					+ "statusType TEXT, timelineVisibility TEXT, reactionsCount INTEGER);";
			
			preStat = DatabaseConnector.getConnection().prepareStatement(createFBPTableSQL);
			preStat.executeUpdate();

			String createFBCTableSQL="CREATE TABLE if not exists FBComment (id TEXT PRIMARY KEY, postId TEXT, createdTime TEXT, commentCount INTEGER, fromId TEXT, "
					+ "likeCount INTEGER, message TEXT, parentId TEXT, isHidden BOOLEAN, attachmentMediaImageSrc TEXT, "
					+ "typedDependencies TEXT, result INTEGER, FOREIGN KEY (postId) REFERENCES FBPost (id), FOREIGN KEY (parentId) REFERENCES FBComment (id));";
			
			preStat = DatabaseConnector.getConnection().prepareStatement(createFBCTableSQL);
			preStat.executeUpdate();
			
			String createFBRTableSQL="CREATE TABLE if not exists FBReaction (postId TEXT REFERENCES FBPost (id), userId TEXT, type TEXT, UNIQUE (postId, userId));";
			
			preStat = DatabaseConnector.getConnection().prepareStatement(createFBRTableSQL);
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

	public static void main(String[] args) {
		DatabaseConnectorCustomName dbc1 = new DatabaseConnectorCustomName("jdbc:sqlite:../hs_save.sqlite");
		DatabaseConnectorCustomName dbc2 = new DatabaseConnectorCustomName("jdbc:sqlite:../hs.sqlite");

		JDBCFBCommentDAO dao1 = new JDBCFBCommentDAO(dbc1);
		JDBCFBCommentDAO dao2 = new JDBCFBCommentDAO(dbc2);

		List<FBComment> comments1 = dao1.getClassifiedFBCommentsCustomName();
		List<FBComment> comments2 = dao2.getUnclassifiedFBCommentsCustomName();
				
		System.out.println("hs_save: " + comments1.size());
		System.out.println("hs: " + comments2.size());
			
		comments1.stream()
			.filter(c -> c.getResult() == 1)
			.filter(c -> comments2.contains(c))
			.forEach(c -> dao2.updateResult(c.getId(), 1));
	}
}
