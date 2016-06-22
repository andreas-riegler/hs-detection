package hatespeech.detection.main;

import hatespeech.detection.dao.JDBCTwitterDAO;
import hatespeech.detection.ml.WekaBowClassifier;
import hatespeech.detection.model.IPosting;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.functions.SMO;

public class ClassifierTwitter {

	public static void main(String[] args) {
		JDBCTwitterDAO daoTW= new JDBCTwitterDAO();
		

		List<IPosting> trainingSamples = new ArrayList<IPosting>();

		daoTW.getClassifiedTweets().stream()
		.forEach(c -> trainingSamples.add(c));

		WekaBowClassifier classifier1 = new WekaBowClassifier(trainingSamples, new SMO());
		classifier1.setRunName("Twitter (all Features)");
		
		classifier1.setUseIsReply(true);
		classifier1.setUseRetweetCount(true);
		classifier1.setUseIsRetweet(true);
		classifier1.setUseNumberOfHashtags(true);
		classifier1.setUseNumberOfFriends(true);
		classifier1.setUseNumberOfFollower(true);
		classifier1.setUseListedCount(true);
		classifier1.setUseNumberOfTweets(true);
		classifier1.setUseLengthOfUsername(true);
		classifier1.setUseLengthOfName(true);
		classifier1.setUseNumberOfWordsInName(true);
		
		classifier1.setUseLengthInTokens(true);
		classifier1.setUseAvgLengthOfWord(true);
		classifier1.setUseNumberOfSentences(true);
		classifier1.setUseAvgSentenceLength(true);
		classifier1.setUseNumberOfCharacters(true);
		classifier1.setUseNumberOfPunctuation(true);
		classifier1.setUseNumberOfSpecialPunctuation(true);
		classifier1.setUseNumberOfOneLetterTokens(true);
		classifier1.setUseNumberOfCapitalizedLetters(true);
		classifier1.setUseNumberOfURLs(true);
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
			
		classifier1.evaluate();

		//WekaBowClassifier classifier2 = new WekaBowClassifier(trainingSamples, new SMO());
		//classifier2.setMessageExactMatch(false);
		//classifier2.evaluate();

		//classifier1.learn();

		//classifier1.findFalsePositives(5);
		
		
	}

}
