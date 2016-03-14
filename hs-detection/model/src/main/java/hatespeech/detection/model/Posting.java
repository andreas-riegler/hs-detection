package hatespeech.detection.model;

public class Posting {

	private String message;
	private String typedDependencies;
	private PostType postType;
	

	public Posting(String message, String typedDependencies, PostType postType) {
		super();
		this.message = message;
		this.typedDependencies = typedDependencies;
		this.postType = postType;
	}
	
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public PostType getPostType() {
		return postType;
	}
	public void setPostType(PostType postType) {
		this.postType = postType;
	}
	public String getTypedDependencies() {
		return typedDependencies;
	}
	public void setTypedDependencies(String typedDependencies) {
		this.typedDependencies = typedDependencies;
	}
	
	@Override
	public String toString() {
		return "Posting [message=" + message + ", typedDependencies="
				+ typedDependencies + ", postType=" + postType + "]";
	}
}
