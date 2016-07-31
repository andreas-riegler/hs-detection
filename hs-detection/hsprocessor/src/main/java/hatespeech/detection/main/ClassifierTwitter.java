package hatespeech.detection.main;

import hatespeech.detection.dao.JDBCTwitterDAO;
import hatespeech.detection.ml.WekaBowClassifier;
import hatespeech.detection.ml.WekaBowClassifier.TokenizerType;
import hatespeech.detection.model.IPosting;
import hatespeech.detection.model.Tweet;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.functions.SMO;

public class ClassifierTwitter {

	public static void main(String[] args) {
		JDBCTwitterDAO daoTW= new JDBCTwitterDAO();
		

		List<IPosting> trainingSamples = new ArrayList<IPosting>();
		List<IPosting> testSamples = new ArrayList<IPosting>();
		
		daoTW.getClassifiedTweets().stream()
		.forEach(c -> trainingSamples.add(c));

		daoTW.getUnclassifiedTweetsRange("751746857597034496", "751747983570526208").stream()
		.forEach(c -> testSamples.add(c));

		WekaBowClassifier classifier1 = new WekaBowClassifier(trainingSamples, new SMO());
		/*
		
		for(int i=0;i<43;i++)
		{
		
		classifier1.setRunName("Twitter Feature "+i);
		
		classifier1.setUseMessage(i==0?true:false);
		classifier1.setMessageApplyStringToWordFilter(true);
		
		classifier1.setUseTypedDependencies(i==1?true:false);
		classifier1.setTypedDependenciesApplyStringToWordFilter(true);
		
		classifier1.setUseSpellChecker(i==3?true:false);
		
		classifier1.setUseLIWC(i==4?true:false);
		
		classifier1.setUseIsReply(i==5?true:false);
		classifier1.setUseRetweetCount(i==6?true:false);
		classifier1.setUseIsRetweet(i==7?true:false);
		classifier1.setUseNumberOfHashtags(i==8?true:false);
		classifier1.setUseNumberOfFriends(i==9?true:false);
		classifier1.setUseNumberOfFollower(i==10?true:false);
		classifier1.setUseListedCount(i==11?true:false);
		classifier1.setUseNumberOfTweets(i==12?true:false);
		classifier1.setUseLengthOfUsername(i==13?true:false);
		classifier1.setUseLengthOfName(i==14?true:false);
		classifier1.setUseNumberOfWordsInName(i==15?true:false);
		
		classifier1.setUseLengthInTokens(i==16?true:false);
		classifier1.setUseAvgLengthOfWord(i==17?true:false);
		classifier1.setUseNumberOfSentences(i==18?true:false);
		classifier1.setUseAvgSentenceLength(i==19?true:false);
		classifier1.setUseNumberOfCharacters(i==20?true:false);
		classifier1.setUseNumberOfPunctuation(i==21?true:false);
		classifier1.setUseNumberOfSpecialPunctuation(i==22?true:false);
		classifier1.setUseNumberOfOneLetterTokens(i==23?true:false);
		classifier1.setUseNumberOfCapitalizedLetters(i==24?true:false);
		classifier1.setUseNumberOfURLs(i==25?true:false);
		classifier1.setUseNumberOfNonAlphaCharInMiddleOfWord(i==26?true:false);
		
		classifier1.setUseNumberOfDiscourseConnectives(i==27?true:false);
		classifier1.setUseNumberOfHatefulTerms(i==28?true:false);
		classifier1.setUseDensityOfHatefulTerms(i==29?true:false);
		classifier1.setUseNumberOfDiscourseParticels(i==30?true:false);
		classifier1.setUseNumberOfModalVerbs(i==31?true:false);
		classifier1.setUseNumberOfFirstPersonPronouns(i==32?true:false);
		classifier1.setUseNumberOfSecondPersonPronouns(i==33?true:false);
		classifier1.setUseNumberOfThirdPersonPronouns(i==34?true:false);
		classifier1.setUseNumberOfDemonstrativPronouns(i==35?true:false);
		classifier1.setUseNumberOfInfinitivPronouns(i==36?true:false);
		classifier1.setUseNumberOfInterrogativPronouns(i==37?true:false);
		classifier1.setUseNumberOfHappyEmoticons(i==38?true:false);
		classifier1.setUseNumberOfSadEmoticons(i==39?true:false);
		classifier1.setUseNumberOfCheekyEmoticons(i==40?true:false);
		classifier1.setUseNumberOfAmazedEmoticons(i==41?true:false);
		
		classifier1.setUseCommentEmbedding(i==42?true:false);
		
		classifier1.setUseAttributeSelectionFilter(false);
		
		classifier1.setMessageNGramMaxSize(2);
		//classifier1.setMessageTokenizerType(TokenizerType.HATEFUL_TERMS_NGRAM);
		classifier1.setTypedDependenciesNGramMaxSize(2);
		//classifier1.setTypedDependenciesTokenizerType(TokenizerType.HATEFUL_TERMS_NGRAM);
		
		classifier1.evaluate();
		}
		*/
		classifier1.setRunName("Twitter (all Features)");
		
		classifier1.setUseMessage(true);
		classifier1.setMessageApplyStringToWordFilter(true);
		
		classifier1.setUseTypedDependencies(true);
		classifier1.setTypedDependenciesApplyStringToWordFilter(true);
		
		classifier1.setUseSpellChecker(true);
		
		classifier1.setUseLIWC(true);
		
		classifier1.setUseFavouriteCount(true);
		classifier1.setUseIsReply(true);
		classifier1.setUseRetweetCount(true);
		classifier1.setUseNumberOfMentionedUser(true);
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
		
		classifier1.setUseCommentEmbedding(true);
			
		classifier1.evaluate();
		
		classifier1.learn();

		for(IPosting posting: testSamples)
		{
			double classification=classifier1.classify(posting);
			
			if(classification>0)
			{
				System.out.println(((Tweet)posting).getTweetid()+" "+posting.getMessage());
			}
		}
		//WekaBowClassifier classifier2 = new WekaBowClassifier(trainingSamples, new SMO());
		//classifier2.setMessageExactMatch(false);
		//classifier2.evaluate();

		//classifier1.learn();

		//classifier1.findFalsePositives(5);
		
		
	}

}
