package hatespeech.detection.hsprocessor;

import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.model.HatePost;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class Preprocessor {
	
	@SuppressWarnings("resource")
	public static void lucPreprocessor(String hassposttext) throws IOException{
		Analyzer analyzer = new GermanAnalyzer();
	    TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(hassposttext));
	    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	    
	    tokenStream.reset();
	    while (tokenStream.incrementToken()) {
	        System.out.println(charTermAttribute.toString());
	    }
	    tokenStream.end();
	    tokenStream.close();
	    
	    //Generate Bigrams
	    StandardTokenizer tokenizer = new StandardTokenizer();
	    tokenizer.setReader(new StringReader(hassposttext));
	    TokenStream tokenStream2 = new StandardFilter(tokenizer);
	    CharTermAttribute charTermAttribute2 = tokenStream2.addAttribute(CharTermAttribute.class);

	    ShingleFilter shfilter = new ShingleFilter(tokenStream2,2,3);
	    shfilter.setOutputUnigrams(true);
	    shfilter.setOutputUnigramsIfNoShingles(true);
	    
	    shfilter.reset();
	    
	    while(shfilter.incrementToken())
	    {
	    	System.out.println(charTermAttribute2.toString());
	    }
	    shfilter.end();
	    shfilter.close();
	}
	
	public static void main(String[] args){
		
		JDBCHSPostDAO dao= new JDBCHSPostDAO();
		try {
			for(HatePost hatePost: dao.selectAllPosts())
			{
				lucPreprocessor(hatePost.getPost());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    
}
    
