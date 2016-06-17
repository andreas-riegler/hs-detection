package hatespeech.detection.model;

public class HatePost implements IPosting{
	
	private String Id, internId, post, link, typedDependencies;
	private int result;
	
	public HatePost()
	{
		
	}
	public HatePost(String Id, String internId,String post, String link) {
		this.Id=Id;
		this.internId=internId;
		this.post=post;
		this.link=link;
	}
	public HatePost(String Id, String internId,String post, String link, int result) {
		this.Id=Id;
		this.internId=internId;
		this.post=post;
		this.link=link;
		this.result=result;
	}
	
	public HatePost(String Id, String internId, String post, String link, String typedDependencies, int result) {
		super();
		this.Id = Id;
		this.internId = internId;
		this.post = post;
		this.link = link;
		this.typedDependencies = typedDependencies;
		this.result = result;
	}
	
	public String getInternId() {
		return internId;
	}
	public void setInternId(String internId) {
		this.internId = internId;
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
	public String getTypedDependencies() {
		return typedDependencies;
	}
	public void setTypedDependencies(String typedDependencies) {
		this.typedDependencies = typedDependencies;
	}
	
	
	@Override
	public String toString() {
		return "HatePost [Id=" + Id + ", internId=" + internId + ", post="
				+ post + ", link=" + link + ", typedDependencies="
				+ typedDependencies + ", result=" + result + "]";
	}
	
	@Override
	public String getMessage() {
		return getPost();
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
}
