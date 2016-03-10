package hatespeech.detection.util;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.HatePost;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class ParZuInputExporter {

	public static void main(String[] args) {
		exportParZuScript();
	}

	public static void exportParZuScript(){

		JDBCFBCommentDAO commentDao = new JDBCFBCommentDAO();
		JDBCHSPostDAO hsPostDao = new JDBCHSPostDAO();
		
		List<FBComment> commentList = commentDao.getClassifiedFBComments();
		List<HatePost> hatePostList = hsPostDao.getAllPosts();

		BufferedWriter writer;

		try {			
			for(FBComment c : commentList){
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("../parzu/input/fbcomment/" + c.getId()), "UTF-8"));
				String [] splitSentences = c.getMessage().split("[.?!\\n]");

				for(int i = 0; i < splitSentences.length; i++){
					if(!splitSentences[i].isEmpty()){
						writer.write(splitSentences[i].replaceAll("[^0-9a-zA-ZäÄöÖüÜß,\\s]", "").trim() + (i < splitSentences.length - 1 ? "\n" : ""));
					}
				}

				writer.flush();
				writer.close();
			}
			
			for(HatePost hp : hatePostList){
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("../parzu/input/hatepost/" + hp.getId()), "UTF-8"));
				String [] splitSentences = hp.getPost().split("[.?!\\n]");

				for(int i = 0; i < splitSentences.length; i++){
					if(!splitSentences[i].isEmpty()){
						writer.write(splitSentences[i].replaceAll("[^0-9a-zA-ZäÄöÖüÜß,\\s]", "").trim() + (i < splitSentences.length - 1 ? "\n" : ""));
					}
				}

				writer.flush();
				writer.close();
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}


	}

}
