package hatespeech.detection.twittercrawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class TwitterHPMiner {

	public static void main(String[] args) throws InterruptedException {
		TwitterStreamingAdapter twStreamingAdapter=new TwitterStreamingAdapter();
		TwitterCrawler twCrawler=new TwitterCrawler();
		List<String> keywords=new ArrayList<String>();
		BufferedReader buffReader=null;
		
		try (FileReader file=new FileReader("../searchTermsTw_neutral.txt");){
			buffReader=new BufferedReader(file);
			String keyword;
			while((keyword=buffReader.readLine())!=null)
			{
				keywords.add(keyword);
			}
			buffReader.close();
			file.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		twStreamingAdapter.setUseFilterKeyWords(true);
		twStreamingAdapter.setToTrackUser(null);
		twStreamingAdapter.trackKeywords(keywords);
		
		do {
			Thread.sleep(60000);
			if (twStreamingAdapter.getMinedTweets() >= 5000) {
				twStreamingAdapter.stopTracking();
				Thread.sleep(2000);
				
			}
		} while (true);
	}

}
