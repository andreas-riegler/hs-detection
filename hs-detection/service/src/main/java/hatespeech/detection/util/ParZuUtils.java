package hatespeech.detection.util;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.HatePost;




import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;import java.util.Map;


public class ParZuUtils {

	private static final String PARZU_INPUT_FBCOMMENT_PATH = "../parzu/output/fbcomment/";
	private static final String PARZU_INPUT_HATEPOST_PATH = "../parzu/output/hatepost/";
	private static final String PARZU_OUTPUT_FBCOMMENT_PATH = "../parzu/output/fbcomment/";
	private static final String PARZU_OUTPUT_HATEPOST_PATH = "../parzu/output/hatepost/";

	public static void main(String[] args) {
		//exportParZuScript();
		importParZuTypedDependencies();
	}

	public static void importParZuTypedDependencies(){
		JDBCFBCommentDAO commentDao = new JDBCFBCommentDAO();
		JDBCHSPostDAO hsPostDao = new JDBCHSPostDAO();

		try {
			Files.walk(Paths.get(PARZU_OUTPUT_FBCOMMENT_PATH)).forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(PARZU_OUTPUT_FBCOMMENT_PATH + filePath.getFileName().toString()), "UTF-8"));

						Map<Integer, String> wordMap = new HashMap<Integer, String>();
						List<String> tempLines = new ArrayList<String>();

						for (String line = reader.readLine(); line != null; line = reader.readLine()) {
							String [] splitLine = line.split("\\t");

							if(!line.isEmpty()){
								tempLines.add(line);
								wordMap.put(Integer.parseInt(splitLine[0]), splitLine[2]);
							}
							else{					
								for(String tempLine : tempLines){
									String [] tempSplitLine = tempLine.split("\\t");

									String firstWord = wordMap.get(Integer.parseInt(tempSplitLine[6]));

									System.out.print("" + tempSplitLine[7] + "(" + (firstWord != null ? firstWord : "ROOT") + "," + tempSplitLine[2] + ") ");
								}
								System.out.println();

								tempLines.clear();
								wordMap.clear();
							}
						}
						System.out.println();
						reader.close();

					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			});
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
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
