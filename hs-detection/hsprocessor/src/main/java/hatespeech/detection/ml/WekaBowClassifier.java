package hatespeech.detection.ml;

import hatespeech.detection.hsprocessor.FeatureExtractor;
import hatespeech.detection.hsprocessor.FeatureExtractor.TypedDependencyWordType;
import hatespeech.detection.model.Category;
import hatespeech.detection.model.CategoryScore;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.IPosting;
import hatespeech.detection.model.PostType;
import hatespeech.detection.model.Tweet;
import hatespeech.detection.paragraphvector.ParagraphToVector;
import hatespeech.detection.tokenizer.RetainHatefulTermsNGramTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.RemoveMisclassified;

public class WekaBowClassifier {

	public enum TokenizerType {
		NGRAM, HATEFUL_TERMS_NGRAM
	}

	private static final Logger logger = LoggerFactory.getLogger(WekaBowClassifier.class);

	private static double WEKA_MISSING_VALUE = Utils.missingValue();

	private String runName = "Default Name";

	private List<IPosting> trainingSamples;
	private Instances trainingInstances = null, trainingInstances_FP;
	private ArrayList<Attribute> featureList = null;
	private StringToWordVector sTWfilter;
	private AttributeSelection attributeFilter;
	private Classifier classifier;
	private FilteredClassifier filteredClassifier;

	private String[] categoryBlacklist = {"Pronoun","I","We","You","Self","Other","Article","Preps","Past","Present","Future"};
	private Set<String> categoryBlacklistSet = new HashSet<String>(Arrays.asList(categoryBlacklist));
	private String[] categoryWhitelist = {"Assent","Affect","Swear","Death","Relig","Space","Home","Discrepancy","Sad","Anger","Anxiety","Negative_emotion","Positive_feeling","Positive_emotion","Social"};
	private Set<String> categoryWhitelistSet = new HashSet<String>(Arrays.asList(categoryWhitelist));

	//Typed Dependencies StringToWordVector filter settings
	private boolean useTypedDependencies = true;
	private TokenizerType typedDependenciesTokenizerType = TokenizerType.NGRAM;
	private int typedDependenciesNGramMinSize = 1;
	private int typedDependenciesNGramMaxSize = 1;
	private boolean typedDependenciesFilterUnigramsToo = false;
	private boolean typedDependenciesExactMatch = true;

	//Message StringToWordVector filter settings
	private TokenizerType messageTokenizerType = TokenizerType.NGRAM;
	private int messageNGramMinSize = 1;
	private int messageNGramMaxSize = 1;
	private boolean messageFilterUnigramsToo = false;
	private boolean messageExactMatch = true;

	//RemoveMisclassified filter settings
	private boolean useRemoveMisclassifiedFilter = false;
	private int removeMisclassifiedFilterNumFolds = 4;
	private double removeMisclassifiedFilterThreshold = 0.5;
	private int removeMisclassifiedFilterMaxIterations = 1;

	//AttributeSelection filter settings
	private boolean useAttributeSelectionFilter = false;

	//SpellChecker settings
	private boolean useSpellChecker = false;

	//LIWC Settings
	private boolean useLIWC = false;

	//Facebook features settings
	private boolean useFBPostReactionType = false;
	private boolean useFBCommentCount = false;
	private boolean useFBLikeCount = false;

	//Twitter features settings
	private boolean useRetweetCount=false;
	private boolean useIsReply=false;
	private boolean useIsRetweet=false;
	private boolean useNumberOfHashtags = false;
	private boolean useNumberOfFriends = false;
	private boolean useNumberOfFollower=false;
	private boolean useListedCount=false;
	private boolean useNumberOfTweets=false;
	private boolean useLengthOfUsername =false;
	private boolean useLengthOfName =false;
	private boolean useNumberOfWordsInName=false;

	//Linguistic features settings
	private boolean useLengthInTokens = false;
	private boolean useAvgLengthOfWord = false;
	private boolean useNumberOfSentences = false;
	private boolean useAvgSentenceLength = false;
	private boolean useNumberOfCharacters = false;
	private boolean useNumberOfPunctuation = false;
	private boolean useNumberOfSpecialPunctuation = false;
	private boolean useNumberOfOneLetterTokens = false;
	private boolean useNumberOfCapitalizedLetters = false;
	private boolean useNumberOfURLs = false;
	private boolean useNumberOfNonAlphaCharInMiddleOfWord = false;

	//Lexical features settings
	private boolean useNumberOfDiscourseConnectives = false;
	private boolean useNumberOfHatefulTerms = false;
	private boolean useDensityOfHatefulTerms = false;
	private boolean useNumberOfDiscourseParticels = false;
	private boolean useNumberOfModalVerbs = false;
	private boolean useNumberOfFirstPersonPronouns = false;
	private boolean useNumberOfSecondPersonPronouns = false;
	private boolean useNumberOfThirdPersonPronouns = false;
	private boolean useNumberOfDemonstrativPronouns = false;
	private boolean useNumberOfInfinitivPronouns = false;
	private boolean useNumberOfInterrogativPronouns = false;
	private boolean useNumberOfHappyEmoticons = false;
	private boolean useNumberOfSadEmoticons = false;
	private boolean useNumberOfCheekyEmoticons = false;
	private boolean useNumberOfAmazedEmoticons = false;
	
	//ParagraphToVector settings
	private List<String>tweetMessagesList=new ArrayList<String>();
	private List<String>labelSourceList=new ArrayList<String>();
	private boolean useCommentEmbedding=false;
	private ParagraphToVector paraToVec=null;
	private ParagraphVectors messageVectors=null;

	public WekaBowClassifier(List<IPosting> trainingSamples,Classifier classifier){
		this.classifier=classifier;
		this.trainingSamples = trainingSamples;
	
	}
	


