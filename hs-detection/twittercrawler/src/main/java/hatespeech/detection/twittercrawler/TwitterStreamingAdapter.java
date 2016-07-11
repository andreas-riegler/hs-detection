package hatespeech.detection.twittercrawler;

import hatespeech.detection.dao.JDBCTwitterDAO;
import hatespeech.detection.model.Hashtag;
import hatespeech.detection.model.Tweet;
import hatespeech.detection.model.User;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterStreamingAdapter implements ITwitterAdapter {
	private JDBCTwitterDAO twDao;
	private TwitterStream stream;
	private long minedTweets;
	private boolean quit;
	private ConfigurationBuilder cb;
	private long[] toTrackUser = {844081278L, 3130731489L, 3728419043L};;
	
	private boolean useFilterKeyWords=false;
	private TwitterCrawler twCraw=new TwitterCrawler();

	public TwitterStreamingAdapter() {
		cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("sGHln59z8uDnNbOExZpihqSWA");
		cb.setOAuthConsumerSecret("j5uquWuzrZ66srXhatPCnhZJHgvReZgmcjGkXYug8UtqwQ3agp");
		cb.setOAuthAccessToken("2846620079-0kOUwMHsdR6ubYvtKB6UGrfqukNOVXS150Gfsum");
		cb.setOAuthAccessTokenSecret("s9QMgq9on5lkwGmQjEeYDz5nppKqtAwtYptDbH0r0Cx0e");
		twDao=new JDBCTwitterDAO();
	}
	@Override
	public long getMinedTweets() {
		return minedTweets;
	}
	public void setToTrackUser(long[] userids) {
		this.toTrackUser=userids;
		
	}
	
	public boolean isUseFilterKeyWords() {
		return useFilterKeyWords;
	}
	public void setUseFilterKeyWords(boolean useFilterKeyWords) {
		this.useFilterKeyWords = useFilterKeyWords;
	}
	@Override
	public void trackKeywords(Collection<String> keywords) {
		quit = false;
		stream = new TwitterStreamFactory(cb.build()).getInstance();
		minedTweets = 0;
		StatusListener listener = new StatusListener() {
			@SuppressWarnings("serial")
			@Override
			public void onStatus(Status s) {
				
				if (!quit) {
					Tweet tweet = twCraw.fillTweet(s);

					twCraw.insertTweet(tweet);
					minedTweets++;
				}
			}

			

			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:"
						+ statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:"
						+ numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId
						+ " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		stream.addListener(listener);
		
		
		FilterQuery f = new FilterQuery();
		if(useFilterKeyWords)
			f.track(keywords.toArray(new String[0]));
		f.follow(toTrackUser);
		f.language(new String[] { "de" });
		stream.filter(f);
		
	}

	@Override
	public void stopTracking() {
		quit = true;
		stream.cleanUp();
		stream.shutdown();
		stream.clearListeners();
	}
	
}
