package hatespeech.detection.model;

public class TrainingSample {

	private FeatureVector featureVector;
	private PostType postType;

	public TrainingSample(FeatureVector featureVector, PostType postType) {
		this.featureVector = featureVector;
		this.postType = postType;
	}

	public FeatureVector getFeatureVector() {
		return featureVector;
	}

	public PostType getPostType() {
		return postType;
	}
}
