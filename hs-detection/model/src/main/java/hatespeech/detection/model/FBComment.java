package hatespeech.detection.model;

import java.util.Date;

public class FBComment implements IImagePosting{

	private String id;
	private String postId;
	private Date createdTime;
	private long commentCount;
	private String fromId;
	private long likeCount;
	private String message;
	private String parentId;
	private boolean isHidden;
	//private boolean isPrivate;
	private String attachmentMediaImageSrc;
	private String typedDependencies;
	private int result;

	public FBComment(String id, String message, int result) {
		super();
		this.id = id;
		this.message = message;
		this.result = result;
	}

	public FBComment(String id, String postId, Date createdTime,
			long commentCount, String fromId, long likeCount, String message,
			String parentId, boolean isHidden,
			String attachmentMediaImageSrc) {
		super();
		this.id = id;
		this.postId = postId;
		this.createdTime = createdTime;
		this.commentCount = commentCount;
		this.fromId = fromId;
		this.likeCount = likeCount;
		this.message = message;
		this.parentId = parentId;
		this.isHidden = isHidden;
		this.attachmentMediaImageSrc = attachmentMediaImageSrc;
	}

	public FBComment(String id, String postId, Date createdTime,
			long commentCount, String fromId, long likeCount, String message,
			String parentId, boolean isHidden,
			String attachmentMediaImageSrc, String typedDependencies) {
		super();
		this.id = id;
		this.postId = postId;
		this.createdTime = createdTime;
		this.commentCount = commentCount;
		this.fromId = fromId;
		this.likeCount = likeCount;
		this.message = message;
		this.parentId = parentId;
		this.isHidden = isHidden;
		this.attachmentMediaImageSrc = attachmentMediaImageSrc;
		this.typedDependencies = typedDependencies;
	}

	public FBComment(String id, String postId, Date createdTime,
			long commentCount, String fromId, long likeCount, String message,
			String parentId, boolean isHidden, 
			String attachmentMediaImageSrc, String typedDependencies, int result) {
		super();
		this.id = id;
		this.postId = postId;
		this.createdTime = createdTime;
		this.commentCount = commentCount;
		this.fromId = fromId;
		this.likeCount = likeCount;
		this.message = message;
		this.parentId = parentId;
		this.isHidden = isHidden;
		this.attachmentMediaImageSrc = attachmentMediaImageSrc;
		this.typedDependencies = typedDependencies;
		this.result = result;
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
	public boolean isHidden() {
		return isHidden;
	}
	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}
	public String getAttachmentMediaImageSrc() {
		return attachmentMediaImageSrc;
	}
	public void setAttachmentMediaImageSrc(String attachmentMediaImageSrc) {
		this.attachmentMediaImageSrc = attachmentMediaImageSrc;
	}

	@Override
	public boolean equals(Object obj) {
		return this.id.equals(((FBComment)obj).id);
	}

	@Override
	public String toString() {
		return "FBComment [id=" + id + ", postId=" + postId + ", createdTime="
				+ createdTime + ", commentCount=" + commentCount + ", fromId="
				+ fromId + ", likeCount=" + likeCount + ", message=" + message
				+ ", parentId=" + parentId + ", isHidden=" + isHidden
				+ ", attachmentMediaImageSrc="
				+ attachmentMediaImageSrc + ", typedDependencies="
				+ typedDependencies + ", result=" + result + "]";
	}

	@Override
	public PostType getPostType() {
		switch(getResult()){
		case 0: return PostType.NEGATIVE;
		case 1: return PostType.POSITIVE;
		case 2: return PostType.POSITIVE;
		case 3: return PostType.POSITIVE;
		default: return null;
		}
	}

	@Override
	public String getImage() {
		return attachmentMediaImageSrc;
	}

}
