package hatespeech.detection.hsprocessor;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.ml.HatepostClassifier;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.Feature;
import hatespeech.detection.model.FeatureVector;
import hatespeech.detection.model.HatePost;
import hatespeech.detection.model.PostType;
import hatespeech.detection.model.TrainingSample;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import weka.classifiers.bayes.NaiveBayes;

public class Preprocessor {
	
	@SuppressWarnings("resource")
	public FeatureVector lucPreprocessor(String hassposttext) throws IOException{
		
		FeatureVector featVec=new FeatureVector();
		featVec.setRawMessage(hassposttext);
		
		Analyzer analyzer = new GermanAnalyzer();
	    TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(hassposttext));
	    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	    
	    tokenStream.reset();
	    while (tokenStream.incrementToken()) {
	    	Feature feature= new Feature();
	    	feature.setnGram(charTermAttribute.toString());
	    	featVec.addFeature(feature);
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
	    
	    return featVec;
	}
	
	public static void main(String[] args){
		
		JDBCFBCommentDAO dao= new JDBCFBCommentDAO();
		Preprocessor preProc=new Preprocessor();
		List<TrainingSample> trainingSamples = new ArrayList<TrainingSample>();
		
		try {
			for(FBComment post: dao.getFBComments())
			{
				if(post.getResult()!=-1)
				{
					if(post.getResult()==0)
						trainingSamples.add(new TrainingSample(preProc.lucPreprocessor(post.getMessage()),PostType.NEGATIVE));
					else
						trainingSamples.add(new TrainingSample(preProc.lucPreprocessor(post.getMessage()),PostType.POSITIVE));
	
				}
			}
			HatepostClassifier classifier=new HatepostClassifier(trainingSamples,new NaiveBayes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    
}
    