	public boolean isUseNumberOfDiscourseConnectives() {
		return useNumberOfDiscourseConnectives;
	}
	public void setUseNumberOfDiscourseConnectives(
			boolean useNumberOfDiscourseConnectives) {
		this.useNumberOfDiscourseConnectives = useNumberOfDiscourseConnectives;
	}
	public boolean isUseNumberOfHatefulTerms() {
		return useNumberOfHatefulTerms;
	}
	public void setUseNumberOfHatefulTerms(boolean useNumberOfHatefulTerms) {
		this.useNumberOfHatefulTerms = useNumberOfHatefulTerms;
	}
	public boolean isUseDensityOfHatefulTerms() {
		return useDensityOfHatefulTerms;
	}
	public void setUseDensityOfHatefulTerms(boolean useDensityOfHatefulTerms) {
		this.useDensityOfHatefulTerms = useDensityOfHatefulTerms;
	}
	public boolean isUseNumberOfDiscourseParticels() {
		return useNumberOfDiscourseParticels;
	}
	public void setUseNumberOfDiscourseParticels(
			boolean useNumberOfDiscourseParticels) {
		this.useNumberOfDiscourseParticels = useNumberOfDiscourseParticels;
	}
	public boolean isUseNumberOfModalVerbs() {
		return useNumberOfModalVerbs;
	}
	public void setUseNumberOfModalVerbs(boolean useNumberOfModalVerbs) {
		this.useNumberOfModalVerbs = useNumberOfModalVerbs;
	}
	public boolean isUseNumberOfFirstPersonPronouns() {
		return useNumberOfFirstPersonPronouns;
	}
	public void setUseNumberOfFirstPersonPronouns(
			boolean useNumberOfFirstPersonPronouns) {
		this.useNumberOfFirstPersonPronouns = useNumberOfFirstPersonPronouns;
	}
	public boolean isUseNumberOfSecondPersonPronouns() {
		return useNumberOfSecondPersonPronouns;
	}
	public void setUseNumberOfSecondPersonPronouns(
			boolean useNumberOfSecondPersonPronouns) {
		this.useNumberOfSecondPersonPronouns = useNumberOfSecondPersonPronouns;
	}
	public boolean isUseNumberOfThirdPersonPronouns() {
		return useNumberOfThirdPersonPronouns;
	}
	public void setUseNumberOfThirdPersonPronouns(
			boolean useNumberOfThirdPersonPronouns) {
		this.useNumberOfThirdPersonPronouns = useNumberOfThirdPersonPronouns;
	}
	public boolean isUseNumberOfDemonstrativPronouns() {
		return useNumberOfDemonstrativPronouns;
	}
	public void setUseNumberOfDemonstrativPronouns(
			boolean useNumberOfDemonstrativPronouns) {
		this.useNumberOfDemonstrativPronouns = useNumberOfDemonstrativPronouns;
	}
	public boolean isUseNumberOfInfinitivPronouns() {
		return useNumberOfInfinitivPronouns;
	}
	public void setUseNumberOfInfinitivPronouns(boolean useNumberOfInfinitivPronouns) {
		this.useNumberOfInfinitivPronouns = useNumberOfInfinitivPronouns;
	}
	public boolean isUseNumberOfInterrogativPronouns() {
		return useNumberOfInterrogativPronouns;
	}
	public void setUseNumberOfInterrogativPronouns(
			boolean useNumberOfInterrogativPronouns) {
		this.useNumberOfInterrogativPronouns = useNumberOfInterrogativPronouns;
	}
	public boolean isUseNumberOfHappyEmoticons() {
		return useNumberOfHappyEmoticons;
	}
	public void setUseNumberOfHappyEmoticons(boolean useNumberOfHappyEmoticons) {
		this.useNumberOfHappyEmoticons = useNumberOfHappyEmoticons;
	}
	public boolean isUseNumberOfSadEmoticons() {
		return useNumberOfSadEmoticons;
	}
	public void setUseNumberOfSadEmoticons(boolean useNumberOfSadEmoticons) {
		this.useNumberOfSadEmoticons = useNumberOfSadEmoticons;
	}
	public boolean isUseNumberOfCheekyEmoticons() {
		return useNumberOfCheekyEmoticons;
	}
	public void setUseNumberOfCheekyEmoticons(boolean useNumberOfCheekyEmoticons) {
		this.useNumberOfCheekyEmoticons = useNumberOfCheekyEmoticons;
	}
	public boolean isUseNumberOfAmazedEmoticons() {
		return useNumberOfAmazedEmoticons;
	}
	public void setUseNumberOfAmazedEmoticons(boolean useNumberOfAmazedEmoticons) {
		this.useNumberOfAmazedEmoticons = useNumberOfAmazedEmoticons;
	}
	public boolean isUseLengthInTokens() {
		return useLengthInTokens;
	}
	public void setUseLengthInTokens(boolean useLengthInTokens) {
		this.useLengthInTokens = useLengthInTokens;
	}
	public boolean isUseAvgLengthOfWord() {
		return useAvgLengthOfWord;
	}
	public void setUseAvgLengthOfWord(boolean useAvgLengthOfWord) {
		this.useAvgLengthOfWord = useAvgLengthOfWord;
	}
	public boolean isUseNumberOfSentences() {
		return useNumberOfSentences;
	}
	public void setUseNumberOfSentences(boolean useNumberOfSentences) {
		this.useNumberOfSentences = useNumberOfSentences;
	}
	public boolean isUseAvgSentenceLength() {
		return useAvgSentenceLength;
	}
	public void setUseAvgSentenceLength(boolean useAvgSentenceLength) {
		this.useAvgSentenceLength = useAvgSentenceLength;
	}
	public boolean isUseNumberOfCharacters() {
		return useNumberOfCharacters;
	}
	public void setUseNumberOfCharacters(boolean useNumberOfCharacters) {
		this.useNumberOfCharacters = useNumberOfCharacters;
	}
	public boolean isUseNumberOfHashtags() {
		return useNumberOfHashtags;
	}
	public void setUseNumberOfHashtags(boolean useNumberOfHashtags) {
		this.useNumberOfHashtags = useNumberOfHashtags;
	}
	public boolean isUseNumberOfPunctuation() {
		return useNumberOfPunctuation;
	}
	public void setUseNumberOfPunctuation(boolean useNumberOfPunctuation) {
		this.useNumberOfPunctuation = useNumberOfPunctuation;
	}
	public boolean isUseNumberOfSpecialPunctuation() {
		return useNumberOfSpecialPunctuation;
	}
	public void setUseNumberOfSpecialPunctuation(
			boolean useNumberOfSpecialPunctuation) {
		this.useNumberOfSpecialPunctuation = useNumberOfSpecialPunctuation;
	}
	public boolean isUseNumberOfOneLetterTokens() {
		return useNumberOfOneLetterTokens;
	}
	public void setUseNumberOfOneLetterTokens(boolean useNumberOfOneLetterTokens) {
		this.useNumberOfOneLetterTokens = useNumberOfOneLetterTokens;
	}
	public boolean isUseNumberOfCapitalizedLetters() {
		return useNumberOfCapitalizedLetters;
	}
	public void setUseNumberOfCapitalizedLetters(
			boolean useNumberOfCapitalizedLetters) {
		this.useNumberOfCapitalizedLetters = useNumberOfCapitalizedLetters;
	}
	public boolean isUseNumberOfURLs() {
		return useNumberOfURLs;
	}
	public void setUseNumberOfURLs(boolean useNumberOfURLs) {
		this.useNumberOfURLs = useNumberOfURLs;
	}
	public boolean isUseNumberOfNonAlphaCharInMiddleOfWord() {
		return useNumberOfNonAlphaCharInMiddleOfWord;
	}
	public void setUseNumberOfNonAlphaCharInMiddleOfWord(
			boolean useNumberOfNonAlphaCharInMiddleOfWord) {
		this.useNumberOfNonAlphaCharInMiddleOfWord = useNumberOfNonAlphaCharInMiddleOfWord;
	}
	public boolean isUseRetweetCount() {
		return useRetweetCount;
	}
	public void setUseRetweetCount(boolean useRetweetCount) {
		this.useRetweetCount = useRetweetCount;
	}
	public boolean isUseIsReply() {
		return useIsReply;
	}
	public void setUseIsReply(boolean useIsReply) {
		this.useIsReply = useIsReply;
	}
	public boolean isUseIsRetweet() {
		return useIsRetweet;
	}
	public void setUseIsRetweet(boolean useIsRetweet) {
		this.useIsRetweet = useIsRetweet;
	}
	public boolean isUseNumberOfFriends() {
		return useNumberOfFriends;
	}
	public void setUseNumberOfFriends(boolean useNumberOfFriends) {
		this.useNumberOfFriends = useNumberOfFriends;
	}
	public boolean isUseNumberOfFollower() {
		return useNumberOfFollower;
	}
	public void setUseNumberOfFollower(boolean useNumberOfFollower) {
		this.useNumberOfFollower = useNumberOfFollower;
	}
	public boolean isUseListedCount() {
		return useListedCount;
	}
	public void setUseListedCount(boolean useListedCount) {
		this.useListedCount = useListedCount;
	}
	public boolean isUseNumberOfTweets() {
		return useNumberOfTweets;
	}
	public void setUseNumberOfTweets(boolean useNumberOfTweets) {
		this.useNumberOfTweets = useNumberOfTweets;
	}
	public boolean isUseLengthOfUsername() {
		return useLengthOfUsername;
	}
	public void setUseLengthOfUsername(boolean useLengthOfUsername) {
		this.useLengthOfUsername = useLengthOfUsername;
	}
	public boolean isUseLengthOfName() {
		return useLengthOfName;
	}
	public void setUseLengthOfName(boolean useLengthOfName) {
		this.useLengthOfName = useLengthOfName;
	}
	public boolean isUseNumberOfWordsInName() {
		return useNumberOfWordsInName;
	}
	public void setUseNumberOfWordsInName(boolean useNumberOfWordsInName) {
		this.useNumberOfWordsInName = useNumberOfWordsInName;
	}
	public boolean isUseFBCommentCount() {
		return useFBCommentCount;
	}
	public void setUseFBCommentCount(boolean useFBCommentCount) {
		this.useFBCommentCount = useFBCommentCount;
	}
	public boolean isUseFBLikeCount() {
		return useFBLikeCount;
	}
	public void setUseFBLikeCount(boolean useFBLikeCount) {
		this.useFBLikeCount = useFBLikeCount;
	}
	public String getRunName() {
		return runName;
	}
	public void setRunName(String runName) {
		this.runName = runName;
	}
	public boolean isUseFBPostReactionType() {
		return useFBPostReactionType;
	}
	public void setUseFBPostReactionType(boolean useFBPostReactionType) {
		this.useFBPostReactionType = useFBPostReactionType;
	}
	public boolean isUseTypedDependencies() {
		return useTypedDependencies;
	}
	public void setUseTypedDependencies(boolean useTypedDependencies) {
		this.useTypedDependencies = useTypedDependencies;
	}
	public int getTypedDependenciesNGramMinSize() {
		return typedDependenciesNGramMinSize;
	}
	public void setTypedDependenciesNGramMinSize(int typedDependenciesNGramMinSize) {
		this.typedDependenciesNGramMinSize = typedDependenciesNGramMinSize;
	}
	public int getTypedDependenciesNGramMaxSize() {
		return typedDependenciesNGramMaxSize;
	}
	public void setTypedDependenciesNGramMaxSize(int typedDependenciesNGramMaxSize) {
		this.typedDependenciesNGramMaxSize = typedDependenciesNGramMaxSize;
	}
	public TokenizerType getMessageTokenizerType() {
		return messageTokenizerType;
	}
	public void setMessageTokenizerType(TokenizerType messageTokenizerType) {
		this.messageTokenizerType = messageTokenizerType;
	}
	public int getMessageNGramMinSize() {
		return messageNGramMinSize;
	}
	public void setMessageNGramMinSize(int messageNGramMinSize) {
		this.messageNGramMinSize = messageNGramMinSize;
	}
	public int getMessageNGramMaxSize() {
		return messageNGramMaxSize;
	}
	public void setMessageNGramMaxSize(int messageNGramMaxSize) {
		this.messageNGramMaxSize = messageNGramMaxSize;
	}
	public boolean isMessageFilterUnigramsToo() {
		return messageFilterUnigramsToo;
	}
	public void setMessageFilterUnigramsToo(boolean messageFilterUnigramsToo) {
		this.messageFilterUnigramsToo = messageFilterUnigramsToo;
	}
	public boolean isMessageExactMatch() {
		return messageExactMatch;
	}
	public void setMessageExactMatch(boolean messageExactMatch) {
		this.messageExactMatch = messageExactMatch;
	}
	public boolean isUseRemoveMisclassifiedFilter() {
		return useRemoveMisclassifiedFilter;
	}
	public void setUseRemoveMisclassifiedFilter(boolean useRemoveMisclassifiedFilter) {
		this.useRemoveMisclassifiedFilter = useRemoveMisclassifiedFilter;
	}
	public int getRemoveMisclassifiedFilterNumFolds() {
		return removeMisclassifiedFilterNumFolds;
	}
	public void setRemoveMisclassifiedFilterNumFolds(
			int removeMisclassifiedFilterNumFolds) {
		this.removeMisclassifiedFilterNumFolds = removeMisclassifiedFilterNumFolds;
	}
	public double getRemoveMisclassifiedFilterThreshold() {
		return removeMisclassifiedFilterThreshold;
	}
	public void setRemoveMisclassifiedFilterThreshold(
			double removeMisclassifiedFilterThreshold) {
		this.removeMisclassifiedFilterThreshold = removeMisclassifiedFilterThreshold;
	}
	public int getRemoveMisclassifiedFilterMaxIterations() {
		return removeMisclassifiedFilterMaxIterations;
	}
	public void setRemoveMisclassifiedFilterMaxIterations(
			int removeMisclassifiedFilterMaxIterations) {
		this.removeMisclassifiedFilterMaxIterations = removeMisclassifiedFilterMaxIterations;
	}
	public boolean isUseAttributeSelectionFilter() {
		return useAttributeSelectionFilter;
	}
	public void setUseAttributeSelectionFilter(boolean useAttributeSelectionFilter) {
		this.useAttributeSelectionFilter = useAttributeSelectionFilter;
	}
	public boolean isUseSpellChecker() {
		return useSpellChecker;
	}
	public void setUseSpellChecker(boolean useSpellChecker) {
		this.useSpellChecker = useSpellChecker;
	}
	public boolean isUseLIWC() {
		return useSpellChecker;
	}
	public void setUseLIWC(boolean useLIWC) {
		this.useLIWC = useLIWC;
	}
	public TokenizerType getTypedDependenciesTokenizerType() {
		return typedDependenciesTokenizerType;
	}
	public void setTypedDependenciesTokenizerType(
			TokenizerType typedDependenciesTokenizerType) {
		this.typedDependenciesTokenizerType = typedDependenciesTokenizerType;
	}
	public boolean isTypedDependenciesFilterUnigramsToo() {
		return typedDependenciesFilterUnigramsToo;
	}
	public void setTypedDependenciesFilterUnigramsToo(
			boolean typedDependenciesFilterUnigramsToo) {
		this.typedDependenciesFilterUnigramsToo = typedDependenciesFilterUnigramsToo;
	}
	public boolean isTypedDependenciesExactMatch() {
		return typedDependenciesExactMatch;
	}
	public void setTypedDependenciesExactMatch(boolean typedDependenciesExactMatch) {
		this.typedDependenciesExactMatch = typedDependenciesExactMatch;
	}
	public boolean isUseCommentEmbedding() {
		return useCommentEmbedding;
	}
	public void setUseCommentEmbedding(boolean useCommentEmbedding) {
		this.useCommentEmbedding = useCommentEmbedding;
	}


