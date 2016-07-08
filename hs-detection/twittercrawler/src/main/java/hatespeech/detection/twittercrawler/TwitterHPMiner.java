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
		
		/*
		 * Einlesen der Liste von einschlägigen Suchwörten in Bezug auf Asyl, Immigranten
		 */
		try (FileReader file=new FileReader("../searchTermsTw.txt");){
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
		
		
		//Afd - Pegida Official - Gipsy105 - Einzelfall -Welt in Chaos - Hab die Nase Voll-German Observer-Hansson -Einzelfallbearbeiter-Aufbruch-HC Strache - schnauzesovoll-germandefenceleague-uwe becher - merkel muss weg- deutschland wehrt sich-lupus lotarius-end of days-mut zur wahrheit
		//vl #youarenotwelcome gutmenschen afd lügenpresse asylanten abmerkeln flüchtlinge einzelfall noislam willkomenskultur Krimigranten asylchaos asylantenpack
		//perlen aus freital - (hass hilft - rechts gegen rechts - hogesatzbau)
		/*
		long userids[] = {844081278L, 3130731489L, 3728419043L,4816230227L,4558206579L,4763025382L,3402505065L,156912564L,1590434754L,2970248351L,117052823L,1108250934L,712318748590473216L,1227192296L,701822420743749636L,3654016996L,2287293282L,4719457457L,4497623716L};
		for(long userid: userids)
			twCrawler.insertAllTweetsFromUser(userid);
		*/
		twStreamingAdapter.setUseFilterKeyWords(false);
		twStreamingAdapter.setToTrackUser(null);
		twStreamingAdapter.trackKeywords(keywords);
		
		do {
			Thread.sleep(60000);
			if (twStreamingAdapter.getMinedTweets() >= 50000) {
				twStreamingAdapter.stopTracking();
				Thread.sleep(2000);
				
			}
		} while (true);
	}

}
