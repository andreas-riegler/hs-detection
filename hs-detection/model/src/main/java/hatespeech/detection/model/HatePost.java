package hatespeech.detection.model;

public class HatePost {
	
	private String Id,post,link;
	
	public HatePost()
	{
		
	}
	public HatePost(String Id, String post, String link) {
		this.Id=Id;
		this.post=post;
		this.link=link;
	}
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getPost() {
		return post;
	}
	public void setPost(String post) {
		this.post = post;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String toString()
	{
		return Id+" "+post+" "+link;
		
	}
	
	
}
