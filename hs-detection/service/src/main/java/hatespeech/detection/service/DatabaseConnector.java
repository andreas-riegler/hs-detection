package hatespeech.detection.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
	private static final String URL = "jdbc:sqlite:fb1.sqlite";
	//private static final String USER = "SA";
	//private static final String PASSWORD = "";
	private static final String DRIVER = "org.sqlite.JDBC";

	private static Connection connection = null;
	
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
			con = DriverManager.getConnection(URL);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}

		DatabaseConnector.connection = con;
	}

	public static void closeConnection() {
		try {
			DatabaseConnector.connection.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
