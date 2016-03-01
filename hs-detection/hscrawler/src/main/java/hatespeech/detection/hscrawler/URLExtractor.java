package hatespeech.detection.hscrawler;


import hatespeech.detection.dao.CreateTable;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.model.HatePost;
import hatespeech.detection.service.DatabaseConnector;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;


public class URLExtractor {

	private List<HatePost> hatePosts=new ArrayList<HatePost>();
	private JDBCHSPostDAO postDAO=new JDBCHSPostDAO();
	
	public List<HatePost> getHatePosts() {
		return hatePosts;
	}
	
	public void setHatePosts(List<HatePost> hatePosts) {
		this.hatePosts = hatePosts;
	}
	
	public void connect() throws Exception{
		
		CreateTable.CreateHSTable();
	
		
		//Extract EauDeStrache.at
		extractEaudestrache(Jsoup.connect("https://www.eaudestrache.at").get());
		extractEaudestrache(Jsoup.connect("https://www.eaudestrache.at/regierung").get());
		extractEaudestrache(Jsoup.connect("https://www.eaudestrache.at/gender").get());
		
		//Extract Perlen-aus-Freital.com
		boolean page=true;
		int i=1;
		do{
			page=extractFreital(Jsoup.connect("https://perlen-aus-freital.tumblr.com/page/"+i).get(),i);
			i++;
		}while(page==true);
		
		
		//Extract HassHilft.de
		extractHassHilft("http://www.hasshilft.de");
			
		
		
	}
	
	
	private void extractHassHilft(String url) throws InterruptedException {
		
		WebDriver driver = new FirefoxDriver();
		
		while (true) {
			driver.get(url);

			List<WebElement> comments = driver.findElements(By
					.className("random-comment"));
			/**
			 * Element commentSection =
			 * doc.getElementById("randomCommentsWrapper"); Element
			 * comments=commentSection.children().get(0);
			 **/
			System.out.println(comments.size());

			for (WebElement com : comments) {

				HatePost post = new HatePost();

				// ID
				// no internal ID

				// Post
				List<WebElement> paras = com.findElements(By.tagName("p"));
				System.out.println(paras.size());
				System.out.println();
				String text=(String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML", paras.get(0));
				post.setPost(text);

				// Link
				// no Source-Link

				hatePosts.add(post);
				postDAO.insertPost(post);
			}
			Thread.sleep(5000);
		}
	}
	
	
	public void extractEaudestrache(Document doc)
	{

		Elements sections=doc.getElementsByTag("section");
		
		for(Element sec:sections)
		{
			String[] classattr=sec.attr("class").split(" ");
			if(classattr[1].startsWith("id"))
			{
				HatePost post=new HatePost();
				
				//ID
				String idsplit[]=classattr[1].split("-");
				post.setId(idsplit[1]);
				
				//Post	
				Elements zitatClass=sec.getElementsByClass("zitat_txt");
				if(zitatClass.hasText())
				{
					post.setPost(zitatClass.get(0).text());
				}
				
				//Link
				Elements linkClass=sec.getElementsByTag("a");
				post.setLink(linkClass.get(0).attr("href"));
				
				hatePosts.add(post);
				postDAO.insertPost(post);
			}
		}	 
	}
	
	
	public boolean extractFreital(Document doc,int i)
	{
		boolean posts=false;

		Elements figcaptions = doc.getElementsByTag("figcaption");
		
		for (Element cap : figcaptions) {

			posts = true;
			HatePost post = new HatePost();

			// Post
			Elements paras = cap.getElementsByTag("p");
			
			//if (paras.get(0).text().startsWith("“")) 
			{
				post.setPost(paras.get(0).text());

				// ID
				post.setId(cap.parent().parent().parent().parent()
						.attr("data-post-id"));

				// Link
				Elements linkClass = cap.getElementsByTag("a");
				if(linkClass.size()>0)
					post.setLink(linkClass.get(0).attr("href"));

				hatePosts.add(post);
				postDAO.insertPost(post);
			}
			
		}
		return posts;
	}
	
	
	public static void main(String[] args) {
		try{
			URLExtractor urlex=new URLExtractor();
			urlex.connect();
			
			System.out.println(urlex.getHatePosts().size());
			for(HatePost pos:urlex.getHatePosts())
				System.out.println(pos.toString());
			
			DatabaseConnector.closeConnection();
			
			}catch(Exception e){
				e.printStackTrace();
			}
	}

}
