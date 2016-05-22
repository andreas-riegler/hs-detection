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
	private String description;
	private String caption;
	private String fullPicture;
	private boolean isExpired;
	private boolean isHidden;
	private boolean isPublished;
	private String link;
	private String name;
	private String permalinkUrl;
	private String statusType;
	private String timelineVisibility;
	private long reactionsCount;
	
	
	public FBPost(String id, long commentsCount, Date createdTime,
			String fromId, long likesCount, String message, long sharesCount,
			String type, String description, String caption,
			String fullPicture, boolean isExpired, boolean isHidden,
			boolean isPublished, String link, String name, String permalinkUrl,
			String statusType, String timelineVisibility, long reactionsCount) {
		super();
		this.id = id;
		this.commentsCount = commentsCount;
		this.createdTime = createdTime;
		this.fromId = fromId;
		this.likesCount = likesCount;
		this.message = message;
		this.sharesCount = sharesCount;
		this.type = type;
		this.description = description;
		this.caption = caption;
		this.fullPicture = fullPicture;
		this.isExpired = isExpired;
		this.isHidden = isHidden;
		this.isPublished = isPublished;
		this.link = link;
		this.name = name;
		this.permalinkUrl = permalinkUrl;
		this.statusType = statusType;
		this.timelineVisibility = timelineVisibility;
		this.reactionsCount = reactionsCount;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getFullPicture() {
		return fullPicture;
	}
	public void setFullPicture(String fullPicture) {
		this.fullPicture = fullPicture;
	}
	public boolean isExpired() {
		return isExpired;
	}
	public void setExpired(boolean isExpired) {
		this.isExpired = isExpired;
	}
	public boolean isHidden() {
		return isHidden;
	}
	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}
	public boolean isPublished() {
		return isPublished;
	}
	public void setPublished(boolean isPublished) {
		this.isPublished = isPublished;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPermalinkUrl() {
		return permalinkUrl;
	}
	public void setPermalinkUrl(String permalinkUrl) {
		this.permalinkUrl = permalinkUrl;
	}
	public String getStatusType() {
		return statusType;
	}
	public void setStatusType(String statusType) {
		this.statusType = statusType;
	}
	public String getTimelineVisibility() {
		return timelineVisibility;
	}
	public void setTimelineVisibility(String timelineVisibility) {
		this.timelineVisibility = timelineVisibility;
	}
	public long getReactionsCount() {
		return reactionsCount;
	}
	public void setReactionsCount(long reactionsCount) {
		this.reactionsCount = reactionsCount;
	}


	@Override
	public String toString() {
		return "FBPost [id=" + id + ", commentsCount=" + commentsCount
				+ ", createdTime=" + createdTime + ", fromId=" + fromId
				+ ", likesCount=" + likesCount + ", message=" + message
				+ ", sharesCount=" + sharesCount + ", type=" + type + "]";
	}
	
}
