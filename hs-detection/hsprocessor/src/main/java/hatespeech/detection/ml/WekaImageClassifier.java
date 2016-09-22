package hatespeech.detection.ml;

import hatespeech.detection.hsprocessor.ImageFeatureExtractor;
import hatespeech.detection.model.IImagePosting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class WekaImageClassifier {

	private List<IImagePosting> trainingSamples;
	private Instances trainingInstances = null;
	private ArrayList<Attribute> featureList = null;
	private Classifier classifier;

	private boolean useSurfFeatureVector = true;

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


	private void init(){
		trainingInstances = initializeInstances("train", trainingSamples);

		//Reihenfolge wichtig
		//		if(useSurfFeatureVector)
		//		{
		//			initializeCharacterBow();
		//		}
	}

	private Instances initializeInstances(String name, List<IImagePosting> trainingSamples) {

		featureList=new ArrayList<Attribute>();

		if(useSurfFeatureVector){
			for(int i = 0; i < ImageFeatureExtractor.getSurfFeatureVectorCount(); i++){
				featureList.add(new Attribute("surfFV" + (i+1)));
			}			
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
