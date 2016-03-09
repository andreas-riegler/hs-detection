package hatespeech.detection.ml;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.hsprocessor.SpellCorrector;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.HatePost;
import hatespeech.detection.model.PostType;
import hatespeech.detection.model.Posting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import weka.filters.unsupervised.attribute.StringToWordVector;

public class WekaBowClassifier {

	private Instances trainingInstances;
	private StringToWordVector filter;
	private Classifier classifier;
	private FilteredClassifier filteredClassifier;
	  
	public WekaBowClassifier(List<Posting> trainingSamples,Classifier classifier){

		this.classifier=classifier;
		trainingInstances=initializeInstances("train",trainingSamples);

		filter=initializeBOWFilter();
		filter.setAttributeIndices("first");

	}



	private Instances initializeInstances(String name,List<Posting> trainingSamples) {

		ArrayList<Attribute> featureList=new ArrayList<Attribute>();
		featureList.add(new Attribute("message",(List<String>)null));

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
			DenseInstance instance = createInstance(post.getMessage(),instances,rowSize);
			instance.setClassValue(post.getPostType().toString().toLowerCase());
			instances.add(instance);
		}	
	}

	/**
	 * Method that converts a text message into an instance.
	 */
	private DenseInstance createInstance(String text, Instances data,int rowSize) {

		// Create instance of length rowSize
		DenseInstance instance = new DenseInstance(rowSize);

		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);

		// Set value for message attribute
		Attribute messageAtt = data.attribute("message");
		instance.setValue(messageAtt, text);


		return instance;
	}

	private StringToWordVector initializeBOWFilter() {
		
		NGramTokenizer tokenizer = new NGramTokenizer();
		tokenizer.setNGramMinSize(1);
		tokenizer.setNGramMaxSize(1);
		tokenizer.setDelimiters("[^0-9a-zA-ZäÄöÖüÜß]");
		
		
		StringToWordVector filter = new StringToWordVector();
		//filter.setInputFormat(trainingInstances);

		filter.setTokenizer(tokenizer);
		filter.setWordsToKeep(1000000);
		//filter.setDoNotOperateOnPerClassBasis(true);
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
		
		return filter;
	}

	/**
	 * This method evaluates the classifier. As recommended by WEKA documentation,
	 * the classifier is defined but not trained yet. Evaluation of previously
	 * trained classifiers can lead to unexpected results.
	 */
	public void evaluate() {
		try {

			filteredClassifier = new FilteredClassifier();
			filteredClassifier.setFilter(filter);
			filteredClassifier.setClassifier(classifier);
			Evaluation eval = new Evaluation(trainingInstances);
			eval.crossValidateModel(filteredClassifier, trainingInstances, 4, new Random(1));
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
			filteredClassifier = new FilteredClassifier();
			filteredClassifier.setFilter(filter);
			filteredClassifier.setClassifier(classifier);
			filteredClassifier.buildClassifier(trainingInstances);

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
		List<Posting> testSamples=new ArrayList<Posting>();

		for(FBComment post: daoFB.getFBComments())
		{
			if(post.getResult()!=-1)
			{
				if(post.getResult()==0)
				{
					trainingSamples.add(new Posting(post.getMessage(),PostType.NEGATIVE));				
				}	
				else if(post.getResult()==1)
				{
					trainingSamples.add(new Posting(post.getMessage(),PostType.POSITIVE));
				}
			}

		}

		for(HatePost hatePost: daoHP.getAllPosts())
		{
			trainingSamples.add(new Posting(hatePost.getPost(),PostType.POSITIVE));
		}

		WekaBowClassifier classifier=new WekaBowClassifier(trainingSamples,new SMO());
		classifier.evaluate();
		classifier.learn();
		}

}
