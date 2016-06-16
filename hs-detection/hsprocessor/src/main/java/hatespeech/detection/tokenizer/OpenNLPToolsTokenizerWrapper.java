package hatespeech.detection.tokenizer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class OpenNLPToolsTokenizerWrapper {

	Tokenizer tokenizer;

	public OpenNLPToolsTokenizerWrapper(opennlp.tools.tokenize.Tokenizer tokenizerImplementation){
		this.tokenizer=tokenizerImplementation;
	}

	public String[] tokenize(String sentence){
		return tokenize(sentence, false);
	}

	public String[] tokenizeWithRootNode(String sentence){
		return tokenize(sentence, true);
	}

	private String[] tokenize(String sentence, boolean addRoot){
		System.out.println(sentence);
		sentence = sentence.replaceAll("[!][!]+", "!");
		sentence = sentence.replaceAll("[\\?][\\?]+", "?");
		sentence = sentence.replaceAll("[.][.]+", ".");
		sentence = sentence.replaceAll("[,][,]+", ",");
		sentence = sentence.replaceAll("[;][;]+", ";");
		sentence = sentence.replaceAll("[:][:]+", ":");

		System.out.println(sentence);
		sentence = sentence.replaceAll("(?<=[,.!?;:])(?!$)", " ");
		System.out.println(sentence);

		String[] tokens = tokenizer.tokenize(sentence);
		
		if(!addRoot){
			return tokens;
		}
		else{
			String[] withRoot = new String[tokens.length+1];
			//withRoot[0]="<root>";
			withRoot[0]=is2.io.CONLLReader09.ROOT;
			System.arraycopy(tokens, 0, withRoot, 1, tokens.length);
			return withRoot;
		}
	}

	public static OpenNLPToolsTokenizerWrapper loadOpenNLPTokenizer(File modelFile) throws IOException{
		BufferedInputStream modelIn = new BufferedInputStream(new FileInputStream(modelFile.toString()));
		opennlp.tools.tokenize.Tokenizer tokenizer = new TokenizerME(new TokenizerModel(modelIn));
		return new OpenNLPToolsTokenizerWrapper(tokenizer);
	}

}
