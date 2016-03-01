package hatespeech.detection.dao;

import hatespeech.detection.service.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateTable {
	
	public static void CreateHSTable() {
	 try {
	
	      String createTableSQL = "CREATE TABLE if not exists HatePost " +
	                   "(ID INTEGER PRIMARY KEY  AUTOINCREMENT," +
	                   " InternID   TEXT   UNIQUE, " + 
	                   " Post       TEXT NOT NULL UNIQUE, " + 
	                   " Link      TEXT)"; 
	      
	      PreparedStatement  preStat = DatabaseConnector.getConnection().prepareStatement(createTableSQL);

	      preStat.executeUpdate();
	      preStat.close();
	      System.out.println("Table created successfully");
	      
	    } catch (SQLException e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    
	  }
}

