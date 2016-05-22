package hatespeech.detection.model;

import java.util.Date;

public class FBComment {

	private String id;
	private String postId;
	private Date createdTime;
	private long commentCount;
	private String fromId;
	private long likeCount;
	private String message;
	private String parentId;
	private boolean isHidden;
	private boolean isPrivate;
	private String attachmentMediaImageSrc;
	private String typedDependencies;
	private int result;
	

	public FBComment(String id, String postId, Date createdTime,
			long commentCount, String fromId, long likeCount, String message,
			String parentId) {
		super();
		this.id = id;
		this.postId = postId;
		this.createdTime = createdTime;
		this.commentCount = commentCount;
		this.fromId = fromId;
		this.likeCount = likeCount;
		this.message = message;
		this.parentId = parentId;
	}

	public FBComment(String id, String postId, Date createdTime,
			long commentCount, String fromId, long likeCount, String message,
			String parentId, String typedDependencies, int result) {
		super();
		this.id = id;
		this.postId = postId;
		this.createdTime = createdTime;
		this.commentCount = commentCount;
		this.fromId = fromId;
		this.likeCount = likeCount;
		this.message = message;
		this.parentId = parentId;
		this.typedDependencies = typedDependencies;
		this.result = result;
	}
	
	public FBComment(String id, String postId, Date createdTime,
			long commentCount, String fromId, long likeCount, String message,
			String parentId, String typedDependencies) {
		super();
		this.id = id;
		this.postId = postId;
		this.createdTime = createdTime;
		this.commentCount = commentCount;
		this.fromId = fromId;
		this.likeCount = likeCount;
		this.message = message;
		this.parentId = parentId;
		this.typedDependencies = typedDependencies;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPostId() {
		return postId;
	}
	public void setPostId(String postId) {
		this.postId = postId;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	public long getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(long commentCount) {
		this.commentCount = commentCount;
	}
	public String getFromId() {
		return fromId;
	}
	public void setFromId(String fromId) {
		this.fromId = fromId;
	}
	public long getLikeCount() {
		return likeCount;
	}
	public void setLikeCount(long likeCount) {
		this.likeCount = likeCount;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getTypedDependencies() {
		return typedDependencies;
	}
	public void setTypedDependencies(String typedDependencies) {
		this.typedDependencies = typedDependencies;
	}
	public int getResult(){
		return result;
	}
	public void setResult(int result){
		this.result = result;
	}

	
	@Override
	public String toString() {
		return "FBComment [id=" + id + ", postId=" + postId + ", createdTime="
				+ createdTime + ", commentCount=" + commentCount + ", fromId="
				+ fromId + ", likeCount=" + likeCount + ", message=" + message
				+ ", parentId=" + parentId + ", typedDependencies="
				+ typedDependencies + ", result=" + result + "]";
	}
	
}
