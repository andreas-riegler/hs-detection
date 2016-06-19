package hatespeech.detection.main;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.ml.WekaBowClassifier;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.HatePost;
import hatespeech.detection.model.IPosting;
import hatespeech.detection.model.PostType;
import hatespeech.detection.model.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import weka.classifiers.functions.SMO;

public class ClassifierFacebook {
	
	public static void main(String[] args) {
		JDBCFBCommentDAO daoFB= new JDBCFBCommentDAO();
		JDBCHSPostDAO daoHP= new JDBCHSPostDAO();

		List<IPosting> trainingSamples = new ArrayList<IPosting>();

		daoFB.getClassifiedFBComments().stream()
		.filter(c -> c.getAttachmentMediaImageSrc() == null)
		.forEach(c -> trainingSamples.add(c));
		
		daoHP.getAllPosts().stream()
		.filter(c -> c.getResult() != -1)
		.forEach(c -> trainingSamples.add(c));

		WekaBowClassifier classifier1 = new WekaBowClassifier(trainingSamples, new SMO());
		classifier1.setRunName("with FBReaction, CommentCount, LikeCount");
		classifier1.setUseFBPostReactionType(true);
		classifier1.setUseFBCommentCount(true);
		classifier1.setUseFBLikeCount(true);
		classifier1.setUseLengthInTokens(true);
		classifier1.setUseAvgLengthOfWord(true);
		classifier1.setUseNumberOfSentences(true);
		classifier1.setUseAvgSentenceLength(true);
		classifier1.setUseNumberOfCharacters(true);
		classifier1.setUseNumberOfHashtags(false);
		classifier1.setUseNumberOfPunctuation(true);
		classifier1.setUseNumberOfSpecialPunctuation(true);
		classifier1.setUseNumberOfOneLetterTokens(true);
		classifier1.setUseNumberOfCapitalizedLetters(true);
		classifier1.setUseNumberOfURLs(false);
		classifier1.setUseNumberOfNonAlphaCharInMiddleOfWord(true);
		
		classifier1.evaluate();

		//WekaBowClassifier classifier2 = new WekaBowClassifier(trainingSamples, new SMO());
		//classifier2.setMessageExactMatch(false);
		//classifier2.evaluate();

		//classifier1.learn();

		//classifier1.findFalsePositives(5);
	}
}
