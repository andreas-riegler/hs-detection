package hatespeech.detection.paragraphvector;

import hatespeech.detection.dao.JDBCTwitterDAO;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

import edu.stanford.nlp.io.EncodingPrintWriter.out;

public class ParagraphToVector {

	private TokenizerFactory tokenizerFactory;
	private ParagraphVectors vec;
	
	public ParagraphVectors buildParagraphVectors(List<String>tweetMessagesList,List<String>labelSourceList)
	{
		SentenceIterator iter = new CollectionSentenceIterator(tweetMessagesList);
		AbstractCache<VocabWord> cache = new AbstractCache<VocabWord>();
		
		tokenizerFactory= new DefaultTokenizerFactory();
		tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        
		LabelsSource source = new LabelsSource(labelSourceList);
		
		 vec = new ParagraphVectors.Builder()
        .minWordFrequency(1)
        .iterations(10)
        .epochs(10)
        .layerSize(100)
        .learningRate(0.025)
        .labelsSource(source)
        .windowSize(10)
        .iterate(iter)
        .trainWordVectors(true)
        .vocabCache(cache)
        .tokenizerFactory(tokenizerFactory)
        //.sampling(0)
        .build();

		vec.fit();
		
		
		
		int counter=1;
		for(String id: labelSourceList)
		{
			System.out.println(counter+ " "+id+ " "+vec.getLookupTable().vector(id));
			counter++;
		}
		System.out.println(tweetMessagesList.get(1945)+" "+tweetMessagesList.get(1946)+" "+vec.similarity("739018485301415936", "739010601473695744"));
		System.out.println(vec.getLookupTable().vector("739018485301415936").getDouble(0,0));
		System.out.println(vec.getLookupTable().vector("739018485301415936").getDouble(0,1));
		System.out.println(vec.getLookupTable().vector("739018485301415936").getDouble(0,2));
		System.out.println(vec.getLookupTable().vector("739018485301415936").getDouble(0));
		System.out.println(vec.getLookupTable().vector("739018485301415936").getDouble(1));
		System.out.println(vec.getLookupTable().vector("739018485301415936").getDouble(2));
		
		return vec;
	}
	public INDArray buildVectorFromUntrainedData(String message) {
	      
	     MeansBuilder meansBuilder = new MeansBuilder(
	         (InMemoryLookupTable<VocabWord>)vec.getLookupTable(),
	           tokenizerFactory);
	     INDArray messageAsCentroid = meansBuilder.messageAsVector(message);
	     
	     return messageAsCentroid;
	           
	}
	public static void main(String[] args) throws Exception {	    

		JDBCTwitterDAO daoTW= new JDBCTwitterDAO();
		List<String> tweetMessagesList=new ArrayList<String>();
		List<String> tweetIdsList=new ArrayList<String>();
		
		daoTW.getClassifiedTweets().stream()
		.forEach(c -> {
		tweetMessagesList.add(c.getMessage());
		tweetIdsList.add(Long.toString(c.getTweetid()));});
		
		ParagraphToVector paraVec=new ParagraphToVector();
		paraVec.buildParagraphVectors(tweetMessagesList, tweetIdsList);
        
    }

}
