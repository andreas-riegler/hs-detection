package hatespeech.detection.model;

public class FBReaction {

	private String postId;
	private String userId;
	private String type;
	
	public FBReaction(String postId, String userId, String type) {
		super();
		this.postId = postId;
		this.userId = userId;
		this.type = type;
	}

	public String getPostId() {
		return postId;
	}
	public void setPostId(String postId) {
		this.postId = postId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
