package hatespeech.detection.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.FBComment;

public class CaffeUtils {

	private static final String CAFFE_INPUT_PATH = "../caffe/input_images/";
	private static final String CAFFE_OUTPUT_PATH = "../caffe/output_images/";
	private static final String CAFFE_INPUT_PATH_CLASSIFY = "../caffe/input_images_classify/";
	private static final int CAFEE_RESULT_TO_CLASSIFY = -5;

	public static void main(String[] args) {
		exportCaffeImages();
		//importCaffePredictions();
//		exportCaffeImagesAndSetResult(500, -5);
//		test();
	}
	
	private static void test(){
		JDBCFBCommentDAO commentDao = new JDBCFBCommentDAO();
		
		List<FBComment> commentList = commentDao.getFBCommentsByResult(-5);
		System.out.println(commentList.size());
	}

	private static void exportCaffeImages() {
		JDBCFBCommentDAO commentDao = new JDBCFBCommentDAO();
		
		List<FBComment> commentList = commentDao.getClassifiedImages();
		System.out.println("new size: " + commentList.size());
		commentList.addAll(commentDao.getClassifiedImagesForTrendAnalysis1());
		System.out.println("new size: " + commentList.size());
		commentList.addAll(commentDao.getClassifiedImagesForTrendAnalysis2());
		System.out.println("new size: " + commentList.size());
		commentList.addAll(commentDao.getClassifiedImagesForTrendAnalysis3());
		System.out.println("new size: " + commentList.size());
		
		commentList.forEach(c -> {
			try {
				Files.copy(Paths.get(c.getAttachmentMediaImageSrc()), Paths.get(CAFFE_INPUT_PATH + c.getId()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
	}

	private static void exportCaffeImagesAndSetResult(int count, int setResult) {
		JDBCFBCommentDAO commentDao = new JDBCFBCommentDAO();
		
		List<FBComment> commentList = commentDao.getShuffledUnclassifiedImageFBCommentsByCount(count);
		
		commentList.forEach(c -> {
			try {
				Files.copy(Paths.get("/home/andreas/repos/hs-detection/hs-detection/images/images/" + c.getAttachmentMediaImageSrc().substring(10)), Paths.get(CAFFE_INPUT_PATH_CLASSIFY + c.getId()));
				commentDao.updateResult(c.getId(), CAFEE_RESULT_TO_CLASSIFY);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
	}

	//TODO: einfügen über TypedDependencies Methode (in Spalte)
	private static void importCaffePredictions() {
		JDBCFBCommentDAO commentDao = new JDBCFBCommentDAO();

		try {
			Files.walk(Paths.get(CAFFE_OUTPUT_PATH)).forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath.toString()), "UTF-8"));
											
						StringBuilder sb = new StringBuilder("");

						reader.readLine();
						for (String line = reader.readLine(); line != null; line = reader.readLine()) {
							String [] splitLine = line.split(" ");

							sb.append(splitLine[2].replaceFirst("\"", "") + " " + splitLine[0] +";");							
						}
						
						if(sb.length() > 0){
							sb.deleteCharAt(sb.length()-1);
						}			
						System.out.println(sb.toString());
						
//						commentDao.updateFBCommentSetTypedDependenciesById(filePath.getFileName().toString(), sb.toString());
						reader.close();		
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
