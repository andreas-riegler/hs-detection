package hatespeech.detection.dao;

import hatespeech.detection.model.HatePost;
import hatespeech.detection.service.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import com.restfb.util.CachedDateFormatStrategy;


public class JDBCHSPostDAO{

	CachedDateFormatStrategy cdfs = new CachedDateFormatStrategy();
	DateFormat df = cdfs.formatFor("dd.MM.yyyy HH:mm:ss");
	
	public void insertPost(HatePost hp) throws IllegalArgumentException{
		if (hp == null) {
			throw new IllegalArgumentException("hp must not be null");
		}

		String sql = "insert into HatePost values(NULL,?,?,?,?,?)";	

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, hp.getId());
			ps.setString(2, hp.getPost());
			ps.setString(3, hp.getLink());	
			//typedDependencies
			ps.setNull(4, java.sql.Types.VARCHAR);
			ps.setInt(5, 1);

			ps.executeUpdate();

			ps.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	public List<HatePost> getAllPosts()
	{
		List<HatePost> hpList=new ArrayList<HatePost>();
		String sql="Select * from HatePost";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();


			while (rs.next()) 
			{
				hpList.add(new HatePost(rs.getString("ID"), rs.getString("InternID"), rs.getString("Post"), rs.getString("Link"), rs.getString("typedDependencies"), rs.getInt("Result")));
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return hpList;
	}

	public void updateHatePostSetTypedDependenciesById(String id, String typedDependencies) throws IllegalArgumentException{

		String sql = "update HatePost set typedDependencies = ? where id = ?";	

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, typedDependencies);
			ps.setString(2, id);

			ps.executeUpdate();

			ps.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

}
