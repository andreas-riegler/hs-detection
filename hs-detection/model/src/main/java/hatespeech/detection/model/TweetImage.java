package hatespeech.detection.model;

public class TweetImage {

	private Integer imageid;
	private String url;
	private Tweet tweet;
	
	public TweetImage(){
		
	}
	public TweetImage(int id, String url,Tweet tweet) {
		this.imageid=id;
		this.url=url;
		this.tweet=tweet;
	}
	public Integer getImageid() {
		return imageid;
	}
	public void setImageid(Integer imageid) {
		this.imageid = imageid;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Tweet getTweet() {
		return tweet;
	}
	public void setTweet(Tweet tweet) {
		this.tweet = tweet;
	}
	
	
}
