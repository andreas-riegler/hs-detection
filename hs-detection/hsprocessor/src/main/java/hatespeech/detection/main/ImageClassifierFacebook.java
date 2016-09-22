package hatespeech.detection.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.EdgeHistogram;
import weka.classifiers.functions.SMO;
import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.ml.WekaImageClassifier;
import hatespeech.detection.model.IImagePosting;

public class ImageClassifierFacebook {


	public static void main(String[] args) {
		JDBCFBCommentDAO daoFB= new JDBCFBCommentDAO();

		List<IImagePosting> trainingSamples = new ArrayList<>();

		daoFB.getClassifiedImages().stream()
		.forEach(trainingSamples::add);

		WekaImageClassifier classifier = new WekaImageClassifier(trainingSamples, new SMO());
		
		classifier.setUseSurfFeatureVector(true);
		
		classifier.setUseGlobalFeatureVectors(true);
		classifier.setGlobalFeaturesList(Arrays.asList(new CEDD(), new ColorLayout(), new EdgeHistogram()));
		
		classifier.setUseFBCommentCount(true);
		classifier.setUseFBFractionOfUserReactionOnTotalReactions(true);
		classifier.setUseFBLikeCount(true);
		classifier.setUseFBPostReactionType(true);
		
		classifier.evaluate();
		classifier.learn();
		classifier.saveInstancesToArff();
	}
}