	private void init(){

		if(useCommentEmbedding){
			
			for(IPosting posting:trainingSamples)
			{
				if(posting instanceof Tweet){
					tweetMessagesList.add(posting.getMessage());
					labelSourceList.add(Long.toString(((Tweet)posting).getTweetid()));
				}
				else if(posting instanceof FBComment)
				{
					tweetMessagesList.add(posting.getMessage());
					labelSourceList.add(((FBComment)posting).getId());
				}
			}
			paraToVec=new ParagraphToVector();
			messageVectors=paraToVec.buildParagraphVectors(tweetMessagesList, labelSourceList);
		}
		trainingInstances=initializeInstances("train",trainingSamples);
		trainingInstances_FP=trainingInstances;
		//Reihenfolge wichtig

		if(useTypedDependencies){
			filterTypedDependencies();
		}
		
		initializeBOWFilter();

		if(useRemoveMisclassifiedFilter){
			removeMisclassified();
		}

		if(useAttributeSelectionFilter){
			attributSelectionFilter();
		}
	}

	private Instances initializeInstances(String name, List<IPosting> trainingSamples) {

		featureList=new ArrayList<Attribute>();
		featureList.add(new Attribute("message",(List<String>)null));

		if(useTypedDependencies){
			featureList.add(new Attribute("typedDependencies", (List<String>)null));
		}

		if(useSpellChecker){
			featureList.add(new Attribute("mistakes"));
			featureList.add(new Attribute("exclMarkMistakes"));
		}

		if (useLIWC) {
			for (Category categorie : FeatureExtractor.getLiwcCategories()) {
				if (!categoryBlacklistSet.contains(categorie.getTitle()))
					featureList.add(new Attribute("liwc_"
							+ categorie.getTitle()));
			}
		}

		if(useFBPostReactionType){
			List<String> fbReactions = new ArrayList<String>();
			fbReactions.add("LIKE");
			fbReactions.add("ANGRY");
			fbReactions.add("WOW");
			fbReactions.add("HAHA");
			fbReactions.add("LOVE");
			fbReactions.add("SAD");
			fbReactions.add("THANKFUL");
			fbReactions.add("NONE");

			featureList.add(new Attribute("fbReactionType", fbReactions));
		}

		if(useFBCommentCount){
			featureList.add(new Attribute("fbCommentCount"));
		}

		if(useFBLikeCount){
			featureList.add(new Attribute("fbLikeCount"));
		}
		if(useRetweetCount)
		{
			featureList.add(new Attribute("retweetCount"));
		}
		if(useIsReply)
		{
			featureList.add(new Attribute("isReply"));
		}
		if(useIsRetweet)
		{
			featureList.add(new Attribute("isRetweet"));
		}
		if(useNumberOfHashtags)
		{
			featureList.add(new Attribute("numberOfHashtags"));
		}
		if(useNumberOfFriends)
		{
			featureList.add(new Attribute("numberOfFriends"));
		}
		if(useNumberOfFollower)
		{
			featureList.add(new Attribute("numberOfFollower"));
		}
		if(useListedCount)
		{
			featureList.add(new Attribute("listedCount"));
		}
		if(useNumberOfTweets)
		{
			featureList.add(new Attribute("numberOfTweets"));
		}
		if(useLengthOfUsername)
		{
			featureList.add(new Attribute("lengthOfUsername"));
		}
		if(useLengthOfName)
		{
			featureList.add(new Attribute("lengthOfName"));
		}
		if(useNumberOfWordsInName)
		{
			featureList.add(new Attribute("numberOfWordsInName"));
		}
		if(useLengthInTokens){
			featureList.add(new Attribute("lingLengthInTokens"));
		}

		if(useAvgLengthOfWord){
			featureList.add(new Attribute("lingAvgLengthOfWord"));
		}

		if(useNumberOfSentences){
			featureList.add(new Attribute("lingNumberOfSentences"));
		}

		if(useAvgSentenceLength){
			featureList.add(new Attribute("lingAvgSentenceLength"));
		}

		if(useNumberOfCharacters){
			featureList.add(new Attribute("lingNumberOfCharacters"));
		}

		if(useNumberOfHashtags){
			featureList.add(new Attribute("lingNumberOfHashtags"));
		}

		if(useNumberOfPunctuation){
			featureList.add(new Attribute("lingNumberOfPunctuation"));
		}

		if(useNumberOfSpecialPunctuation){
			featureList.add(new Attribute("lingNumberOfSpecialPunctuation"));
		}

		if(useNumberOfOneLetterTokens){
			featureList.add(new Attribute("lingNumberOfOneLetterTokens"));
		}

		if(useNumberOfCapitalizedLetters){
			featureList.add(new Attribute("lingNumberOfCapitalizedLetters"));
		}

		if(useNumberOfURLs){
			featureList.add(new Attribute("lingNumberOfURLs"));
		}

		if(useNumberOfNonAlphaCharInMiddleOfWord){
			featureList.add(new Attribute("linguseNumberOfNonAlphaCharInMiddleOfWord"));
		}

		if(useNumberOfDiscourseConnectives){
			featureList.add(new Attribute("lexNumberOfDiscourseConnectives"));
		}

		if(useNumberOfHatefulTerms){
			featureList.add(new Attribute("lexNumberOfHatefulTerms"));
		}

		if(useDensityOfHatefulTerms){
			featureList.add(new Attribute("lexDensityOfHatefulTerms"));
		}

		if(useNumberOfDiscourseParticels){
			featureList.add(new Attribute("lexNumberOfDiscourseParticels"));
		}

		if(useNumberOfModalVerbs){
			featureList.add(new Attribute("lexNumberOfModalVerbs"));
		}

		if(useNumberOfFirstPersonPronouns){
			featureList.add(new Attribute("lexNumberOfFirstPersonPronouns"));
		}

		if(useNumberOfSecondPersonPronouns){
			featureList.add(new Attribute("lexNumberOfSecondPersonPronouns"));
		}

		if(useNumberOfThirdPersonPronouns){
			featureList.add(new Attribute("lexNumberOfThirdPersonPronouns"));
		}

		if(useNumberOfDemonstrativPronouns){
			featureList.add(new Attribute("lexNumberOfDemonstrativPronouns"));
		}

		if(useNumberOfInfinitivPronouns){
			featureList.add(new Attribute("lexNumberOfInfinitivPronouns"));
		}

		if(useNumberOfInterrogativPronouns){
			featureList.add(new Attribute("lexNumberOfInterrogativPronouns"));
		}

		if(useNumberOfHappyEmoticons){
			featureList.add(new Attribute("lexNumberOfHappyEmoticons"));
		}

		if(useNumberOfSadEmoticons){
			featureList.add(new Attribute("lexNumberOfSadEmoticons"));
		}

		if(useNumberOfCheekyEmoticons){
			featureList.add(new Attribute("lexNumberOfCheekyEmoticons"));
		}

		if(useNumberOfAmazedEmoticons){
			featureList.add(new Attribute("lexNumberOfAmazedEmoticons"));
		}
		if(useCommentEmbedding){
			for(int i=0;i<messageVectors.getLayerSize();i++)
				featureList.add(new Attribute("vectorAttribute_"+i));
		}

		List<String> hatepostResults = new ArrayList<String>();
		hatepostResults.add("negative");
		hatepostResults.add("positive");
		featureList.add(new Attribute("__hatepost__",hatepostResults));

		Instances instances = new Instances(name, featureList, trainingSamples.size());
		instances.setClassIndex(featureList.size()-1);

		updateData(trainingSamples, instances, featureList.size());

		return instances;
	}

