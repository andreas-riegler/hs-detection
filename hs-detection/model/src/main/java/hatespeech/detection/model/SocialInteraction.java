package hatespeech.detection.model;

public class SocialInteraction {
	private int retweets;
	private int follows;
	private int blocks;
	private int replies;
	private int mentions;
	private int favorites;

	public SocialInteraction() {
	}

	public SocialInteraction(int retweets, int follows, int blocks,
			int replies, int mentions, int favorites) {
		super();
		this.retweets = retweets;
		this.follows = follows;
		this.blocks = blocks;
		this.replies = replies;
		this.mentions = mentions;
		this.favorites = favorites;
	}

	public int getRetweets() {
		return retweets;
	}

	public void setRetweets(int retweets) {
		this.retweets = retweets;
	}

	public int getFollows() {
		return follows;
	}

	public void setFollows(int follows) {
		this.follows = follows;
	}

	public int getBlocks() {
		return blocks;
	}

	public void setBlocks(int blocks) {
		this.blocks = blocks;
	}

	public int getReplies() {
		return replies;
	}

	public void setReplies(int replies) {
		this.replies = replies;
	}

	public int getMentions() {
		return mentions;
	}

	public void setMentions(int mentions) {
		this.mentions = mentions;
	}

	public int getFavorites() {
		return favorites;
	}

	public void setFavorites(int favorites) {
		this.favorites = favorites;
	}

}
