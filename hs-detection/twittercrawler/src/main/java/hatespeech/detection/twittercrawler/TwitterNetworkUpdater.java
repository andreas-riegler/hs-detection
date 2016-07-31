package hatespeech.detection.twittercrawler;

import hatespeech.detection.dao.JDBCTwitterDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TwitterNetworkUpdater {

	public static void main(String[] args) {
		
		TwitterCrawler twCraw=new TwitterCrawler();
		JDBCTwitterDAO daoTW= new JDBCTwitterDAO();
		
		List<Long> classifiedTweetIds=new ArrayList<Long>();
		
//		daoTW.getClassifiedTweets().stream()
//		.forEach(c -> classifiedTweetIds.add(c.getTweetid()));
		
		//twCraw.insertLikesRetweetsFromTweets(classifiedTweetIds);
		
		//Afd - Pegida Official - Gipsy105 - Einzelfall -Welt in Chaos - Hab die Nase Voll-German Observer-Hansson -Einzelfallbearbeiter-Aufbruch-HC Strache - schnauzesovoll-germandefenceleague-uwe becher - merkel muss weg- deutschland wehrt sich-lupus lotarius-end of days-mut zur wahrheit
		Long userids[] = {844081278L, 3130731489L, 3728419043L,4816230227L,4558206579L,4763025382L,3402505065L,156912564L,1590434754L,2970248351L,117052823L,1108250934L,712318748590473216L,1227192296L,701822420743749636L,3654016996L,2287293282L,4719457457L,4497623716L};
		
		twCraw.insertFollowingUsersByUserIDs(Arrays.<Long>asList(userids));

	}

}
