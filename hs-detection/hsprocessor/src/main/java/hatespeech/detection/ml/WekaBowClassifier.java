package hatespeech.detection.ml;

import hatespeech.detection.hsprocessor.FeatureExtractor;
import hatespeech.detection.hsprocessor.FeatureExtractor.TypedDependencyWordType;
import hatespeech.detection.model.Category;
import hatespeech.detection.model.CategoryScore;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.IPosting;
import hatespeech.detection.model.PostType;
import hatespeech.detection.tokenizer.RetainHatefulTermsNGramTokenizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

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
	private Instances trainingInstances = null,trainingInstances_FP;
	private ArrayList<Attribute> featureList=null;
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
	private boolean useReactionType = false;


	public WekaBowClassifier(List<IPosting> trainingSamples, Classifier classifier){
		this.classifier=classifier;
		this.trainingSamples = trainingSamples;
	}


	public String getRunName() {
		return runName;
	}
	public void setRunName(String runName) {
		this.runName = runName;
	}
	public boolean isUseReactionType() {
		return useReactionType;
	}
	public void setUseReactionType(boolean useReactionType) {
		this.useReactionType = useReactionType;
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


	private void init(){

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

		featureList.add(new Attribute("mistakes"));
		featureList.add(new Attribute("exclMarkMistakes"));

		if(useTypedDependencies){
			featureList.add(new Attribute("typedDependencies", (List<String>)null));
		}

		if (useLIWC) {
			for (Category categorie : FeatureExtractor.getLiwcCategories()) {
				if (!categoryBlacklistSet.contains(categorie.getTitle()))
					featureList.add(new Attribute("liwc_"
							+ categorie.getTitle()));
			}
		}

		if(useReactionType){
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
		Pattern p2 = Pattern.compile("\\[[0-9a-z_A-Z�������]+\\]");

		for(IPosting posting : trainingSamples)
		{

			String message = posting.getMessage();

			message = message.replace("'", "");
			message = message.replace("�", "");
			message = message.replace("xD", "");
			message = message.replace(":D", "");
			message = message.replace("[�]", "");
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


		if(useReactionType){

			Attribute reactionTypeAtt = data.attribute("fbReactionType");
			
			if(posting instanceof FBComment){
				instance.setValue(reactionTypeAtt, FeatureExtractor.getFBReactionByFBComment((FBComment) posting));
			}
			else{
				instance.setValue(reactionTypeAtt, WEKA_MISSING_VALUE);
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
		tokenizer.setDelimiters("[^0-9a-zA-Z�������]");

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
		stringToWordVectorFilter.setAttributeIndices("4");
		stringToWordVectorFilter.setWordsToKeep(1000000);
		stringToWordVectorFilter.setLowerCaseTokens(true);

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
	
	private void logRunConfiguration(){
		logger.info("configuration for run: {}\nmessageTokenizerType = {}\nmessageNGramMinSize = {}\nmessageNGramMaxSize = {}\nmessageFilterUnigramsToo = {}\nmessageExactMatch = {}\n"
				+ "useTypedDependencies = {}\ntypedDependenciesTokenizerType = {}\ntypedDependenciesNGramMinSize = {}\ntypedDependenciesNGramMaxSize = {}\ntypedDependenciesFilterUnigramsToo = {}\n"
				+ "typedDependenciesExactMatch = {}\nuseRemoveMisclassifiedFilter = {}\nremoveMisclassifiedFilterNumFolds = {}\nremoveMisclassifiedFilterThreshold = {}\n"
				+ "removeMisclassifiedFilterMaxIterations = {}\nuseAttributeSelectionFilter = {}\nuseSpellChecker = {}\nuseLIWC = {}\nuseReactionType = {}",runName, messageTokenizerType.name(),
				messageNGramMinSize, messageNGramMaxSize, messageFilterUnigramsToo, messageExactMatch, useTypedDependencies, typedDependenciesTokenizerType.name(), typedDependenciesNGramMinSize,
				typedDependenciesNGramMaxSize, typedDependenciesFilterUnigramsToo, typedDependenciesExactMatch,	useRemoveMisclassifiedFilter, removeMisclassifiedFilterNumFolds,
				removeMisclassifiedFilterThreshold, removeMisclassifiedFilterMaxIterations, useAttributeSelectionFilter, useSpellChecker, useLIWC, useReactionType);
	}
	
	private void logRunEvaluation(Evaluation eval) throws Exception{
		logger.info("evaluation for run: {}\n{}\n{}", runName, eval.toSummaryString(), eval.toClassDetailsString());
	}

}
