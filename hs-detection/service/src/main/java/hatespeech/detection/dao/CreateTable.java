package hatespeech.detection.dao;

import hatespeech.detection.service.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateTable {
	
	public static void CreateHSTable() {
	 try {
	
			String createHPTableSQL = "CREATE TABLE if not exists HatePost "
					+ "(ID INTEGER PRIMARY KEY  AUTOINCREMENT,"
					+ " InternID   TEXT   UNIQUE, "
					+ " Post       TEXT NOT NULL UNIQUE, "
					+ " Link      TEXT);";

	      PreparedStatement  preStat = DatabaseConnector.getConnection().prepareStatement(createHPTableSQL);
	      preStat.executeUpdate();
	      
	      String createFBPTableSQL="CREATE TABLE if not exists FBPost (id TEXT PRIMARY KEY, commentsCount INTEGER, createdTime TEXT, fromId TEXT, likesCount INTEGER, message TEXT, sharesCount INTEGER, type TEXT);";
	      preStat = DatabaseConnector.getConnection().prepareStatement(createFBPTableSQL);
	      preStat.executeUpdate();
	      
	      String createFBCTableSQL="CREATE TABLE if not exists FBComment (id TEXT PRIMARY KEY, postId TEXT, createdTime TEXT, commentCount INTEGER, fromId TEXT, likeCount INTEGER, message TEXT, parentId TEXT, FOREIGN KEY (postId) REFERENCES FBPost (id), FOREIGN KEY (parentId) REFERENCES FBComment (id));";
	      preStat = DatabaseConnector.getConnection().prepareStatement(createFBCTableSQL);
	      preStat.executeUpdate();
	      
	      preStat.close();
	      System.out.println("Table created successfully");
	      
	    } catch (SQLException e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    
	  }
}

