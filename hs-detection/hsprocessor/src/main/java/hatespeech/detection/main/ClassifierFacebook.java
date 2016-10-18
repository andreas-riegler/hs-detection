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
import weka.classifiers.trees.RandomForest;

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

		WekaBowClassifier classifier1 = new WekaBowClassifier(trainingSamples, new RandomForest());
		classifier1.setRunName("with all features");
		
		//message features
		classifier1.setUseMessage(true);
		classifier1.setMessageApplyStringToWordFilter(true);
		classifier1.setMessageNGramMinSize(1);
		classifier1.setMessageNGramMaxSize(1);
		classifier1.setMessageFilterUnigramsToo(false);
		classifier1.setMessageTokenizerType(TokenizerType.NGRAM);
		
		//typed dependencies features
		classifier1.setUseTypedDependencies(true);
		classifier1.setTypedDependenciesApplyStringToWordFilter(true);
		classifier1.setTypedDependenciesNGramMinSize(1);
		classifier1.setTypedDependenciesNGramMaxSize(1);
		classifier1.setTypedDependenciesFilterUnigramsToo(false);
		classifier1.setTypedDependenciesTokenizerType(TokenizerType.NGRAM);
		classifier1.setUseTypedDependenciesTypeWhitelist(false);
		
		//spellchecker feature
		classifier1.setUseSpellChecker(true);
		
		//liwc features
		classifier1.setUseLIWC(true);
		
		//facebook features
		classifier1.setUseFBPostReactionType(true);
		classifier1.setUseFBCommentCount(true);
		classifier1.setUseFBLikeCount(true);
		classifier1.setUseFBFractionOfUserReactionOnTotalReactions(true);
		
		//linguistic features
		classifier1.setUseLengthInTokens(true);
		classifier1.setUseAvgLengthOfWord(true);
		classifier1.setUseNumberOfSentences(true);
		classifier1.setUseAvgSentenceLength(true);
		classifier1.setUseNumberOfCharacters(true);
		classifier1.setUseNumberOfHashtags(false); //not relevant
		classifier1.setUseNumberOfPunctuation(true);
		classifier1.setUseNumberOfSpecialPunctuation(true);
		classifier1.setUseNumberOfOneLetterTokens(true);
		classifier1.setUseNumberOfCapitalizedLetters(true);
		classifier1.setUseNumberOfURLs(false); //not relevant
		classifier1.setUseNumberOfNonAlphaCharInMiddleOfWord(true);
		
		//lexical features
		classifier1.setUseNumberOfDiscourseConnectives(true);
		classifier1.setUseNumberOfHatefulTerms(true);
		classifier1.setUseNumberOfHatefulTermsInApostrophe(true);
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
		classifier1.setUseNumberOfAngryEmoticons(true);
		
		//comment embeddings features
		classifier1.setUseCommentEmbedding(false);
		
		//character ngram features
		classifier1.setUseCharacterNGram(false);
		classifier1.setCharacterNGramMinSize(2);
		classifier1.setCharacterNGramMaxSize(4);
		
		//filters
		classifier1.setUseAttributeSelectionFilter(false);
		
			
		classifier1.evaluate();
		//classifier1.learn();
		classifier1.saveInstancesToArff();
		
		//daoFB.getRandomUnclassifiedTextFBCommentsByCount(100).forEach(c -> System.out.println(c.getMessage() + " : " + classifier1.classify(c) + "\n"));
		
		//WekaBowClassifier classifier2 = new WekaBowClassifier(trainingSamples, new SMO());
		//classifier2.setMessageExactMatch(false);
		//classifier2.evaluate();

		//classifier1.learn();

		//classifier1.findFalsePositives(5);
	}
}
