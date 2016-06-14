package hatespeech.detection.hsprocessor;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.Parser;
import is2.tag.Tagger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hatespeech.detection.model.Category;
import hatespeech.detection.model.CategoryScore;
import hatespeech.detection.model.SpellCheckedMessage;
import hatespeech.detection.tokenizer.OpenNLPToolsTokenizerWrapper;

public class FeatureExtractor {

	private static SpellCorrector spellCorr;
	private static SpellCheckedMessage checkedMessage;
	private static LIWCDictionary liwcDic;

	//Typed Dependency variables
	private static SentenceData09 sentenceContainer;
	private static OpenNLPToolsTokenizerWrapper tokenizer;
	private static Lemmatizer lemmatizer;
	private static StringTokenizer st;
	private static Parser dependencyParser;
	private static Tagger tagger;
	private static is2.mtag.Tagger mTagger;
	
	private static final Pattern punctuationMark = Pattern.compile("\\p{Punct}");
	private static final Pattern specialPunctuationMark = Pattern.compile("[\"?!.]");
	private static final Pattern reaptSpecialPunctuationMark = Pattern.compile("[\"?!.]{2,}");
	
	static
	{
		spellCorr=new SpellCorrector();
		liwcDic=LIWCDictionary.loadDictionaryFromFile("../dictionary.obj");
		sentenceContainer = new SentenceData09();
		try {
			tokenizer = OpenNLPToolsTokenizerWrapper.loadOpenNLPTokenizer(new File("resources/de-token.bin"));
			lemmatizer = new Lemmatizer("resources/lemma-ger-3.6.model");
			tagger = new Tagger("resources/tag-ger-3.6.model");
			mTagger = new is2.mtag.Tagger("resources/morphology-ger-3.6.model");
			dependencyParser = new Parser("resources/parser-ger-3.6.model");
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static String getTypedDependencies(String message)
	{	
		String[] tokenizedMessage = tokenizer.tokenize(message);
		
		sentenceContainer.init(tokenizedMessage);
		
		sentenceContainer = lemmatizer.apply(sentenceContainer);	
		sentenceContainer = tagger.apply(sentenceContainer);
		sentenceContainer = mTagger.apply(sentenceContainer);
		sentenceContainer = dependencyParser.apply(sentenceContainer);

		System.out.println("FROM\tHEAD\tLABEL\tFEATS");
		for (int k=0;k< sentenceContainer.length();k++){
			System.out.println((k+1) + " "+sentenceContainer.forms[k]+"\t"+sentenceContainer.pheads[k]+"\t"+sentenceContainer.plabels[k]+"\t"+sentenceContainer.pfeats[k]
					+"\t"+sentenceContainer.ppos[k]+"\t"+sentenceContainer.gpos[k]);
		}
		
		for (int k=0;k< sentenceContainer.length();k++){
			System.out.print((k+1) + " "+sentenceContainer.forms[k]+"\t"+sentenceContainer.pheads[k]+"\t"+sentenceContainer.plabels[k]+"\t"+sentenceContainer.pfeats[k]
					+"\t"+sentenceContainer.ppos[k]+"\t"+sentenceContainer.gpos[k]);
		}
		
		//Erschießt sie, nur so werden es weniger.
		//neb(werden,erschießen) subj(erschießen,sie) root(ROOT,,) adv(so,nur) adv(werden,so) root(ROOT,werden) expl(werden,es) adv(werden,weniger)
		
		return null;
	}

	public static Integer getMistakes(String message)
	{
		if(checkedMessage!=null)
			checkedMessage = spellCorr.findMistakes(message);

		return checkedMessage.getMistakes();
	}

	public static Integer getExclMarkMistakes(String message)
	{
		if(checkedMessage!=null)
			checkedMessage = spellCorr.findMistakes(message);

		return checkedMessage.getExclMarkMistakes();
	}

	public static List<CategoryScore> getLiwcCountsPerCategory(String message)
	{
		return liwcDic.classifyMessage(message);
	}

	public static Set<Category> getLiwcCategories()
	{
		return liwcDic.getCategories();
	}
	
	//Linguistic Features
	public static Integer getLengthinTokens(String message)
	{
		String[] split=message.split(" ");
		return split.length;
	}
	
	public static Double getAvgLengthofWord(String message)
	{
		Double sumLength=0.0;
		Double counter=0.0;
		String[] split=message.split(" ");
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				sumLength+=punctuationMark.matcher(word).replaceAll("").length();
				counter++;
			}
		}
		return sumLength/counter;
	}
	public static Integer getNumberofPunctuation(String message)
	{
		Integer hits=0;
		String[] split=message.split(" ");
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				Matcher m=punctuationMark.matcher(word);
				while (m.find()) {
				    hits++;
				}
			}
		}
		return hits;
	}
	
	public static Integer getNumberofSpecialPunctuation(String message)
	{
		Integer hits=0;
		String[] split=message.split(" ");
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				Matcher m=specialPunctuationMark.matcher(word);
				while (m.find()) {
				    hits++;
				}
				m=reaptSpecialPunctuationMark.matcher(word);
				while(m.find())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	

	public static void main(String[] args) {
		//FeatureExtractor.getTypedDependencies("Peter hat eine Katze, die gerne Mäuse fängt.");
		System.out.println(FeatureExtractor.getNumberofSpecialPunctuation("Hi...wie gehts??"));
	}
}
