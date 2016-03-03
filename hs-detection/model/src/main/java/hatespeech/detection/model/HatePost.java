package hatespeech.detection.model;

public class HatePost {
	
	private String Id,post,link;
	private int result;
	
	public HatePost()
	{
		
	}
	public HatePost(String Id, String post, String link) {
		this.Id=Id;
		this.post=post;
		this.link=link;
	}
	public HatePost(String Id, String post, String link, int result) {
		this.Id=Id;
		this.post=post;
		this.link=link;
		this.result=result;
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
	public int getResult(){
		return result;
	}
	public void setResult(int result){
		this.result = result;
	}
	
	public String toString()
	{
		return Id+" "+post+" "+link;
		
	}
	
	
}