	private void updateData(List<IPosting> trainingSamples, Instances instances, int rowSize) {

		Pattern p = Pattern.compile("\\b\\d+\\b");
		Pattern p2 = Pattern.compile("\\[[0-9a-z_A-ZäÄöÖüÜß]+\\]");

		for(IPosting posting : trainingSamples)
		{

			//not used at the moment
			String message = posting.getMessage();

			message = message.replace("'", "");
			message = message.replace("’", "");
			message = message.replace("xD", "");
			message = message.replace(":D", "");
			message = message.replace("[…]", "");
			message = p.matcher(message).replaceAll("");
			message = p2.matcher(message).replaceAll("");

			DenseInstance instance = createInstance(posting, instances, rowSize);
			instance.setClassValue(posting.getPostType().toString().toLowerCase());
			instances.add(instance);
		}	
	}

	/**
	 * Method that converts a text message into an instance.
	 */
	private DenseInstance createInstance(IPosting posting, Instances data, int rowSize) {

		// Create instance of length rowSize
		DenseInstance instance = new DenseInstance(rowSize);

		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);

		// Set value for message attribute
		Attribute messageAtt = data.attribute("message");
		instance.setValue(messageAtt, posting.getMessage());


		if(useSpellChecker){

			//Set value for mistakes attribute
			Attribute mistakesAtt = data.attribute("mistakes");
			instance.setValue(mistakesAtt, FeatureExtractor.getMistakes(posting.getMessage()));
		}

