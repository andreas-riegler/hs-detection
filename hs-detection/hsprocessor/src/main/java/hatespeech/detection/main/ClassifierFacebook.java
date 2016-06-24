package hatespeech.detection.main;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.ml.WekaBowClassifier;
import hatespeech.detection.ml.WekaBowClassifier.TokenizerType;
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
		classifier1.setRunName("with all features");
		
		classifier1.setUseMessage(true);
		classifier1.setMessageApplyStringToWordFilter(true);
		
		classifier1.setUseTypedDependencies(true);
		classifier1.setTypedDependenciesApplyStringToWordFilter(true);
		
		classifier1.setUseSpellChecker(false);
		
		classifier1.setUseLIWC(true);
		
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
		
		classifier1.setUseNumberOfDiscourseConnectives(true);
		classifier1.setUseNumberOfHatefulTerms(true);
		classifier1.setUseDensityOfHatefulTerms(true);
		classifier1.setUseNumberOfDiscourseParticels(true);
		classifier1.setUseNumberOfModalVerbs(true);
		classifier1.setUseNumberOfFirstPersonPronouns(true);
		classifier1.setUseNumberOfSecondPersonPronouns(true);
		classifier1.setUseNumberOfThirdPersonPronouns(true);
		classifier1.setUseNumberOfDemonstrativPronouns(true);
		classifier1.setUseNumberOfInfinitivPronouns(true);
		classifier1.setUseNumberOfInterrogativPronouns(true);
		classifier1.setUseNumberOfHappyEmoticons(true);
		classifier1.setUseNumberOfSadEmoticons(true);
		classifier1.setUseNumberOfCheekyEmoticons(true);
		classifier1.setUseNumberOfAmazedEmoticons(true);
		
		classifier1.setUseAttributeSelectionFilter(false);
		
		classifier1.setMessageNGramMaxSize(2);
		classifier1.setMessageTokenizerType(TokenizerType.HATEFUL_TERMS_NGRAM);
		classifier1.setTypedDependenciesNGramMaxSize(2);
		classifier1.setTypedDependenciesTokenizerType(TokenizerType.HATEFUL_TERMS_NGRAM);
		
		classifier1.setUseCommentEmbedding(true);
			
		classifier1.evaluate();
		//classifier1.learn();
		//classifier1.saveInstancesToArff();
 
		//WekaBowClassifier classifier2 = new WekaBowClassifier(trainingSamples, new SMO());
		//classifier2.setMessageExactMatch(false);
		//classifier2.evaluate();

		//classifier1.learn();

		//classifier1.findFalsePositives(5);
	}
}
