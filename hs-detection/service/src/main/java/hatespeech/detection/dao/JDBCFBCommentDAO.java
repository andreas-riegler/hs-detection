package hatespeech.detection.dao;

import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.FBPost;
import hatespeech.detection.model.HatePost;
import hatespeech.detection.service.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.restfb.util.CachedDateFormatStrategy;


public class JDBCFBCommentDAO{

	CachedDateFormatStrategy cdfs = new CachedDateFormatStrategy();
	DateFormat df = cdfs.formatFor("dd.MM.yyyy HH:mm:ss");

	public void insertFBPost(FBPost p) throws IllegalArgumentException{

		if (p == null) {
			throw new IllegalArgumentException("p must not be null");
		}

		String sql = "insert into FBPost values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
			ps.setString(9, p.getDescription());
			ps.setString(10, p.getCaption());
			ps.setString(11, p.getFullPicture());
			ps.setBoolean(12, p.isExpired());
			ps.setBoolean(13, p.isHidden());
			ps.setBoolean(14, p.isPublished());
			ps.setString(15, p.getLink());
			ps.setString(16, p.getName());
			ps.setString(17, p.getPermalinkUrl());
			ps.setString(18, p.getStatusType());
			ps.setString(19, p.getTimelineVisibility());
			ps.setLong(20, p.getReactionsCount());

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

		String sql = "insert into FBComment values(?,?,?,?,?,?,?,?,?,?,?,?)";	

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, c.getId());			
			ps.setString(2, c.getPostId());
			ps.setString(3, df.format(c.getCreatedTime()));
			ps.setLong(4, c.getCommentCount());
			ps.setString(5, c.getFromId());
			ps.setLong(6, c.getLikeCount());
			ps.setString(7, c.getMessage());

			if(c.getParentId() != null){
				ps.setString(8, c.getParentId());
			}
			else{
				ps.setNull(8, java.sql.Types.VARCHAR);
			}				

			ps.setBoolean(9, c.isHidden());
			ps.setString(10, c.getAttachmentMediaImageSrc());
			
			//typedDependencies
			ps.setNull(11, java.sql.Types.VARCHAR);
			
			ps.setInt(12, -1);
			
			ps.executeUpdate();

			ps.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void updateFBCommentSetTypedDependenciesById(String id, String typedDependencies) throws IllegalArgumentException{
		
		String sql = "update FBComment set typedDependencies = ? where id = ?";	

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

	public FBPost getFBPostById(String postId)
	{
		String sql = "select * from FBPost where id = ?";	

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setString(1, postId);

			ResultSet rs = ps.executeQuery();

			FBPost post = new FBPost(rs.getString("id"), rs.getLong("commentsCount"), df.parse(rs.getString("createdTime")), rs.getString("fromId"),
					rs.getLong("likesCount"), rs.getString("message"), rs.getLong("sharesCount"), rs.getString("type"), rs.getString("description"), rs.getString("caption"),
					rs.getString("fullPicture"), rs.getBoolean("isExpired"), rs.getBoolean("isHidden"), rs.getBoolean("isPublished"), rs.getString("link"), 
					rs.getString("name"), rs.getString("permalinkUrl"), rs.getString("statusType"), rs.getString("timelineVisibility"), rs.getLong("reactionsCount"));

			rs.close();
			ps.close();

			return post;

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;

		} catch (ParseException e) {
			System.out.println(e.getMessage());
			return null;

		}
	}

	public List<FBPost> getFBPosts()
	{
		List<FBPost> postList = new ArrayList<FBPost>();

		String sql="select * from FBPost";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();


			while (rs.next()) 
			{
				postList.add(new FBPost(rs.getString("id"), rs.getLong("commentsCount"), df.parse(rs.getString("createdTime")), rs.getString("fromId"),
						rs.getLong("likesCount"), rs.getString("message"), rs.getLong("sharesCount"), rs.getString("type"), rs.getString("description"), rs.getString("caption"),
						rs.getString("fullPicture"), rs.getBoolean("isExpired"), rs.getBoolean("isHidden"), rs.getBoolean("isPublished"), rs.getString("link"), 
						rs.getString("name"), rs.getString("permalinkUrl"), rs.getString("statusType"), rs.getString("timelineVisibility"), rs.getLong("reactionsCount")));
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}

		return postList;
	}

	public List<FBComment> getFBComments()
	{
		List<FBComment> commentList = new ArrayList<FBComment>();

		String sql="select * from FBComment";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) 
			{
				commentList.add(new FBComment(rs.getString("id"), rs.getString("postId"), df.parse(rs.getString("createdTime")), rs.getLong("commentCount"),
						rs.getString("fromId"), rs.getLong("likeCount"), rs.getString("message"), rs.getString("parentId"), rs.getBoolean("isHidden"), 
						rs.getString("attachmentMediaImageSrc"), rs.getString("typedDependencies"), rs.getInt("result")));
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}

		return commentList;
	}

	public List<FBComment> getClassifiedFBComments()
	{
		List<FBComment> commentList = new ArrayList<FBComment>();
		
		String sql="select * from FBComment where Result != -1";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) 
			{
				commentList.add(new FBComment(rs.getString("id"), rs.getString("postId"), df.parse(rs.getString("createdTime")), rs.getLong("commentCount"),
						rs.getString("fromId"), rs.getLong("likeCount"), rs.getString("message"), rs.getString("parentId"), rs.getBoolean("isHidden"), 
						rs.getString("attachmentMediaImageSrc"), rs.getString("typedDependencies"), rs.getInt("result")));
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}

		return commentList;
	}
	public List<FBComment> getUnclassifiedFBCommentsRange(int min,int max)
	{
		List<FBComment> commentList = new ArrayList<FBComment>();
		
		
		String sql="select * from FBComment where Result = -1 and rowid between ? and ?";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setInt(1, min);
			ps.setInt(2, max);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) 
			{
				commentList.add(new FBComment(rs.getString("id"), rs.getString("postId"), df.parse(rs.getString("createdTime")), rs.getLong("commentCount"),
						rs.getString("fromId"), rs.getLong("likeCount"), rs.getString("message"), rs.getString("parentId"), rs.getBoolean("isHidden"), 
						rs.getString("attachmentMediaImageSrc"), rs.getString("typedDependencies"), rs.getInt("result")));
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}

		return commentList;
	}
	public void updateResult(String id,int result) 
	{
		String sql="UPDATE FBComment SET result = ? WHERE id=?";
				
		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ps.setInt(1, result);
			ps.setString(2,id);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
