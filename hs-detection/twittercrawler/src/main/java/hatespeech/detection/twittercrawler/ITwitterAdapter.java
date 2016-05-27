package hatespeech.detection.twittercrawler;

import java.util.Collection;

public interface ITwitterAdapter {

	public abstract long getMinedTweets();

	public abstract void trackKeywords(Collection<String> keywords);

	public abstract void stopTracking();

}