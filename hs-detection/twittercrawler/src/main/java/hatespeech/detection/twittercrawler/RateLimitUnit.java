package hatespeech.detection.twittercrawler;

public class RateLimitUnit {

	private String family;
	private String resource;
	private int remainingRequests;
	private long resetTime;
	
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public int getRemainingRequests() {
		return remainingRequests;
	}
	public void setRemainingRequests(int remainingRequest) {
		this.remainingRequests = remainingRequest;
	}
	public long getResetTime() {
		return resetTime;
	}
	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
	}
}
