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

	public static void main(String[] args) {
		exportCaffeImages();
		//importCaffePredictions();
	}

	private static void exportCaffeImages() {
		JDBCFBCommentDAO commentDao = new JDBCFBCommentDAO();
		
		List<FBComment> commentList = commentDao.getClassifiedImages();
		
		commentList.forEach(c -> {
			try {
				Files.copy(Paths.get(c.getAttachmentMediaImageSrc()), Paths.get(CAFFE_INPUT_PATH + c.getId()));
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
