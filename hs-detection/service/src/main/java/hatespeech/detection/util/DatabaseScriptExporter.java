package hatespeech.detection.util;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.FBPost;
import hatespeech.detection.service.DatabaseConnector;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.restfb.util.CachedDateFormatStrategy;

public class DatabaseScriptExporter {

	private static CachedDateFormatStrategy cdfs = new CachedDateFormatStrategy();
	private static DateFormat df = cdfs.formatFor("dd.MM.yyyy HH:mm:ss");

	public static void main(String[] args) {
		exportInsertOrReplacePostsAndComments();
	}

	@Deprecated
	public static void exportUpdateNP(){

		List<FBComment> commentList = new ArrayList<FBComment>();

		String sql = "select * from FBComment where result = 0";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) 
			{
				commentList.add(new FBComment(rs.getString("id"), rs.getString("postId"), df.parse(rs.getString("createdTime")), rs.getLong("commentCount"),
						rs.getString("fromId"), rs.getLong("likeCount"), rs.getString("message"), rs.getString("parentId"), rs.getString("typedDependencies"), rs.getInt("result")));
			}

			ps.close();

			Writer writer = new FileWriter("updateNP.sql");
			writer.write("UPDATE FBComment SET Result = 0 WHERE id IN (");

			for(int i = 0; i < commentList.size(); i++){
				writer.write('\"' + commentList.get(i).getId() + '\"' + (i != commentList.size() - 1? "," : ""));
			}

			writer.write(");");

			writer.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	@Deprecated
	public static void exportUpdateHP(){

		List<FBComment> commentList = new ArrayList<FBComment>();

		String sql = "select * from FBComment where result = 1";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) 
			{
				commentList.add(new FBComment(rs.getString("id"), rs.getString("postId"), df.parse(rs.getString("createdTime")), rs.getLong("commentCount"),
						rs.getString("fromId"), rs.getLong("likeCount"), rs.getString("message"), rs.getString("parentId"), rs.getString("typedDependencies"), rs.getInt("result")));
			}

			ps.close();

			Writer writer = new FileWriter("updateHP.sql");
			writer.write("UPDATE FBComment SET Result = 1 WHERE id IN (");

			for(int i = 0; i < commentList.size(); i++){
				writer.write('\"' + commentList.get(i).getId() + '\"' + (i != commentList.size() - 1? "," : ""));
			}

			writer.write(");");

			writer.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void exportInsertOrReplacePostsAndComments(){

		JDBCFBCommentDAO commentDao = new JDBCFBCommentDAO();

		List<FBComment> commentList = commentDao.getClassifiedFBComments();
		Set<String> postIdSet = new HashSet<String>();

		try {

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("insertOrReplacePostsAndComments.sql"), "UTF-8"));

			for(FBComment c : commentList){
				postIdSet.add(c.getPostId());
			}

			for(String postId : postIdSet){

				FBPost post = commentDao.getFBPostById(postId);

				writer.write("INSERT OR REPLACE INTO FBPost VALUES (\'" + post.getId() + "\', " + post.getCommentsCount() + ", \'" + df.format(post.getCreatedTime())
						+ "\', \'" + post.getFromId() + "\', " + post.getLikesCount() + ", "
						+ (post.getMessage() != null ? "\'" + post.getMessage().replace("'", "''") + "\'" : "NULL") + ", " + post.getSharesCount()
						+ ", \'" + post.getType() + "\');\n");
			}

			for(FBComment c : commentList){
				writer.write("INSERT OR REPLACE INTO FBComment VALUES (\'" + c.getId() + "\', \'" + c.getPostId() + "\', \'" + df.format(c.getCreatedTime())
						+ "\', " + c.getCommentCount() + ", \'" + c.getFromId() + "\', " + c.getLikeCount() + ", \'" + c.getMessage().replace("'", "''")
						+ "\', " + (c.getParentId() != null ? "\'" + c.getParentId() + "\'" : "NULL") + ", " + c.getResult() + ");\n" );
			}

			writer.flush();

			writer.close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
