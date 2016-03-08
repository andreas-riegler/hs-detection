package hatespeech.detection.model;

public class Posting {

	private String message;
	private PostType postType;
	
	public Posting(String message, PostType postType)
	{
		this.message=message;
		this.postType=postType;
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
	
	
}
