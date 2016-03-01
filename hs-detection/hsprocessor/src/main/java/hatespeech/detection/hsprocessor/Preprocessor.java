package hatespeech.detection.hsprocessor;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class Preprocessor {
	
	@SuppressWarnings("resource")
	public static void LucPreprocessor() throws IOException{
		Analyzer analyzer = new GermanAnalyzer();
	    TokenStream tokenStream = analyzer.tokenStream("content", new StringReader("Hass-posting: Das ist ein schlimmer Hasspost"));
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
		try {
			LucPreprocessor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    
}
    
