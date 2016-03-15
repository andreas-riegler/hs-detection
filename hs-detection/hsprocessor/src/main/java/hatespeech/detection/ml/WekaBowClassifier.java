package hatespeech.detection.ml;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.hsprocessor.LIWCDictionary;
import hatespeech.detection.hsprocessor.SpellCorrector;
import hatespeech.detection.model.Category;
import hatespeech.detection.model.CategoryScore;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.HatePost;
import hatespeech.detection.model.PostType;
import hatespeech.detection.model.Posting;
import hatespeech.detection.model.SpellCheckedMessage;
import hatespeech.detection.tokenizer.RetainHatefulTermsNGramTokenizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class WekaBowClassifier {

	private Instances trainingInstances;
	private StringToWordVector filter;
	private Classifier classifier;
	private FilteredClassifier filteredClassifier;
	private SpellCorrector spellCorr;
	private LIWCDictionary liwcDic;
	private String[] categoryBlacklist = {"Pronoun","I","We","You","Self","Other","Article","Preps","Past","Present","Future"};
	private Set<String> categoryBlacklistSet = new HashSet<String>(Arrays.asList(categoryBlacklist));
	private String[] categoryWhitelist = {"Assent","Affect","Swear","Death","Relig","Space","Home","Discrepancy","Sad","Anger","Anxiety","Negative_emotion","Positive_feeling","Positive_emotion","Social"};
	private Set<String> categoryWhitelistSet = new HashSet<String>(Arrays.asList(categoryWhitelist));

	public WekaBowClassifier(List<Posting> trainingSamples,Classifier classifier){

		this.classifier=classifier;
		spellCorr=new SpellCorrector();
		liwcDic=LIWCDictionary.loadDictionaryFromFile("../dictionary.obj");
		
		trainingInstances=initializeInstances("train",trainingSamples);

		//Reihenfolge wichtig
		//filterTypedDependencies();
		initializeBOWFilter();
		attributSelectionFilter();
	}



	private Instances initializeInstances(String name,List<Posting> trainingSamples) {

		ArrayList<Attribute> featureList=new ArrayList<Attribute>();
		featureList.add(new Attribute("message",(List<String>)null));
		
		featureList.add(new Attribute("mistakes"));
		featureList.add(new Attribute("exclMarkMistakes"));

		//featureList.add(new Attribute("typedDependencies", (List<String>)null));
		
		for(Category categorie: liwcDic.getCategories())
		{
			if(!categoryBlacklistSet.contains(categorie.getTitle()))
				featureList.add(new Attribute("liwc_"+categorie.getTitle()));
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

	private void updateData(List<Posting> trainingSamples, Instances instances,int rowSize) {

		for(Posting post:trainingSamples)
		{
			DenseInstance instance = createInstance(post.getMessage(), post.getTypedDependencies(), instances, rowSize);
			instance.setClassValue(post.getPostType().toString().toLowerCase());
			instances.add(instance);
		}	
	}

	/**
	 * Method that converts a text message into an instance.
	 */
	private DenseInstance createInstance(String text, String typedDependencies, Instances data,int rowSize) {

		// Create instance of length rowSize
		DenseInstance instance = new DenseInstance(rowSize);

		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);

		// Set value for message attribute
		Attribute messageAtt = data.attribute("message");
		instance.setValue(messageAtt, text);

		
		SpellCheckedMessage checkedMessage=spellCorr.findMistakes(text);
		//Set value for mistakes attribute
		Attribute mistakesAtt = data.attribute("mistakes");
		instance.setValue(mistakesAtt, checkedMessage.getMistakes());

		/*
		//Set value for ExplanationMark Mistakes
		Attribute exklMarkmistakesAtt = data.attribute("exclMarkMistakes");
		instance.setValue(exklMarkmistakesAtt, checkedMessage.getExclMarkMistakes());
		*/ 

		//Set value for typedDependencies attribute
		//Attribute typedDependenciesAtt = data.attribute("typedDependencies");
		//instance.setValue(typedDependenciesAtt, typedDependencies);
		
		
		//Set liwc category values
		List<CategoryScore> scores=liwcDic.classifyMessage(text);
		
		for(CategoryScore catScore: scores)
		{
			if(!categoryBlacklistSet.contains(catScore.getCategory().getTitle()))
			{
				Attribute liwcAttr = data.attribute("liwc_"+catScore.getCategory().getTitle());
				instance.setValue(liwcAttr, catScore.getScore());
			}
		}
		double[] defaultValues = new double[rowSize];
		instance.replaceMissingValues(defaultValues);
		
		return instance;
	}

	private void initializeBOWFilter() {

		RetainHatefulTermsNGramTokenizer tokenizer = new RetainHatefulTermsNGramTokenizer();
//		NGramTokenizer tokenizer = new NGramTokenizer();
		tokenizer.setNGramMinSize(1);
		tokenizer.setNGramMaxSize(2);
		tokenizer.setDelimiters("[^0-9a-zA-ZäÄöÖüÜß]");
		tokenizer.setFilterUnigramsToo(false);
		tokenizer.setTokenFormatTypedDependencies(false);


		StringToWordVector filter = new StringToWordVector();
		//filter.setInputFormat(trainingInstances);

		filter.setTokenizer(tokenizer);
		filter.setWordsToKeep(1000000);
		filter.setDoNotOperateOnPerClassBasis(true);
		filter.setLowerCaseTokens(true);

		//Apply Stopwordlist
		WordsFromFile stopwords =new WordsFromFile();
		stopwords.setStopwords(new File("../stopwords.txt"));
		filter.setStopwordsHandler(stopwords);

		//Apply Stemmer
		SnowballStemmer stemmer= new SnowballStemmer("german");
		filter.setStemmer(stemmer);

		//Apply IDF-TF Weighting + DocLength-Normalization
		filter.setTFTransform(true);
		filter.setIDFTransform(true);
		filter.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));

		filter.setAttributeIndices("first");
		try {
			filter.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void filterTypedDependencies(){

//		RetainHatefulTermsNGramTokenizer nGramTokenizer = new RetainHatefulTermsNGramTokenizer();
		NGramTokenizer nGramTokenizer = new NGramTokenizer();
		nGramTokenizer.setNGramMinSize(1);
		nGramTokenizer.setNGramMaxSize(1);
		nGramTokenizer.setDelimiters("[ \\n]");
//		nGramTokenizer.setFilterUnigramsToo(false);
//		nGramTokenizer.setTokenFormatTypedDependencies(true);

		StringToWordVector stringToWordVectorFilter = new StringToWordVector();

		stringToWordVectorFilter.setTokenizer(nGramTokenizer);
		stringToWordVectorFilter.setAttributeIndices("4");
		stringToWordVectorFilter.setWordsToKeep(1000000);
		stringToWordVectorFilter.setLowerCaseTokens(true);

		try {
			stringToWordVectorFilter.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, stringToWordVectorFilter);
			System.out.println(trainingInstances.toSummaryString());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	private void attributSelectionFilter()
	{
		AttributeSelection attributeFilter = new AttributeSelection(); 
		 
        InfoGainAttributeEval ev = new InfoGainAttributeEval(); 
        Ranker ranker = new Ranker(); 
//      ranker.setNumToSelect(3410); 
        ranker.setNumToSelect(4500);
        
        attributeFilter.setEvaluator(ev); 
        attributeFilter.setSearch(ranker); 
        try {
			attributeFilter.setInputFormat(trainingInstances);
			trainingInstances=Filter.useFilter(trainingInstances, attributeFilter);
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
		try {

			Evaluation eval = new Evaluation(trainingInstances);
			eval.crossValidateModel(classifier, trainingInstances, 5, new Random(1));
			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			System.out.println("===== Evaluating on filtered (training) dataset done =====");
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

			System.out.println(classifier);
			System.out.println("===== Training on filtered (training) dataset done =====");
		}
		catch (Exception e) {
			System.out.println("Problem found when training");
		}
	}

	public static void main(String[] args) {
		JDBCFBCommentDAO daoFB= new JDBCFBCommentDAO();
		JDBCHSPostDAO daoHP= new JDBCHSPostDAO();

		List<Posting> trainingSamples = new ArrayList<Posting>();

		for(FBComment post: daoFB.getFBComments())
		{
			if(post.getResult()!=-1)
			{
				if(post.getResult()==0)
				{
					trainingSamples.add(new Posting(post.getMessage(), post.getTypedDependencies(), PostType.NEGATIVE));				
				}	
				else if(post.getResult()==1)
				{
					trainingSamples.add(new Posting(post.getMessage(), post.getTypedDependencies(), PostType.POSITIVE));
				}
			}

		}

		for(HatePost hatePost: daoHP.getAllPosts())
		{
			trainingSamples.add(new Posting(hatePost.getPost(), hatePost.getTypedDependencies(), PostType.POSITIVE));
		}

		WekaBowClassifier classifier=new WekaBowClassifier(trainingSamples,new SMO());
		classifier.evaluate();
		//classifier.learn();
	}

}
