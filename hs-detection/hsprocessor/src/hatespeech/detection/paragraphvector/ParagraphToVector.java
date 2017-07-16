package hatespeech.detection.paragraphvector;

import hatespeech.detection.dao.JDBCTwitterDAO;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.learning.impl.sequence.DM;
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
			//.minLearningRate(0.001)
			.labelsSource(source)
			//.stopWords(Files.readAllLines(new File("../stopwords.txt").toPath(), Charset.defaultCharset() ))
			.windowSize(10)
			.iterate(iter)
			.trainWordVectors(true)
			.vocabCache(cache)
			//Wahlweise Distributional-BOW(default) oder Distributional Memory new DM<VocabWord>()
			.sequenceLearningAlgorithm(new DM<VocabWord>())
			.tokenizerFactory(tokenizerFactory)
			//.sampling(0)
			.build();
		

		vec.fit();
		
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
