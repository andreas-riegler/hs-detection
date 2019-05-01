package hatespeech.detection.ml;

import hatespeech.detection.hsprocessor.FeatureExtractor;
import hatespeech.detection.hsprocessor.ImageFeatureExtractor;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.IImagePosting;
import hatespeech.detection.model.IPosting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bytedeco.javacv.FrameGrabber.ImageMode;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class WekaImageClassifier {

	private static double WEKA_MISSING_VALUE = Utils.missingValue();

	private List<IImagePosting> trainingSamples;
	private Instances trainingInstances = null;
	private ArrayList<Attribute> featureList = null;
	private Classifier classifier;

	//surf features
	private boolean useSurfFeatureVector = false;

	//global features
	private boolean useGlobalFeatureVectors = false;
	private List<GlobalFeature> globalFeaturesList;

	//Facebook features settings
	private boolean useFBPostReactionType = false;
	private boolean useFBCommentCount = false;
	private boolean useFBLikeCount = false;
	private boolean useFBFractionOfUserReactionOnTotalReactions = false;

	//DeepConvolutionalNeuralNetwork settings
	private boolean useDeepConvolutionalNeuralNetworkCaffeNet = false;
	private boolean useDeepConvolutionalNeuralNetworkGoogleNet = false;
	private boolean useDeepConvolutionalNeuralNetworkResNet = false;

	private StringToWordVector stwvResNet;

	private StringToWordVector stwvGoogleNet;

	private StringToWordVector stwvCaffeNet;

	public WekaImageClassifier(List<IImagePosting> trainingSamples, Classifier classifier){
		this.classifier=classifier;
		this.trainingSamples = trainingSamples;
	}


	public boolean isUseDeepConvolutionalNeuralNetworkResNet() {
		return useDeepConvolutionalNeuralNetworkResNet;
	}
	public void setUseDeepConvolutionalNeuralNetworkResNet(
			boolean useDeepConvolutionalNeuralNetworkResNet) {
		this.useDeepConvolutionalNeuralNetworkResNet = useDeepConvolutionalNeuralNetworkResNet;
	}
	public boolean isUseDeepConvolutionalNeuralNetworkCaffeNet() {
		return useDeepConvolutionalNeuralNetworkCaffeNet;
	}
	public void setUseDeepConvolutionalNeuralNetworkCaffeNet(
			boolean useDeepConvolutionalNeuralNetworkCaffeNet) {
		this.useDeepConvolutionalNeuralNetworkCaffeNet = useDeepConvolutionalNeuralNetworkCaffeNet;
	}
	public boolean isUseDeepConvolutionalNeuralNetworkGoogleNet() {
		return useDeepConvolutionalNeuralNetworkGoogleNet;
	}
	public void setUseDeepConvolutionalNeuralNetworkGoogleNet(
			boolean useDeepConvolutionalNeuralNetworkGoogleNet) {
		this.useDeepConvolutionalNeuralNetworkGoogleNet = useDeepConvolutionalNeuralNetworkGoogleNet;
	}
	public boolean isUseSurfFeatureVector() {
		return useSurfFeatureVector;
	}
	public void setUseSurfFeatureVector(boolean useSurfFeatureVector) {
		this.useSurfFeatureVector = useSurfFeatureVector;
	}
	public boolean isUseFBPostReactionType() {
		return useFBPostReactionType;
	}
	public void setUseFBPostReactionType(boolean useFBPostReactionType) {
		this.useFBPostReactionType = useFBPostReactionType;
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
	public boolean isUseGlobalFeatureVectors() {
		return useGlobalFeatureVectors;
	}
	public void setUseGlobalFeatureVectors(boolean useGlobalFeatureVectors) {
		this.useGlobalFeatureVectors = useGlobalFeatureVectors;
	}
	public List<GlobalFeature> getGlobalFeaturesList() {
		return globalFeaturesList;
	}
	public void setGlobalFeaturesList(List<GlobalFeature> globalFeaturesList) {
		this.globalFeaturesList = globalFeaturesList;
	}
	public void setUseFBLikeCount(boolean useFBLikeCount) {
		this.useFBLikeCount = useFBLikeCount;
	}
	public boolean isUseFBFractionOfUserReactionOnTotalReactions() {
		return useFBFractionOfUserReactionOnTotalReactions;
	}


	public void setUseFBFractionOfUserReactionOnTotalReactions(
			boolean useFBFractionOfUserReactionOnTotalReactions) {
		this.useFBFractionOfUserReactionOnTotalReactions = useFBFractionOfUserReactionOnTotalReactions;
	}


	private void init(){
		trainingInstances = initializeInstances("train", trainingSamples);

		if(useDeepConvolutionalNeuralNetworkCaffeNet){
			initializeDeepConvolutionalNeuralNetworkCaffeNetBOW();
		}
		if(useDeepConvolutionalNeuralNetworkGoogleNet){
			initializeDeepConvolutionalNeuralNetworkGoogleNetBOW();
		}
		if(useDeepConvolutionalNeuralNetworkResNet){
			initializeDeepConvolutionalNeuralNetworkResNetBOW();
		}

		//reorder class attribute
		setClassAttributeAsLastIndex();
	}

	private Instances initializeInstances(String name, List<IImagePosting> trainingSamples) {

		featureList=new ArrayList<Attribute>();

		if(useSurfFeatureVector){
			for(int i = 0; i < ImageFeatureExtractor.getSurfFeatureVectorCount(); i++){
				featureList.add(new Attribute("surfFV" + (i+1)));
			}			
		}

		if(useGlobalFeatureVectors){
			for(String featureName : ImageFeatureExtractor.getGlobalFeatureVectors(trainingSamples.get(0), globalFeaturesList).keySet()){
				featureList.add(new Attribute(featureName));
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

		if(useFBFractionOfUserReactionOnTotalReactions){
			featureList.add(new Attribute("fbFractionOfUserReactionOnTotalReactions"));
		}

		if(useDeepConvolutionalNeuralNetworkCaffeNet){
			featureList.add(new Attribute("caffeNet",(List<String>)null));
		}

		if(useDeepConvolutionalNeuralNetworkGoogleNet){
			featureList.add(new Attribute("googleNet",(List<String>)null));
		}
		
		if(useDeepConvolutionalNeuralNetworkResNet){
			featureList.add(new Attribute("resNet",(List<String>)null));
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

	private void updateData(List<IImagePosting> trainingSamples, Instances instances, int rowSize) {

		for(IImagePosting posting : trainingSamples)
		{
			DenseInstance instance = createInstance(posting, instances, rowSize);
			instance.setClassValue(posting.getPostType().toString().toLowerCase());
			instances.add(instance);
		}	
	}

	/**
	 * Method that converts a text message into an instance.
	 */
	private DenseInstance createInstance(IImagePosting posting, Instances data, int rowSize) {

		// Create instance of length rowSize
		DenseInstance instance = new DenseInstance(rowSize);

		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);

		if(useSurfFeatureVector){
			// Set values for SURF Feature Vector attribute
			for(Map.Entry<String, Double> entry: ImageFeatureExtractor.getSurfFeatureVector(posting).entrySet()){
				Attribute surfFeatureVectorAttr = data.attribute(entry.getKey());
				instance.setValue(surfFeatureVectorAttr, entry.getValue());
			}			
		}

		if(useGlobalFeatureVectors){
			for(Map.Entry<String, Double> entry : ImageFeatureExtractor.getGlobalFeatureVectors(posting, globalFeaturesList).entrySet()){
				Attribute globalFeatureVectorAttr = data.attribute(entry.getKey());
				instance.setValue(globalFeatureVectorAttr, entry.getValue());
			}
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
		if(useFBFractionOfUserReactionOnTotalReactions){
			Attribute fractionOfUserReactionOnTotalReactionsAtt = data.attribute("fbFractionOfUserReactionOnTotalReactions");

			if(posting instanceof FBComment){
				String reaction = FeatureExtractor.getFBReactionByFBComment((FBComment) posting);
				if(reaction.equals("NONE")){
					instance.setValue(fractionOfUserReactionOnTotalReactionsAtt, 0);
				}
				else{
					instance.setValue(fractionOfUserReactionOnTotalReactionsAtt, FeatureExtractor.getFBFractionOfUserReactionOnTotalReactions((FBComment) posting));
				}
			}
			else{
				instance.setValue(fractionOfUserReactionOnTotalReactionsAtt, WEKA_MISSING_VALUE);
			}
		}

		if(useDeepConvolutionalNeuralNetworkCaffeNet){
			Attribute caffeNetAtt = data.attribute("caffeNet");
			instance.setValue(caffeNetAtt, ImageFeatureExtractor.getDeepConvolutionalNeuralNetworkImageFeatures(
					posting.getId(),
					ImageFeatureExtractor.DeepConvolutionalNeuralNetworkModelType.CAFFE_NET ,
					ImageFeatureExtractor.DeepConvolutionalNeuralNetworkFeatureType.TOP_5));
		}

		if(useDeepConvolutionalNeuralNetworkGoogleNet){
			Attribute googleNetAtt = data.attribute("googleNet");
			instance.setValue(googleNetAtt, ImageFeatureExtractor.getDeepConvolutionalNeuralNetworkImageFeatures(
					posting.getId(),
					ImageFeatureExtractor.DeepConvolutionalNeuralNetworkModelType.GOOGLE_NET ,
					ImageFeatureExtractor.DeepConvolutionalNeuralNetworkFeatureType.TOP_5));
		}
		
		if(useDeepConvolutionalNeuralNetworkResNet){
			Attribute resNetAtt = data.attribute("resNet");
			instance.setValue(resNetAtt, ImageFeatureExtractor.getDeepConvolutionalNeuralNetworkImageFeatures(
					posting.getId(),
					ImageFeatureExtractor.DeepConvolutionalNeuralNetworkModelType.RES_NET ,
					ImageFeatureExtractor.DeepConvolutionalNeuralNetworkFeatureType.TOP_5));
		}

		return instance;

	}

	private void initializeDeepConvolutionalNeuralNetworkCaffeNetBOW() {
		stwvCaffeNet = new StringToWordVector();
		NGramTokenizer tokenizer = new NGramTokenizer();

		tokenizer.setNGramMinSize(1);
		tokenizer.setNGramMaxSize(1);
		tokenizer.setDelimiters(" ");

		stwvCaffeNet.setTokenizer(tokenizer);
		stwvCaffeNet.setWordsToKeep(1000000);
		stwvCaffeNet.setLowerCaseTokens(true);

		stwvCaffeNet.setAttributeNamePrefix("caffeNet_");

		Integer columnIndex = trainingInstances.attribute("caffeNet").index()+1;
		stwvCaffeNet.setAttributeIndices(columnIndex.toString());

		try {
			stwvCaffeNet.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, stwvCaffeNet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeDeepConvolutionalNeuralNetworkGoogleNetBOW() {
		stwvGoogleNet = new StringToWordVector();
		NGramTokenizer tokenizer = new NGramTokenizer();

		tokenizer.setNGramMinSize(1);
		tokenizer.setNGramMaxSize(1);
		tokenizer.setDelimiters(" ");

		stwvGoogleNet.setTokenizer(tokenizer);
		stwvGoogleNet.setWordsToKeep(1000000);
		stwvGoogleNet.setLowerCaseTokens(true);

		stwvGoogleNet.setAttributeNamePrefix("googleNet_");

		Integer columnIndex = trainingInstances.attribute("googleNet").index()+1;
		stwvGoogleNet.setAttributeIndices(columnIndex.toString());

		try {
			stwvGoogleNet.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, stwvGoogleNet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initializeDeepConvolutionalNeuralNetworkResNetBOW() {
		stwvResNet = new StringToWordVector();
		NGramTokenizer tokenizer = new NGramTokenizer();

		tokenizer.setNGramMinSize(1);
		tokenizer.setNGramMaxSize(1);
		tokenizer.setDelimiters(" ");

		stwvResNet.setTokenizer(tokenizer);
		stwvResNet.setWordsToKeep(1000000);
		stwvResNet.setLowerCaseTokens(true);

		stwvResNet.setAttributeNamePrefix("resNet_");

		Integer columnIndex = trainingInstances.attribute("resNet").index()+1;
		stwvResNet.setAttributeIndices(columnIndex.toString());

		try {
			stwvResNet.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, stwvResNet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * sets the attribute at the given col index as the new class attribute, i.e.
	 * it moves it to the end of the attributes
	 * 
	 * @param columnIndex		the index of the column
	 */
	private void setClassAttributeAsLastIndex() {
		Reorder     reorder;
		String      order;
		int         classColumnIndex, i;

		classColumnIndex = trainingInstances.attribute("__hatepost__").index() + 1;

		try {
			// build order string (1-based!)
			order = "";
			for (i = 1; i < trainingInstances.numAttributes() + 1; i++) {
				// skip new class
				if (i == classColumnIndex)
					continue;

				if (!order.equals(""))
					order += ",";
				order += Integer.toString(i);
			}
			if (!order.equals(""))
				order += ",";
			order += Integer.toString(classColumnIndex);

			// process data
			reorder = new Reorder();
			reorder.setAttributeIndices(order);
			reorder.setInputFormat(trainingInstances);
			trainingInstances = Filter.useFilter(trainingInstances, reorder);

			// set class index
			trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Instances setClassAttributeAsLastIndex(Instances instances) {
		Reorder     reorder;
		String      order;
		int         classColumnIndex = 0, i;

		System.out.println("class column index " + classColumnIndex);
		classColumnIndex = instances.attribute("__hatepost__").index() + 1;
		System.out.println("class column index " + classColumnIndex);

		
		try {
			// build order string (1-based!)
			order = "";
			for (i = 1; i < instances.numAttributes() + 1; i++) {
				// skip new class
				if (i == classColumnIndex)
					continue;

				if (!order.equals(""))
					order += ",";
				order += Integer.toString(i);
			}
			if (!order.equals(""))
				order += ",";
			order += Integer.toString(classColumnIndex);
		
			// process data
			reorder = new Reorder();
			reorder.setAttributeIndices(order);
			reorder.setInputFormat(instances);
			instances = Filter.useFilter(instances, reorder);

			// set class index
			instances.setClassIndex(instances.numAttributes() - 1);
			System.out.println("class index is " + instances.classIndex());
			System.out.println("hatepost index " + (instances.attribute("__hatepost__").index() + 1));
			
			return instances;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method evaluates the classifier. As recommended by WEKA documentation,
	 * the classifier is defined but not trained yet. Evaluation of previously
	 * trained classifiers can lead to unexpected results.
	 */
	public void evaluate() {

		if(trainingInstances == null){
			long startTimeExtraction=System.currentTimeMillis();
			init();
			long endTimeExtraction=System.currentTimeMillis();
			System.out.println((double)(endTimeExtraction-startTimeExtraction)/1000+"s Feature-Extraktion");
		}

		try {
			Evaluation eval = new Evaluation(trainingInstances);
			long startTimeEvaluation=System.currentTimeMillis();
			eval.crossValidateModel(classifier, trainingInstances, 10, new Random(1));
			long endTimeEvaluation=System.currentTimeMillis();

			System.out.println((double)(endTimeEvaluation-startTimeEvaluation)/1000+"s Evaluationszeit");

			System.out.println(eval.toSummaryString());
			System.out.println(eval.toClassDetailsString());
			//System.out.println(trainingInstances.toSummaryString());

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

			if(trainingInstances == null){
				init();
			}

			classifier.buildClassifier(trainingInstances);

			//System.out.println(classifier);
			System.out.println("===== Training on filtered (training) dataset done =====");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	public Double classify(IImagePosting posting)  {
		Instances testInstances = new Instances("live", featureList, 1);
		testInstances.setClassIndex(featureList.size() - 1);
		DenseInstance instanceToClassify = createInstance(posting, testInstances, featureList.size());
		instanceToClassify.setClassMissing();
		testInstances.add(instanceToClassify);
		
		try {
			if(useDeepConvolutionalNeuralNetworkCaffeNet)
				testInstances=Filter.useFilter(testInstances, stwvCaffeNet);
			if(useDeepConvolutionalNeuralNetworkGoogleNet)
				testInstances=Filter.useFilter(testInstances, stwvGoogleNet);
			if(useDeepConvolutionalNeuralNetworkResNet)
				testInstances=Filter.useFilter(testInstances, stwvResNet);

		} catch (Exception e1) {
			System.out.println("ex1: " + e1.getMessage());
			e1.printStackTrace();
		}

		Double classification = null;
		try {
//			System.out.println("get: " + testInstances.get(0));
			classification = classifier.classifyInstance(testInstances.get(0));
		} catch (Exception e) {
			System.out.println("ex: " + e.getMessage());
			e.printStackTrace();
		}
		return classification;

	}
	
	public Instances buildInstances(List<IImagePosting> postings)  {
		Instances testInstances = new Instances("live", featureList, 1);
		testInstances.setClassIndex(featureList.size() - 1);
		
		for(IImagePosting posting : postings){
			DenseInstance instanceToClassify = createInstance(posting, testInstances, featureList.size());
			instanceToClassify.setClassValue(posting.getPostType().getValue());
			testInstances.add(instanceToClassify);
		}
		
		try {
			if(useDeepConvolutionalNeuralNetworkCaffeNet)
				testInstances=Filter.useFilter(testInstances, stwvCaffeNet);
			if(useDeepConvolutionalNeuralNetworkGoogleNet)
				testInstances=Filter.useFilter(testInstances, stwvGoogleNet);
			if(useDeepConvolutionalNeuralNetworkResNet)
				testInstances=Filter.useFilter(testInstances, stwvResNet);

			testInstances = setClassAttributeAsLastIndex(testInstances);

		} catch (Exception e1) {
			System.out.println("ex1: " + e1.getMessage());
			e1.printStackTrace();
		}

		return testInstances;
	}

	public void saveInstancesToArff(){
		ArffSaver saver = new ArffSaver();
		saver.setInstances(trainingInstances);
		try {
			saver.setFile(new File("hatespeech_images.arff"));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveInstancesToArff(Instances instances, String fileName){
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		try {
			saver.setFile(new File(fileName + ".arff"));
			//saver.setDestination(new File("./data/test.arff"));   // **not** necessary in 3.5.4 and later
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
