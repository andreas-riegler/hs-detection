package hatespeech.detection.hsprocessor;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.Parser;
import is2.tag.Tagger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
	private static Parser dependencyParser;
	private static Tagger tagger;
	private static is2.mtag.Tagger mTagger;
	
	//Linguistic Features variables
	private static final Pattern punctuationMark = Pattern.compile("\\p{Punct}");
	private static final Pattern specialPunctuationMark = Pattern.compile("[\"?!.]");
	private static final Pattern reaptSpecialPunctuationMark = Pattern.compile("[\"?!.]{2,}");
	private static final Pattern capitTokens = Pattern.compile("[A-Z]");
	private static final Pattern nonAlphaInWord = Pattern.compile("[A-Za-zƒ‹÷‰¸ˆﬂ]+[^A-Za-zƒ‹÷‰¸ˆﬂ\\s]+[A-Za-zƒ‹÷‰¸ˆﬂ]+");
	private static List<String>connectorsList;
	public enum TypedDependencyWordType {
		ORIGINAL, LEMMA
	}
	static
	{
		spellCorr=new SpellCorrector();
		liwcDic=LIWCDictionary.loadDictionaryFromFile("../dictionary.obj");
		

		try {
			connectorsList = Files.readAllLines(new File("../connectors.txt").toPath(), Charset.defaultCharset() );
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

	public static String getTypedDependencies(String message, TypedDependencyWordType wordType)
	{	
		String[] tokenizedMessage = tokenizer.tokenize(message);

		sentenceContainer = new SentenceData09();
		sentenceContainer.init(tokenizedMessage);

		sentenceContainer = lemmatizer.apply(sentenceContainer);	
		sentenceContainer = tagger.apply(sentenceContainer);
		sentenceContainer = mTagger.apply(sentenceContainer);
		sentenceContainer = dependencyParser.apply(sentenceContainer);
		
		StringBuilder typedDependencies = new StringBuilder();

		if(wordType == TypedDependencyWordType.ORIGINAL){
			for (int k=0;k< sentenceContainer.length();k++){
				typedDependencies.append((sentenceContainer.plabels[k].equals("--") ? "root" : sentenceContainer.plabels[k]) + 
						"(" + (sentenceContainer.pheads[k] == 0 ? "ROOT" : sentenceContainer.forms[sentenceContainer.pheads[k]-1].toLowerCase()) +
						"," + sentenceContainer.forms[k].toLowerCase() + ") ");

			}
		}
		else if(wordType == TypedDependencyWordType.LEMMA){
			for (int k=0;k< sentenceContainer.length();k++){
				String typedDependency = sentenceContainer.plabels[k].equals("--") ? "root" : sentenceContainer.plabels[k];
				String firstLabel = sentenceContainer.pheads[k] == 0 ? "ROOT" : sentenceContainer.plemmas[sentenceContainer.pheads[k]-1].toLowerCase();
				String secondLabel = sentenceContainer.plemmas[k].toLowerCase();
				
				if(firstLabel.equals("--")){
					firstLabel = sentenceContainer.forms[sentenceContainer.pheads[k]-1].toLowerCase();
				}
				if(secondLabel.equals("--")){
					secondLabel = sentenceContainer.forms[k].toLowerCase();
				}
				
				typedDependencies.append(typedDependency + "(" + (firstLabel) +	"," + secondLabel + ") ");
			}
		}
		typedDependencies = new StringBuilder(typedDependencies.toString().trim());

		return typedDependencies.toString();
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
	public static Integer getNumberofOneLetterTokens(String message)
	{
		Integer hits=0;
		String[] split=message.split(" ");
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(word.length()==1)
					hits++;
			}
		}
		return hits;
	}
	public static Integer getNumberofCapitalizedTokens(String message)
	{
		Integer hits=0;
		String[] split=message.split(" ");
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				Matcher m=capitTokens.matcher(word);
				while (m.find()) {
				    hits++;
				}
			}
		}
		return hits;
	}
	public static Integer getNumberofURLs(String message)
	{
		Integer hits=0;
		String[] split=message.split(" ");
		for(String word : split)
		{
			if(word.startsWith("http"))
			{
				    hits++;
			}
		}
		return hits;
	}
	public static Integer getNumberofNonAlphaCharInMiddleOfWord(String message)
	{
		Integer hits=0;
		String[] split=message.split(" ");
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				Matcher m=nonAlphaInWord.matcher(word);
				while (m.find()) {
				    hits++;
				}
			}
		}
		return hits;
	}
	public static Integer getNumberOfDiscourseConnectives(String message)
	{
		Integer hits=0;
		String[] split=message.split(" ");
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(connectorsList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}


	public static void main(String[] args) {
		//FeatureExtractor.getTypedDependencies("Peter hat eine Katze, die gerne M‰use f‰ngt.");
		System.out.println(FeatureExtractor.getNumberofCapitalizedTokens("Hal@oo wie geht e/S dir"));
		System.out.println(FeatureExtractor.getTypedDependencies("Erschieﬂt sie, nur so werden es weniger.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Erschieﬂt as, nur so geht's uns besser.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Ich gebe dir 1000 Euro.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Ich gebe dir 1000 Ä.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Ich gebe dir 1000Ä.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Ich/ geb'e dir} nicht [ 1000Ä!!!!!??!!?!", TypedDependencyWordType.LEMMA));
	}
}