		/*
		//Set value for ExplanationMark Mistakes
		Attribute exklMarkmistakesAtt = data.attribute("exclMarkMistakes");
		instance.setValue(exklMarkmistakesAtt, FeatureExtraktor.getExclMarkMistakes(text));*/


		if(useTypedDependencies){
			//Set value for typedDependencies attribute
			Attribute typedDependenciesAtt = data.attribute("typedDependencies");
			instance.setValue(typedDependenciesAtt, FeatureExtractor.getTypedDependencies(posting.getMessage(), TypedDependencyWordType.LEMMA));
		}

		if(useLIWC)
		{
			// Set liwc category values
			List<CategoryScore> scores = FeatureExtractor.getLiwcCountsPerCategory(posting.getMessage());

			for (CategoryScore catScore : scores) {
				if (!categoryBlacklistSet.contains(catScore.getCategory()
						.getTitle())) {
					Attribute liwcAttr = data.attribute("liwc_"
							+ catScore.getCategory().getTitle());
					instance.setValue(liwcAttr, catScore.getScore());
				}
			}

			double[] defaultValues = new double[rowSize];
			instance.replaceMissingValues(defaultValues);
		}


		if(useFBPostReactionType){

			Attribute reactionTypeAtt = data.attribute("fbReactionType");

			if(posting instanceof FBComment){
				instance.setValue(reactionTypeAtt, FeatureExtractor.getFBReactionByFBComment((FBComment) posting));
			}
			else{
				instance.setValue(reactionTypeAtt, WEKA_MISSING_VALUE);
			}
		}

		if(useFBCommentCount){
			Attribute commentCountAtt = data.attribute("fbCommentCount");

			if(posting instanceof FBComment){
				instance.setValue(commentCountAtt, ((FBComment) posting).getCommentCount());
			}
			else{
				instance.setValue(commentCountAtt, WEKA_MISSING_VALUE);
			}
		}

