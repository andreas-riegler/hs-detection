package hatespeech.detection.ml;

import hatespeech.detection.model.Feature;
import hatespeech.detection.model.FeatureVector;
import hatespeech.detection.model.PostType;
import hatespeech.detection.model.TrainingSample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.BinarySparseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class HatepostClassifier {

	private final Classifier classifier;
	private final Instances trainingInstances;
	private final ArrayList<Attribute> featureList;
	private final Map<String, Integer> featureIndexMap;
	
	public HatepostClassifier(List<TrainingSample> trainingSamples, Classifier algorithm){
		
		classifier = algorithm;
		featureList = createFeatureList(trainingSamples);
		featureIndexMap = initFeatureIndexMap(featureList);
		trainingInstances = createInstances("train", trainingSamples, featureList);

		train();
	}
	
	private Map<String, Integer> initFeatureIndexMap(ArrayList<Attribute> featureList) {
		Map<String, Integer> featureIndexMap = new HashMap<>();
		for (int i = 0; i < featureList.size() - 1; i++) {
			Attribute feature = featureList.get(i);
			featureIndexMap.put(feature.name(), i);
		}
		return featureIndexMap;
	}
	

	private ArrayList<Attribute> createFeatureList(Iterable<TrainingSample>trainingSamples)
	{
		
			Set<String> featureStrings = loadDistinctFeatures(trainingSamples);
			ArrayList<Attribute> featureList = new ArrayList<>();
			for (String feature : featureStrings) {
				featureList.add(new Attribute(feature));
			}

			List<String> hatepostResults = new ArrayList<String>();
			hatepostResults.add("negative");
			hatepostResults.add("positive");
			Attribute hatePostAttribute = new Attribute("__hatepost__",hatepostResults);
			featureList.add(hatePostAttribute);

			return featureList;
		
	}

	private Set<String> loadDistinctFeatures(Iterable<TrainingSample> trainingSamples)
	{
		Set<String> features = new HashSet<String>();
		for (TrainingSample trainingSample : trainingSamples) {
			FeatureVector featureVector = trainingSample.getFeatureVector();
			for (Feature feature : featureVector.getFeatures()) {
				features.add(feature.getnGram());
			}
		}
		return features;
	}

	private void train() {
		try {
			classifier.buildClassifier(trainingInstances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Factory method to create an {@code Instances} object from the given training samples.
	 */
	private Instances createInstances(String name,
	                                  List<TrainingSample> trainingSamples,
	                                  ArrayList<Attribute> featureList) {
		Instances instances = new Instances(name, featureList, trainingSamples.size());
		instances.setClassIndex(featureList.size() - 1);

		for (TrainingSample trainingSample : trainingSamples) {
			FeatureVector featureVector = trainingSample.getFeatureVector();
			PostType postType = trainingSample.getPostType();
			Instance instance = createInstance(instances, featureVector, postType);
			instances.add(instance);
		}

		return instances;
	}

	/**
	 * Factory method to create an {@code Instance} based on the given feature vector.
	 */
	private Instance createInstance(Instances instances, FeatureVector featureVector) {
		return createInstance(instances, featureVector, null);
	}

	/**
	 * Factory method to create an {@code Instance} based on the given feature vector and sentiment value.
	 */
	private Instance createInstance(Instances instances, FeatureVector featureVector, PostType postType) {
		BinarySparseInstance instance = new BinarySparseInstance(featureList.size());
		instance.setDataset(instances);

		for (Feature feature : featureVector.getFeatures()) {
			if (isUsedForClassification(feature.getnGram())) {
				instance.setValue(featureIndexMap.get(feature.getnGram()), 1.0);
			}
		}

		if (postType != null) {
			instance.setClassValue(intFromPostType(postType));
		}

		double[] defaultValues = new double[featureList.size()];
		instance.replaceMissingValues(defaultValues);

		return instance;
	}

	/**
	 * Returns true if the string represented by {@code token} is used for classification (i.e. if it is contained
	 * in the {@code featureList} or false if it should be ignored.
	 */
	private boolean isUsedForClassification(String token) {
		return featureIndexMap.get(token) != null;
	}

	private int intFromPostType(PostType postType) {
		if (postType == PostType.NEGATIVE) {
			return 0;
		} else {
			return 1;
		}
	}

	
	public PostType classify(FeatureVector featureVector)  {
		Instances instances = new Instances("live", featureList, 1);
		instances.setClassIndex(featureList.size() - 1);
		Instance instanceToClassify = createInstance(instances, featureVector);
		instances.add(instanceToClassify);

		double classification=0;
		try {
			classification = classifier.classifyInstance(instanceToClassify);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return postTypeFromClassification(classification);

	}

	private PostType postTypeFromClassification(double classification) {
		if (classification == 0) {
			return PostType.NEGATIVE;
		} else {
			return PostType.POSITIVE;
		}
	}

	public Evaluation evaluate(List<TrainingSample> testSamples)
	{
		Instances testInstances = createInstances("test", testSamples, featureList);
		Evaluation evaluate=null;
		try {	
			evaluate = new Evaluation(trainingInstances);
			evaluate.evaluateModel(classifier, testInstances);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return evaluate;
	}
}
