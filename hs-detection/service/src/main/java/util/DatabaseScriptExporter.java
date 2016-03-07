package util;

import hatespeech.detection.model.FBComment;
import hatespeech.detection.service.DatabaseConnector;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.restfb.util.CachedDateFormatStrategy;

public class DatabaseScriptExporter {
	
	private static CachedDateFormatStrategy cdfs = new CachedDateFormatStrategy();
	private static DateFormat df = cdfs.formatFor("dd.MM.yyyy HH:mm:ss");

	public static void main(String[] args) {
		exportUpdateHP();
	}
	
	public static void exportUpdateNP(){

		List<FBComment> commentList = new ArrayList<FBComment>();
		
		String sql = "select * from FBComment where result = 0";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) 
			{
				commentList.add(new FBComment(rs.getString("id"), rs.getString("postId"), df.parse(rs.getString("createdTime")), rs.getLong("commentCount"),
						rs.getString("fromId"), rs.getLong("likeCount"), rs.getString("message"), rs.getString("parentId"), rs.getInt("result")));
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
	
	public static void exportUpdateHP(){

		List<FBComment> commentList = new ArrayList<FBComment>();
		
		String sql = "select * from FBComment where result = 1";

		try {
			PreparedStatement ps = DatabaseConnector.getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) 
			{
				commentList.add(new FBComment(rs.getString("id"), rs.getString("postId"), df.parse(rs.getString("createdTime")), rs.getLong("commentCount"),
						rs.getString("fromId"), rs.getLong("likeCount"), rs.getString("message"), rs.getString("parentId"), rs.getInt("result")));
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
	
}
