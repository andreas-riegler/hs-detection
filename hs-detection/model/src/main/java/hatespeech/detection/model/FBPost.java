package hatespeech.detection.model;

import java.util.Date;

public class FBPost {

	private String id;
	private long commentsCount;
	private Date createdTime;
	private String fromId;
	private long likesCount;
	private String message;
	private long sharesCount;
	private String type;
		
	
	public FBPost(String id, long commentsCount, Date createdTime,
			String fromId, long likesCount, String message, long sharesCount,
			String type) {
		super();
		this.id = id;
		this.commentsCount = commentsCount;
		this.createdTime = createdTime;
		this.fromId = fromId;
		this.likesCount = likesCount;
		this.message = message;
		this.sharesCount = sharesCount;
		this.type = type;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getCommentsCount() {
		return commentsCount;
	}
	public void setCommentsCount(long commentsCount) {
		this.commentsCount = commentsCount;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	public String getFromId() {
		return fromId;
	}
	public void setFromId(String fromId) {
		this.fromId = fromId;
	}
	public long getLikesCount() {
		return likesCount;
	}
	public void setLikesCount(long likesCount) {
		this.likesCount = likesCount;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getSharesCount() {
		return sharesCount;
	}
	public void setSharesCount(long sharesCount) {
		this.sharesCount = sharesCount;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	
	@Override
	public String toString() {
		return "FBPost [id=" + id + ", commentsCount=" + commentsCount
				+ ", createdTime=" + createdTime + ", fromId=" + fromId
				+ ", likesCount=" + likesCount + ", message=" + message
				+ ", sharesCount=" + sharesCount + ", type=" + type + "]";
	}
	
}