		if(useFBLikeCount){
			Attribute likeCountAtt = data.attribute("fbLikeCount");

			if(posting instanceof FBComment){
				instance.setValue(likeCountAtt, ((FBComment) posting).getLikeCount());
			}
			else{
				instance.setValue(likeCountAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useRetweetCount){
			Attribute retweetCountAtt = data.attribute("retweetCount");

			if(posting instanceof Tweet){
				instance.setValue(retweetCountAtt, ((Tweet) posting).getRetweetcount());
			}
			else{
				instance.setValue(retweetCountAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useIsReply){
			Attribute isReplyAtt = data.attribute("isReply");

			if(posting instanceof Tweet){
				instance.setValue(isReplyAtt, ((Tweet) posting).isReply()==true?1:0);
			}
			else{
				instance.setValue(isReplyAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useIsRetweet){
			Attribute isRetweetAtt = data.attribute("isRetweet");

			if(posting instanceof Tweet){
				instance.setValue(isRetweetAtt, ((Tweet) posting).isRetweet()==true?1:0);
			}
			else{
				instance.setValue(isRetweetAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useNumberOfFriends){
			Attribute numberOfFriendsAtt = data.attribute("numberOfFriends");

			if(posting instanceof Tweet){
				if(((Tweet)posting).getUser().getUsername()!=null)
					instance.setValue(numberOfFriendsAtt, ((Tweet) posting).getUser().getFriendscount());
				else
					instance.setValue(numberOfFriendsAtt, WEKA_MISSING_VALUE);
			}
			else{
				instance.setValue(numberOfFriendsAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useNumberOfFollower){
			Attribute numberOfFollowerAtt = data.attribute("numberOfFollower");

			if(posting instanceof Tweet){
				if(((Tweet)posting).getUser().getUsername()!=null)
					instance.setValue(numberOfFollowerAtt, ((Tweet) posting).getUser().getFollowerscount());
				else
					instance.setValue(numberOfFollowerAtt, WEKA_MISSING_VALUE);
			}
			else{
				instance.setValue(numberOfFollowerAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useListedCount){
			Attribute listedCountAtt = data.attribute("listedCount");

			if(posting instanceof Tweet){
				if(((Tweet)posting).getUser().getUsername()!=null)
					instance.setValue(listedCountAtt, ((Tweet) posting).getUser().getListedcount());
				else
					instance.setValue(listedCountAtt, WEKA_MISSING_VALUE);
			}
			else{
				instance.setValue(listedCountAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useNumberOfTweets){
			Attribute numberOfTweetsAtt = data.attribute("numberOfTweets");

			if(posting instanceof Tweet){
				if(((Tweet)posting).getUser().getUsername()!=null)
					instance.setValue(numberOfTweetsAtt, ((Tweet) posting).getUser().getTweetcount());
				else
					instance.setValue(numberOfTweetsAtt, WEKA_MISSING_VALUE);
			}
			else{
				instance.setValue(numberOfTweetsAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useLengthOfUsername){
			Attribute lengthOfUsernameAtt = data.attribute("lengthOfUsername");

			if(posting instanceof Tweet){
				if(((Tweet)posting).getUser().getUsername()!=null)
					instance.setValue(lengthOfUsernameAtt, ((Tweet) posting).getUser().getUsername().length());
				else
					instance.setValue(lengthOfUsernameAtt, WEKA_MISSING_VALUE);
			}
			else{
				instance.setValue(lengthOfUsernameAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useLengthOfName){
			Attribute lengthOfNameAtt = data.attribute("lengthOfName");

			if(posting instanceof Tweet){
				if(((Tweet)posting).getUser().getUsername()!=null)
					instance.setValue(lengthOfNameAtt, ((Tweet) posting).getUser().getName().length());
				else
					instance.setValue(lengthOfNameAtt, WEKA_MISSING_VALUE);
			}
			else{
				instance.setValue(lengthOfNameAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useNumberOfWordsInName){
			Attribute numberOfWordsInNameAtt = data.attribute("numberOfWordsInName");

			if(posting instanceof Tweet){
				if(((Tweet)posting).getUser().getUsername()!=null)
					instance.setValue(numberOfWordsInNameAtt, ((Tweet) posting).getUser().getName().split(" ").length);
				else
					instance.setValue(numberOfWordsInNameAtt, WEKA_MISSING_VALUE);
			}
			else{
				instance.setValue(numberOfWordsInNameAtt, WEKA_MISSING_VALUE);
			}
		}
		if(useLengthInTokens){
			Attribute lingLengthInTokensAtt = data.attribute("lingLengthInTokens");
			instance.setValue(lingLengthInTokensAtt, FeatureExtractor.getLengthInTokens(posting.getMessage()));
		}

		if(useAvgLengthOfWord){
			Attribute lingAvgLengthOfWordAtt = data.attribute("lingAvgLengthOfWord");
			instance.setValue(lingAvgLengthOfWordAtt, FeatureExtractor.getAvgLengthOfWord(posting.getMessage()));
		}

		if(useNumberOfSentences){
			Attribute lingNumberOfSentencesAtt = data.attribute("lingNumberOfSentences");
			instance.setValue(lingNumberOfSentencesAtt, FeatureExtractor.getNumberOfSentences(posting.getMessage()));
		}

		if(useAvgSentenceLength){
			Attribute lingAvgSentenceLengthAtt = data.attribute("lingAvgSentenceLength");
			instance.setValue(lingAvgSentenceLengthAtt, FeatureExtractor.getAvgSentenceLength(posting.getMessage()));
		}

		if(useNumberOfCharacters){
			Attribute lingNumberOfCharactersAtt = data.attribute("lingNumberOfCharacters");
			instance.setValue(lingNumberOfCharactersAtt, FeatureExtractor.getNumberOfCharacters(posting.getMessage()));
		}

		if(useNumberOfHashtags){
			Attribute lingNumberOfHashtagsAtt = data.attribute("lingNumberOfHashtags");
			instance.setValue(lingNumberOfHashtagsAtt, FeatureExtractor.getNumberOfHashtags(posting.getMessage()));
		}

		if(useNumberOfPunctuation){
			Attribute lingNumberOfPunctuationAtt = data.attribute("lingNumberOfPunctuation");
			instance.setValue(lingNumberOfPunctuationAtt, FeatureExtractor.getNumberOfPunctuation(posting.getMessage()));
		}

		if(useNumberOfSpecialPunctuation){
			Attribute lingNumberOfSpecialPunctuationAtt = data.attribute("lingNumberOfSpecialPunctuation");
			instance.setValue(lingNumberOfSpecialPunctuationAtt, FeatureExtractor.getNumberOfSpecialPunctuation(posting.getMessage()));
		}

		if(useNumberOfOneLetterTokens){
			Attribute lingNumberOfOneLetterTokensAtt = data.attribute("lingNumberOfOneLetterTokens");
			instance.setValue(lingNumberOfOneLetterTokensAtt, FeatureExtractor.getNumberOfOneLetterTokens(posting.getMessage()));
		}

		if(useNumberOfCapitalizedLetters){
			Attribute lingNumberOfCapitalizedLettersAtt = data.attribute("lingNumberOfCapitalizedLetters");
			instance.setValue(lingNumberOfCapitalizedLettersAtt, FeatureExtractor.getNumberOfCapitalizedLetters(posting.getMessage()));
		}

		if(useNumberOfURLs){
			Attribute lingNumberOfURLsAtt = data.attribute("lingNumberOfURLs");
			instance.setValue(lingNumberOfURLsAtt, FeatureExtractor.getNumberOfURLs(posting.getMessage()));
		}

		if(useNumberOfNonAlphaCharInMiddleOfWord){
			Attribute lingNumberOfNonAlphaCharInMiddleOfWordAtt = data.attribute("linguseNumberOfNonAlphaCharInMiddleOfWord");
			instance.setValue(lingNumberOfNonAlphaCharInMiddleOfWordAtt, FeatureExtractor.getNumberOfNonAlphaCharInMiddleOfWord(posting.getMessage()));
		}

		if(useNumberOfDiscourseConnectives){
			Attribute lexNumberOfDiscourseConnectivesAtt = data.attribute("lexNumberOfDiscourseConnectives");
			instance.setValue(lexNumberOfDiscourseConnectivesAtt, FeatureExtractor.getNumberOfDiscourseConnectives(posting.getMessage()));
		}

		if(useNumberOfHatefulTerms){
			Attribute lexNumberOfHatefulTermsAtt = data.attribute("lexNumberOfHatefulTerms");
			instance.setValue(lexNumberOfHatefulTermsAtt, FeatureExtractor.getNumberOfHatefulTerms(posting.getMessage()));
		}

		if(useDensityOfHatefulTerms){
			Attribute lexDensityOfHatefulTermsAtt = data.attribute("lexDensityOfHatefulTerms");
			instance.setValue(lexDensityOfHatefulTermsAtt, FeatureExtractor.getDensityOfHatefulTerms(posting.getMessage()));
		}

		if(useNumberOfDiscourseParticels){
			Attribute lexNumberOfDiscourseParticelsAtt = data.attribute("lexNumberOfDiscourseParticels");
			instance.setValue(lexNumberOfDiscourseParticelsAtt, FeatureExtractor.getNumberOfDiscourseParticels(posting.getMessage()));
		}

		if(useNumberOfModalVerbs){
			Attribute lexNumberOfModalVerbsAtt = data.attribute("lexNumberOfModalVerbs");
			instance.setValue(lexNumberOfModalVerbsAtt, FeatureExtractor.getNumberOfModalVerbs(posting.getMessage()));
		}

		if(useNumberOfFirstPersonPronouns){
			Attribute lexNumberOfFirstPersonPronounsAtt = data.attribute("lexNumberOfFirstPersonPronouns");
			instance.setValue(lexNumberOfFirstPersonPronounsAtt, FeatureExtractor.getNumberOfFirstPersonPronouns(posting.getMessage()));
		}

		if(useNumberOfSecondPersonPronouns){
			Attribute lexNumberOfSecondPersonPronounsAtt = data.attribute("lexNumberOfSecondPersonPronouns");
			instance.setValue(lexNumberOfSecondPersonPronounsAtt, FeatureExtractor.getNumberOfSecondPersonPronouns(posting.getMessage()));
		}

		if(useNumberOfThirdPersonPronouns){
			Attribute lexNumberOfThirdPersonPronounsAtt = data.attribute("lexNumberOfThirdPersonPronouns");
			instance.setValue(lexNumberOfThirdPersonPronounsAtt, FeatureExtractor.getNumberOfThirdPersonPronouns(posting.getMessage()));
		}

		if(useNumberOfDemonstrativPronouns){
			Attribute lexNumberOfDemonstrativPronounsAtt = data.attribute("lexNumberOfDemonstrativPronouns");
			instance.setValue(lexNumberOfDemonstrativPronounsAtt, FeatureExtractor.getNumberOfDemonstrativPronouns(posting.getMessage()));
		}

		if(useNumberOfInfinitivPronouns){
			Attribute lexNumberOfInfinitivPronounsAtt = data.attribute("lexNumberOfInfinitivPronouns");
			instance.setValue(lexNumberOfInfinitivPronounsAtt, FeatureExtractor.getNumberOfInfinitivPronouns(posting.getMessage()));
		}

		if(useNumberOfInterrogativPronouns){
			Attribute lexNumberOfInterrogativPronounsAtt = data.attribute("lexNumberOfInterrogativPronouns");
			instance.setValue(lexNumberOfInterrogativPronounsAtt, FeatureExtractor.getNumberOfInterrogativPronouns(posting.getMessage()));
		}

		if(useNumberOfHappyEmoticons){
			Attribute lexNumberOfHappyEmoticonsAtt = data.attribute("lexNumberOfHappyEmoticons");
			instance.setValue(lexNumberOfHappyEmoticonsAtt, FeatureExtractor.getNumberOfHappyEmoticons(posting.getMessage()));
		}

		if(useNumberOfSadEmoticons){
			Attribute lexNumberOfSadEmoticonsAtt = data.attribute("lexNumberOfSadEmoticons");
			instance.setValue(lexNumberOfSadEmoticonsAtt, FeatureExtractor.getNumberOfSadEmoticons(posting.getMessage()));
		}

		if(useNumberOfCheekyEmoticons){
			Attribute lexNumberOfCheekyEmoticonsAtt = data.attribute("lexNumberOfCheekyEmoticons");
			instance.setValue(lexNumberOfCheekyEmoticonsAtt, FeatureExtractor.getNumberOfCheekyEmoticons(posting.getMessage()));
		}

		if(useNumberOfAmazedEmoticons){
			Attribute lexNumberOfAmazedEmoticonsAtt = data.attribute("lexNumberOfAmazedEmoticons");
			instance.setValue(lexNumberOfAmazedEmoticonsAtt, FeatureExtractor.getNumberOfAmazedEmoticons(posting.getMessage()));
		}
		if(useCommentEmbedding){
			INDArray messageVec=null;
		
			if(posting instanceof Tweet)
				messageVec=messageVectors.getLookupTable().vector(Long.toString(((Tweet)posting).getTweetid()));
			else if(posting instanceof FBComment)
				messageVec=messageVectors.getLookupTable().vector(((FBComment)posting).getId());
			
			for(int i=0;i<messageVectors.getLayerSize();i++){
				instance.setValue(data.attribute("vectorAttribute_"+i),messageVec.getDouble(i));
			}
		}

		return instance;
	}

	private void initializeBOWFilter() {

		NGramTokenizer tokenizer = null;

		if(messageTokenizerType == TokenizerType.HATEFUL_TERMS_NGRAM){
			tokenizer = new RetainHatefulTermsNGramTokenizer();

			((RetainHatefulTermsNGramTokenizer) tokenizer).setFilterUnigramsToo(messageFilterUnigramsToo);
			((RetainHatefulTermsNGramTokenizer) tokenizer).setTokenFormatTypedDependencies(false);
			((RetainHatefulTermsNGramTokenizer) tokenizer).setExactMatch(messageExactMatch);
		}
		else if(messageTokenizerType == TokenizerType.NGRAM){
			tokenizer = new NGramTokenizer();
		}

		tokenizer.setNGramMinSize(messageNGramMinSize);
		tokenizer.setNGramMaxSize(messageNGramMaxSize);
		tokenizer.setDelimiters("[^0-9a-zA-ZäÄöÖüÜß]");

		sTWfilter = new StringToWordVector();

		sTWfilter.setTokenizer(tokenizer);
		sTWfilter.setWordsToKeep(1000000);
		//sTWfilter.setDoNotOperateOnPerClassBasis(true);
		sTWfilter.setLowerCaseTokens(true);

		//Apply Stopwordlist
		WordsFromFile stopwords =new WordsFromFile();
		stopwords.setStopwords(new File("../stopwords.txt"));
		sTWfilter.setStopwordsHandler(stopwords);

		//Apply Stemmer
		SnowballStemmer stemmer= new SnowballStemmer("german");
		sTWfilter.setStemmer(stemmer);

		//Apply IDF-TF Weighting + DocLength-Normalization
		sTWfilter.setTFTransform(true);
		sTWfilter.setIDFTransform(true);
		sTWfilter.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));

		//experimental
		//sTWfilter.setOutputWordCounts(true);

		sTWfilter.setAttributeIndices("first");
		try {
			sTWfilter.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, sTWfilter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void filterTypedDependencies(){

		NGramTokenizer tokenizer = null;

		if(typedDependenciesTokenizerType == TokenizerType.HATEFUL_TERMS_NGRAM){
			tokenizer = new RetainHatefulTermsNGramTokenizer();

			((RetainHatefulTermsNGramTokenizer) tokenizer).setFilterUnigramsToo(typedDependenciesFilterUnigramsToo);
			((RetainHatefulTermsNGramTokenizer) tokenizer).setTokenFormatTypedDependencies(true);
			((RetainHatefulTermsNGramTokenizer) tokenizer).setExactMatch(typedDependenciesExactMatch);
		}
		else if(typedDependenciesTokenizerType == TokenizerType.NGRAM){
			tokenizer = new NGramTokenizer();
		}

		tokenizer.setNGramMinSize(typedDependenciesNGramMinSize);
		tokenizer.setNGramMaxSize(typedDependenciesNGramMaxSize);
		tokenizer.setDelimiters("[ \\n]");

		StringToWordVector stringToWordVectorFilter = new StringToWordVector();

		stringToWordVectorFilter.setTokenizer(tokenizer);
		stringToWordVectorFilter.setAttributeIndices("2");
		stringToWordVectorFilter.setWordsToKeep(1000000);
		stringToWordVectorFilter.setLowerCaseTokens(true);
		//experimental
		//stringToWordVectorFilter.setOutputWordCounts(true);

		try {
			stringToWordVectorFilter.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, stringToWordVectorFilter);
			//System.out.println(trainingInstances.toSummaryString());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	private void attributSelectionFilter()
	{
		attributeFilter = new AttributeSelection(); 

		InfoGainAttributeEval ev = new InfoGainAttributeEval(); 
		Ranker ranker = new Ranker(); 
		//ranker.setNumToSelect(4500);

		attributeFilter.setEvaluator(ev); 
		attributeFilter.setSearch(ranker);

		try {
			attributeFilter.setInputFormat(trainingInstances);
			trainingInstances=Filter.useFilter(trainingInstances, attributeFilter);
			System.out.println("Calculated NumToSelect: " + ranker.getCalculatedNumToSelect());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void removeMisclassified()
	{
		RemoveMisclassified misFilter = new RemoveMisclassified();
		misFilter.setClassifier(classifier);
		misFilter.setClassIndex(trainingInstances.classIndex());
		misFilter.setNumFolds(removeMisclassifiedFilterNumFolds);
		misFilter.setThreshold(removeMisclassifiedFilterThreshold);
		misFilter.setMaxIterations(removeMisclassifiedFilterMaxIterations);
		try {
			misFilter.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, misFilter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * This method evaluates the classifier. As recommended by WEKA documentation,
	 * the classifier is defined but not trained yet. Evaluation of previously
	 * trained classifiers can lead to unexpected results.
	 */
	public void evaluate() {
		logRunConfiguration();

		if(trainingInstances == null){
			init();
		}

		try {

			Evaluation eval = new Evaluation(trainingInstances);
			eval.crossValidateModel(classifier, trainingInstances, 10, new Random(1));
			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			//System.out.println(trainingInstances.toSummaryString());

			System.out.println("===== Evaluating on filtered (training) dataset done =====");
			logRunEvaluation(eval);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem found when evaluating");
		}
	}

	/**
	 * This method trains the classifier on the dataset.
	 */
	public void learn() {
		try {

			classifier.buildClassifier(trainingInstances);

			//System.out.println(classifier);
			System.out.println("===== Training on filtered (training) dataset done =====");
		}
		catch (Exception e) {
			System.out.println("Problem found when training");
		}
	}
	public Double classify(IPosting posting)  {
		Instances testInstances = new Instances("live", featureList, 1);
		testInstances.setClassIndex(featureList.size() - 1);
		DenseInstance instanceToClassify = createInstance(posting, testInstances, featureList.size());
		instanceToClassify.setClassMissing();
		testInstances.add(instanceToClassify);

		try {
			testInstances=Filter.useFilter(testInstances, sTWfilter);

			if(isUseAttributeSelectionFilter())
				testInstances=Filter.useFilter(testInstances, attributeFilter);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Double classification=null;
		try {
			classification = classifier.classifyInstance(testInstances.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classification;

	}
	private void findFalsePositives(int numberOfFolds)
	{
		Instances randData, trainSplit = null, testSplit = null;

		Random rand = new Random(1);   
		randData = new Instances(trainingInstances_FP);   
		randData.randomize(rand);
		int numFP = 0;

		for (int i = 0; i < numberOfFolds; i++) {
			trainSplit = randData.trainCV(numberOfFolds, i);
			testSplit = randData.testCV(numberOfFolds, i);

			try {
				trainSplit = Filter.useFilter(trainSplit, sTWfilter);
				if(isUseAttributeSelectionFilter()){
					trainSplit = Filter.useFilter(trainSplit, attributeFilter);
				}

				classifier.buildClassifier(trainSplit);
				for(Instance testInstance : testSplit)
				{
					//System.out.println(testSplit.numAttributes());
					Attribute messattr = testSplit.attribute("message");
					//Posting post=new Posting(testInstance.stringValue(messattr),null,PostType.valueOf(testInstance.stringValue(testSplit.attribute("__hatepost__")).toUpperCase()));

					//Result
					final PostType testSplitPostType = PostType.valueOf(testInstance.stringValue(testSplit.attribute("__hatepost__")).toUpperCase());
					//Message
					final String testSplitmessage = testInstance.stringValue(messattr);

					Double classification = classify(new IPosting() {

						@Override
						public PostType getPostType() {
							//not used - classified by classify()
							return null;
						}

						@Override
						public String getMessage() {
							return testSplitmessage;
						}
					});
					if(classification != testSplitPostType.getValue())
					{
						numFP++;
						System.out.println(testSplitmessage + " " + testSplitPostType);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(numFP);
		learn();
	}

	public void saveInstancesToArff(){
		ArffSaver saver = new ArffSaver();
		saver.setInstances(trainingInstances);
		try {
			saver.setFile(new File("hatespeech.arff"));
			//saver.setDestination(new File("./data/test.arff"));   // **not** necessary in 3.5.4 and later
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void logRunConfiguration(){
		logger.info("configuration for run: {}\nmessageTokenizerType = {}\nmessageNGramMinSize = {}\nmessageNGramMaxSize = {}\nmessageFilterUnigramsToo = {}\nmessageExactMatch = {}\n"
				+ "useTypedDependencies = {}\ntypedDependenciesTokenizerType = {}\ntypedDependenciesNGramMinSize = {}\ntypedDependenciesNGramMaxSize = {}\ntypedDependenciesFilterUnigramsToo = {}\n"
				+ "typedDependenciesExactMatch = {}\nuseRemoveMisclassifiedFilter = {}\nremoveMisclassifiedFilterNumFolds = {}\nremoveMisclassifiedFilterThreshold = {}\n"
				+ "removeMisclassifiedFilterMaxIterations = {}\nuseAttributeSelectionFilter = {}\nuseSpellChecker = {}\nuseLIWC = {}\nuseFBPostReactionType = {}\nuseFBCommentCount = {}\n"
				+ "useFBLikeCount = {}\nuseLengthInTokens = {}\nuseAvgLengthOfWord = {}\nuseNumberOfSentences = {}\nuseAvgSentenceLength = {}\nuseNumberOfCharacters = {}\nuseNumberOfHashtags = {}\n"
				+ "useNumberOfPunctuation = {}\nuseNumberOfSpecialPunctuation = {}\nuseNumberOfOneLetterTokens = {}\nuseNumberOfCapitalizedLetters = {}\nuseNumberOfURLs = {}\n"
				+ "useNumberOfNonAlphaCharInMiddleOfWord = {}\nuseNumberOfDiscourseConnectives = {}\nuseNumberOfHatefulTerms = {}\nuseDensityOfHatefulTerms = {}\nuseNumberOfDiscourseParticels = {}\n"
				+ "useNumberOfModalVerbs = {}\nuseNumberOfFirstPersonPronouns = {}\nuseNumberOfSecondPersonPronouns = {}\nuseNumberOfThirdPersonPronouns = {}\nuseNumberOfDemonstrativPronouns = {}\n"
				+ "useNumberOfInfinitivPronouns = {}\nuseNumberOfInterrogativPronouns = {}\nuseNumberOfHappyEmoticons = {}\nuseNumberOfSadEmoticons = {}\nuseNumberOfCheekyEmoticons = {}\n"
				+ "useNumberOfAmazedEmoticons = {}",
				runName, messageTokenizerType.name(), messageNGramMinSize, messageNGramMaxSize, messageFilterUnigramsToo, messageExactMatch, useTypedDependencies, typedDependenciesTokenizerType.name(),
				typedDependenciesNGramMinSize, typedDependenciesNGramMaxSize, typedDependenciesFilterUnigramsToo, typedDependenciesExactMatch,	useRemoveMisclassifiedFilter,
				removeMisclassifiedFilterNumFolds, removeMisclassifiedFilterThreshold, removeMisclassifiedFilterMaxIterations, useAttributeSelectionFilter, useSpellChecker, useLIWC,
				useFBPostReactionType, useFBCommentCount, useFBLikeCount, useLengthInTokens, useAvgLengthOfWord, useNumberOfSentences, useAvgSentenceLength, useNumberOfCharacters, useNumberOfHashtags,
				useNumberOfPunctuation, useNumberOfSpecialPunctuation, useNumberOfOneLetterTokens, useNumberOfCapitalizedLetters, useNumberOfURLs, useNumberOfNonAlphaCharInMiddleOfWord,
				useNumberOfDiscourseConnectives, useNumberOfHatefulTerms, useDensityOfHatefulTerms, useNumberOfDiscourseParticels, useNumberOfModalVerbs, useNumberOfFirstPersonPronouns,
				useNumberOfSecondPersonPronouns, useNumberOfThirdPersonPronouns, useNumberOfDemonstrativPronouns, useNumberOfInfinitivPronouns, useNumberOfInterrogativPronouns, useNumberOfHappyEmoticons,
				useNumberOfSadEmoticons, useNumberOfCheekyEmoticons, useNumberOfAmazedEmoticons);
	}

	private void logRunEvaluation(Evaluation eval) throws Exception{
		logger.info("evaluation for run: {}\n{}\n{}", runName, eval.toSummaryString(), eval.toClassDetailsString());
	}

}
