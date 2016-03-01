package hatespeech.detection.dao;

import hatespeech.detection.model.HatePost;
import hatespeech.detection.service.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public class JDBCHSPostDAO{
	
	public void insertPost(HatePost hp) throws IllegalArgumentException{
		if (hp == null) {
			throw new IllegalArgumentException("hp must not be null");
		}

		String sql = "insert into HatePost values(NULL,?,?,?)";	
		
		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, hp.getId());
			ps.setString(2, hp.getPost());
			ps.setString(3, hp.getLink());


			ps.executeUpdate();

			ps.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

}
