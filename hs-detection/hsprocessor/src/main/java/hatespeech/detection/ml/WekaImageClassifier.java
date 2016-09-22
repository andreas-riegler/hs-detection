package hatespeech.detection.ml;

import hatespeech.detection.hsprocessor.FeatureExtractor;
import hatespeech.detection.hsprocessor.ImageFeatureExtractor;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.IImagePosting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;

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

	public WekaImageClassifier(List<IImagePosting> trainingSamples, Classifier classifier){
		this.classifier=classifier;
		this.trainingSamples = trainingSamples;
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
		
		return instance;

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
}
