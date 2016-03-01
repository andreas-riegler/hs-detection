package hatespeech.detection.dao;

import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.FBPost;
import hatespeech.detection.service.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;

import com.restfb.util.CachedDateFormatStrategy;


public class JDBCFBCommentDAO{
	
	CachedDateFormatStrategy cdfs = new CachedDateFormatStrategy();
	DateFormat df = cdfs.formatFor("dd.MM.yyyy HH:mm:ss");
	
	public void insertFBPost(FBPost p) throws IllegalArgumentException{
		
		if (p == null) {
			throw new IllegalArgumentException("p must not be null");
		}

		String sql = "insert into FBPost values(?,?,?,?,?,?,?,?)";	
		
		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, p.getId());
			ps.setLong(2, p.getCommentsCount());
			ps.setString(3, df.format(p.getCreatedTime()));
			ps.setString(4, p.getFromId());
			ps.setLong(5, p.getLikesCount());
			ps.setString(6, p.getMessage());
			ps.setLong(7, p.getSharesCount());
			ps.setString(8, p.getType());

			ps.executeUpdate();

			ps.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void insertFBComment(FBComment c) throws IllegalArgumentException{
		if (c == null) {
			throw new IllegalArgumentException("c must not be null");
		}

		String sql = "insert into FBComment values(?,?,?,?,?,?,?,?)";	
		
		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, c.getId());
			ps.setString(2, c.getPostId() != null ? c.getPostId() : "null");
			ps.setString(3, df.format(c.getCreatedTime()));
			ps.setLong(4, c.getCommentCount());
			ps.setString(5, c.getFromId());
			ps.setLong(6, c.getLikeCount());
			ps.setString(7, c.getMessage());
			ps.setString(8, c.getParentId() != null ? c.getParentId() : "null");

			ps.executeUpdate();

			ps.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public boolean existsFBPostId(String id){
		
		String sql = "select count(id) from FBPost where id = ?";	
		
		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, id);

			ResultSet rs = ps.executeQuery();
			int count = rs.getInt(1);
			rs.close();
			ps.close();
			
			return count > 0;

			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return true;
		}
	}
	
	public boolean existsFBCommentId(String id){
		
		String sql = "select count(id) from FBComment where id = ?";	
		
		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, id);

			ResultSet rs = ps.executeQuery();
			int count = rs.getInt(1);
			rs.close();
			ps.close();
			
			return count > 0;

			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return true;
		}
	}

}
