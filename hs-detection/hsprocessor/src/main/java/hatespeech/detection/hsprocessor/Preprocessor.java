package hatespeech.detection.hsprocessor;

import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.model.HatePost;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class Preprocessor {
	
	@SuppressWarnings("resource")
	public static void LucPreprocessor(String Hassposttext) throws IOException{
		Analyzer analyzer = new GermanAnalyzer();
	    TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(Hassposttext));
	    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	    
	    tokenStream.reset();
	    
	    while(tokenStream.incrementToken())
	    {
	    	System.out.println(charTermAttribute.toString());
	    }
	    tokenStream.end();
	    tokenStream.close();
	}
	
	public static void main(String[] args){
		
		JDBCHSPostDAO dao= new JDBCHSPostDAO();
		try {
			for(HatePost hatePost: dao.selectAllPosts())
			{
				LucPreprocessor(hatePost.getPost());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    
}
    
