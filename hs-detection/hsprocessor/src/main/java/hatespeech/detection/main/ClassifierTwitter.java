package hatespeech.detection.main;

import hatespeech.detection.dao.JDBCTwitterDAO;
import hatespeech.detection.ml.WekaBowClassifier;
import hatespeech.detection.ml.WekaBowClassifier.TokenizerType;
import hatespeech.detection.model.IPosting;
import hatespeech.detection.model.PostType;
import hatespeech.detection.model.Posting;
import hatespeech.detection.model.Tweet;
import hatespeech.detection.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;

public class ClassifierTwitter {

	public static void main(String[] args) {
		
		long start=System.currentTimeMillis();
		JDBCTwitterDAO daoTW= new JDBCTwitterDAO();
		
		//Afd - Pegida Official - Gipsy105 - Einzelfall -Welt in Chaos - Hab die Nase Voll-German Observer-Hansson -Einzelfallbearbeiter-Aufbruch-HC Strache - schnauzesovoll-germandefenceleague-uwe becher - merkel muss weg- deutschland wehrt sich-lupus lotarius-end of days-mut zur wahrheit
		Long userids[] = {844081278L, 3130731489L, 3728419043L,4816230227L,4558206579L,4763025382L,3402505065L,156912564L,1590434754L,2970248351L,117052823L,1108250934L,712318748590473216L,1227192296L,701822420743749636L,3654016996L,2287293282L,4719457457L,4497623716L};
				
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
		
		classifier1.setRunName("Twitter (all - svm)");
		
		classifier1.setUseMessage(false);
		classifier1.setMessageNGramMinSize(1);
		classifier1.setMessageNGramMaxSize(2);
		classifier1.setMessageApplyStringToWordFilter(true);
		
		classifier1.setUseTypedDependencies(false);
		classifier1.setTypedDependenciesApplyStringToWordFilter(true);
		
		classifier1.setCharacterNGramMinSize(3);
		classifier1.setCharacterNGramMaxSize(4);
		classifier1.setUseCharacterNGram(false);
		
		classifier1.setUseSpellChecker(false);
		
		classifier1.setUseLIWC(false);
		
		classifier1.setUseFavouriteCount(false);
		classifier1.setUseIsReply(false);
		classifier1.setUseRetweetCount(false);
		classifier1.setUseNumberOfMentionedUser(false);
		classifier1.setUseIsRetweet(false);
		classifier1.setUseNumberOfHashtags(false);
		classifier1.setUseNumberOfFriends(false);
		classifier1.setUseNumberOfFollower(false);
		classifier1.setUseListedCount(false);
		classifier1.setUseNumberOfTweets(false);
		classifier1.setUseLengthOfUsername(false);
		classifier1.setUseLengthOfName(false);
		classifier1.setUseNumberOfWordsInName(false);
		
		classifier1.setUseLengthInTokens(false);
		classifier1.setUseAvgLengthOfWord(false);
		classifier1.setUseNumberOfSentences(false);
		classifier1.setUseAvgSentenceLength(false);
		classifier1.setUseNumberOfCharacters(false);
		classifier1.setUseNumberOfPunctuation(false);
		classifier1.setUseNumberOfSpecialPunctuation(false);
		classifier1.setUseNumberOfOneLetterTokens(false);
		classifier1.setUseNumberOfCapitalizedLetters(false);
		classifier1.setUseNumberOfURLs(false);
		classifier1.setUseNumberOfNonAlphaCharInMiddleOfWord(false);
		
		classifier1.setUseNumberOfDiscourseConnectives(false);
		classifier1.setUseNumberOfHatefulTerms(false);
		classifier1.setUseNumberOfHatefulTermsInApostrophe(false);
		classifier1.setUseDensityOfHatefulTerms(false);
		classifier1.setUseNumberOfDiscourseParticels(false);
		classifier1.setUseNumberOfModalVerbs(false);
		classifier1.setUseNumberOfFirstPersonPronouns(false);
		classifier1.setUseNumberOfSecondPersonPronouns(false);
		classifier1.setUseNumberOfThirdPersonPronouns(false);
		classifier1.setUseNumberOfDemonstrativPronouns(false);
		classifier1.setUseNumberOfInfinitivPronouns(false);
		classifier1.setUseNumberOfInterrogativPronouns(false);
		classifier1.setUseNumberOfHappyEmoticons(false);
		classifier1.setUseNumberOfSadEmoticons(false);
		classifier1.setUseNumberOfCheekyEmoticons(false);
		classifier1.setUseNumberOfAmazedEmoticons(false);
		classifier1.setUseNumberOfAngryEmoticons(false);
		
		classifier1.setUseCommentEmbedding(true);
		
		classifier1.setSpecificFollowedUsers(userids);
		classifier1.setUseNetworkFollowerFeature(false);
		
		classifier1.setUseAttributeSelectionFilter(false);
		
		
		
		classifier1.evaluate();
		classifier1.saveInstancesToArff();
		/*
		classifier1.learn();

		testSamples.add((IPosting) new Tweet(1L,new User(),"Diese Musels gehören erschlagen und ausgewiesen!!",0,0,false,false,null,new HashSet<User>(),1));
		for(IPosting posting: testSamples)
		{
			double classification=classifier1.classify(posting);
			
			if(classification>0)
			{
				System.out.println(((Tweet)posting).getTweetid()+" "+posting.getMessage());
			}
		}
		
		
		*/
		//WekaBowClassifier classifier2 = new WekaBowClassifier(trainingSamples, new SMO());
		//classifier2.setMessageExactMatch(false);
		//classifier2.evaluate();

		//classifier1.learn();

		//classifier1.findFalsePositives(5);
		
		System.out.println(System.currentTimeMillis()-start);
	}

}
