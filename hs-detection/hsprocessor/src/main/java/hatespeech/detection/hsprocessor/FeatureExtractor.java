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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.core.pattern.color.BlackCompositeConverter;
import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.Category;
import hatespeech.detection.model.CategoryScore;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.SpellCheckedMessage;
import hatespeech.detection.tokenizer.OpenNLPToolsTokenizerWrapper;

public class FeatureExtractor {

	private static JDBCFBCommentDAO fbCommentDao;

	private static SpellCorrector spellCorr;
	private static SpellCheckedMessage checkedMessage;
	private static LIWCDictionary liwcDic;

	//Typed Dependency variables
	private static final Pattern PRINTABLE_CHARACTERS_PATTERN = Pattern.compile("[^ -~������߀]");
	private static SentenceData09 sentenceContainer;
	private static OpenNLPToolsTokenizerWrapper tokenizer;
	private static Lemmatizer lemmatizer;
	private static Parser dependencyParser;
	private static Tagger tagger;
	private static is2.mtag.Tagger mTagger;
	private static List<String> dependencyTypeBlacklist;
	
	//Linguistic Features variables
	private static final Pattern punctuationMark = Pattern.compile("\\p{Punct}");
	private static final Pattern specialPunctuationMark = Pattern.compile("[\"?!.]");
	private static final Pattern endOfSentence = Pattern.compile("[?!.]+");
	private static final Pattern tokenizerpunctuationMark = Pattern.compile("[?!.,;:)(\\-\"]+"); //Unterschied zu punctuationMark? vorher ([?!.,\"]+)
	private static final Pattern character = Pattern.compile("[A-Za-z]");
	private static final Pattern hashtag = Pattern.compile("#");
	private static final Pattern reaptSpecialPunctuationMark = Pattern.compile("[\"?!.]{2,}");
	private static final Pattern capitLetter = Pattern.compile("[A-Z]");
	private static final Pattern nonAlphaInWord = Pattern.compile("[A-Za-z�������]+[^A-Za-z�������\\s]+[A-Za-z�������]+");

	//Lexical Features varibles
	private static List<String> connectorsList;
	private static List<String> hatefulTermsList;
	private static List<String>modalVerbsList;
	private static List<String>particlesList;
	private static List<String>firstPersonPronounsList;
	private static List<String>secondPersonPronounsList;
	private static List<String>thirdPersonPronounsList;
	private static List<String>interrogativPronounsList;
	private static List<String>indefinitPronounsList;
	private static List<String>demonstrativPronounsList;
	private static final Pattern HAPPY_EMOTICON_PATTERN = Pattern.compile("[:=xX][ -co]?[)D>\\]]|<3|;D");
	private static final Pattern SAD_EMOTICON_PATTERN = Pattern.compile("[:=xX;][ -']?[(<C/\\[]");
	private static final Pattern CHEEKY_EMOTICON_PATTERN=Pattern.compile("[:=xX][ -o]?[Pbp)D\\]]|;[ -o]?[)\\]]");
	private static final Pattern AMAZED_EMOTICON_PATTERN=Pattern.compile("[:=][ -]?[oO0]");


	public enum TypedDependencyWordType {
		ORIGINAL, LEMMA
	}
	static
	{
		fbCommentDao = new JDBCFBCommentDAO();
		spellCorr=new SpellCorrector();
		liwcDic=LIWCDictionary.loadDictionaryFromFile("../dictionary.obj");
		//dependencyTypeBlacklist = Arrays.asList("root");
		dependencyTypeBlacklist = new ArrayList<String>();
		
		try {
			connectorsList = Files.readAllLines(new File("resources/wordlists/connectors.txt").toPath(), Charset.defaultCharset());
			hatefulTermsList = Files.readAllLines(new File("resources/wordlists/hatefulTerms.txt").toPath(), Charset.defaultCharset());
			modalVerbsList = Files.readAllLines(new File("resources/wordlists/modalverbs.txt").toPath(), Charset.defaultCharset() );
			particlesList = Files.readAllLines(new File("resources/wordlists/particles.txt").toPath(), Charset.defaultCharset() );
			firstPersonPronounsList = Files.readAllLines(new File("resources/wordlists/firstpersonpronouns.txt").toPath(), Charset.defaultCharset() );
			secondPersonPronounsList = Files.readAllLines(new File("resources/wordlists/secondpersonpronouns.txt").toPath(), Charset.defaultCharset() );
			thirdPersonPronounsList = Files.readAllLines(new File("resources/wordlists/thirdpersonpronouns.txt").toPath(), Charset.defaultCharset() );
			interrogativPronounsList = Files.readAllLines(new File("resources/wordlists/interrogativpronouns.txt").toPath(), Charset.defaultCharset() );
			indefinitPronounsList = Files.readAllLines(new File("resources/wordlists/indefinitpronouns.txt").toPath(), Charset.defaultCharset() );
			demonstrativPronounsList = Files.readAllLines(new File("resources/wordlists/demonstrativpronouns.txt").toPath(), Charset.defaultCharset() );

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

	private FeatureExtractor(){}

	public static String getTypedDependencies(String message, TypedDependencyWordType wordType)
	{
		message = PRINTABLE_CHARACTERS_PATTERN.matcher(message).replaceAll("");

		if(message.isEmpty()){
			return "";
		}

		String[] tokenizedMessage = tokenizer.tokenizeWithRootNode(message);

		if(tokenizedMessage.length == 0){
			return "";
		}

		sentenceContainer = new SentenceData09();
		sentenceContainer.init(tokenizedMessage);
		sentenceContainer = lemmatizer.apply(sentenceContainer);	
		sentenceContainer = tagger.apply(sentenceContainer);
		sentenceContainer = mTagger.apply(sentenceContainer);
		sentenceContainer = dependencyParser.apply(sentenceContainer);

		StringBuilder typedDependencies = new StringBuilder();

		for (int k=0;k< sentenceContainer.length();k++){
			String typedDependency = sentenceContainer.plabels[k].equals("--") ? "root" : sentenceContainer.plabels[k];
			String firstLabel = "";
			String secondLabel = "";

			switch(wordType){
			case ORIGINAL:
				firstLabel = sentenceContainer.pheads[k] == 0 ? "ROOT" : sentenceContainer.forms[sentenceContainer.pheads[k]-1].toLowerCase();
				secondLabel = sentenceContainer.forms[k].toLowerCase();
				break;
			case LEMMA:
				firstLabel = sentenceContainer.pheads[k] == 0 ? "ROOT" : sentenceContainer.plemmas[sentenceContainer.pheads[k]-1].toLowerCase();
				secondLabel = sentenceContainer.plemmas[k].toLowerCase();
				if(firstLabel.equals("--")){
					firstLabel = sentenceContainer.forms[sentenceContainer.pheads[k]-1].toLowerCase();
				}
				if(secondLabel.equals("--")){
					secondLabel = sentenceContainer.forms[k].toLowerCase();
				}
				break;
			}

			if(!punctuationMark.matcher(firstLabel).matches() && !punctuationMark.matcher(secondLabel).matches() && !dependencyTypeBlacklist.contains(typedDependency)){
				typedDependencies.append(typedDependency + "(" + (firstLabel) +	"," + secondLabel + ") ");
			}
		}
		typedDependencies = new StringBuilder(typedDependencies.toString().trim());

		return typedDependencies.toString();
	}

	public static int getMistakes(String message)
	{
		if(spellCorr!=null)
			checkedMessage = spellCorr.findMistakes(message);

		return checkedMessage.getMistakes();
	}

	public static int getExclMarkMistakes(String message)
	{
		if(spellCorr!=null)
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
	public static int getLengthInTokens(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);

		for(String word : split)
		{
			if(!tokenizerpunctuationMark.matcher(word).find())
			{
				hits++;
			}
		}
		return hits;
	}

	public static double getAvgLengthOfWord(String message)
	{
		double sumLength=0.0;
		double tokenCount=getLengthInTokens(message);
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				sumLength+=punctuationMark.matcher(word).replaceAll("").length();
				//counter++;
			}
		}
		if(tokenCount != 0.0){
			return sumLength/tokenCount;
		}
		else{
			return 0.0;
		}
	}
	public static int getNumberOfSentences(String message)
	{
		int hits=0;

		Matcher m=endOfSentence.matcher(message);
		while (m.find()) {
			hits++;
		}
		return hits;
	}
	public static double getAvgSentenceLength(String message)
	{	
		double numberOfSentences = getNumberOfSentences(message);
		if(numberOfSentences != 0){
			return (double)getLengthInTokens(message)/(double)getNumberOfSentences(message);
		}
		else{
			return 0.0;
		}
	}
	public static int getNumberOfCharacters(String message)
	{
		int hits=0;

		Matcher m=character.matcher(message);
		while (m.find()) {
			hits++;
		}		

		return hits;
	}
	public static int getNumberOfHashtags(String message)
	{
		int hits=0;

		Matcher m=hashtag.matcher(message);
		while (m.find()) {
			hits++;
		}		

		return hits;
	}
	public static int getNumberOfPunctuation(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
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

	public static int getNumberOfSpecialPunctuation(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
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
	public static int getNumberOfOneLetterTokens(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
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
	public static int getNumberOfCapitalizedLetters(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				Matcher m=capitLetter.matcher(word);
				while (m.find()) {
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfURLs(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(word.startsWith("http"))
			{
				hits++;
			}
		}
		return hits;
	}
	public static int getNumberOfNonAlphaCharInMiddleOfWord(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
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

	//Lexical Features
	public static int getNumberOfDiscourseConnectives(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
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
	public static int getNumberOfHatefulTerms(String message)
	{
		int hits=0;
		String[] split =tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(hatefulTermsList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfHatefulTermsInApostrophe(String message)
	{
		int hits=0;
		String[] split =tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(hatefulTermsList.stream().filter(s -> ("\""+s+"\"").equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static double getDensityOfHatefulTerms(String message)
	{
		double lengthInTokens = getLengthInTokens(message);
		if(lengthInTokens != 0){
			return (double)getNumberOfHatefulTerms(message)/(double)getLengthInTokens(message);
		}
		else{
			return 0.0;
		}
	}
	public static int getNumberOfDiscourseParticels(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(particlesList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfModalVerbs(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(modalVerbsList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfFirstPersonPronouns(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(firstPersonPronounsList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfSecondPersonPronouns(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(secondPersonPronounsList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfThirdPersonPronouns(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(thirdPersonPronounsList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfDemonstrativPronouns(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(demonstrativPronounsList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfIndefinitPronouns(String message)
	{
		int hits=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{
			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(indefinitPronounsList.stream().filter(s -> s.equals(word.toLowerCase())).findFirst().isPresent())
				{
					hits++;
				}
			}
		}
		return hits;
	}
	public static int getNumberOfInterrogativPronouns(String message)
	{
		int hits=0,counter=0;
		String[] split=tokenizer.tokenize(message);
		for(String word : split)
		{	
			String trigramHelper=null;

			if(split.length>counter+2)
			{
				trigramHelper=(word+" "+split[counter+1]+" "+split[counter+2]).toLowerCase();
			}
			String trigram=trigramHelper;

			if(!word.equals("RT")&&!word.startsWith("@")&&!word.startsWith("http"))
			{
				if(interrogativPronounsList.stream().filter(s -> s.equals(word.toLowerCase())||s.equals(trigram)).findFirst().isPresent())
				{
					hits++;
				}
			}
			counter++;
		}

		return hits;
	}
	public static int getNumberOfHappyEmoticons(String message)
	{
		int hits=0;
		Matcher m=HAPPY_EMOTICON_PATTERN.matcher(message);
		while (m.find()) {
			hits++;
		}
		return hits;
	}
	public static int getNumberOfSadEmoticons(String message)
	{
		int hits=0;
		Matcher m=SAD_EMOTICON_PATTERN.matcher(message);
		while (m.find()) {
			hits++;
		}
		return hits;
	}
	public static int getNumberOfCheekyEmoticons(String message)
	{
		int hits=0;
		Matcher m=CHEEKY_EMOTICON_PATTERN.matcher(message);
		while (m.find()) {
			hits++;
		}
		return hits;
	}
	public static int getNumberOfAmazedEmoticons(String message)
	{
		int hits=0;
		Matcher m=AMAZED_EMOTICON_PATTERN.matcher(message);
		while (m.find()) {
			hits++;
		}
		return hits;
	}

	//Facebook features
	public static String getFBReactionByFBComment(FBComment comment){
		return fbCommentDao.getFBReaction(comment.getPostId(), comment.getFromId());
	}

	public static void main(String[] args) {
		//FeatureExtractor.getTypedDependencies("Peter hat eine Katze, die gerne M�use f�ngt.");

		//		FBComment c = new FBComment("123123", "asdasdasd", 1);
		//		c.setFromId("1021855351220968");
		//		c.setPostId("1553687164876733_1756833867895394");
		//
		//		System.out.println(FeatureExtractor.getFBReactionByFBComment(c));
		//		c.setFromId("123123123132312312313");
		//		System.out.println(FeatureExtractor.getFBReactionByFBComment(c));
		//		c.setPostId("1553687164876733_1756576847921096");
		//		c.setFromId("1356572597693428");
		//		System.out.println(FeatureExtractor.getFBReactionByFBComment(c));

		//		System.out.println(FeatureExtractor.getLengthInTokens("Hallo!;) :D asdasdd :D asdasd :-)asdasdasd :) xD XDXD :)))) ;-)"));
		//		System.out.println(FeatureExtractor.getLengthInTokens("ad ! aad ,asd ;asd asd;asd asd,asd:asd asd, as!!! ass ad't asd' ad'sd 'sd zs!?! asd ?! !? !!asd!!asd !?"));
		//		System.out.println(FeatureExtractor.getLengthInTokens("a'b\"_er j;-a, eh ;!"));
		//		System.out.println(FeatureExtractor.getNumberOfHatefulTermsInApostrophe("was \"f�r\" ein \"Hurensohn\""));
				System.out.println(FeatureExtractor.getAvgLengthOfWord("DU bist,ein?Huren??sohn!!!"));
		System.out.println(FeatureExtractor.getTypedDependencies("Erschie�t sie, nur so werden es weniger.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Erschie�t as, nur so geht's uns besser.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Ich gebe dir 1000 Euro.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Ich gebe dir 1000 �.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Ich gebe dir 1000�.", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Ich geb' dir nicht 1000�!!!", TypedDependencyWordType.LEMMA));
		System.out.println(FeatureExtractor.getTypedDependencies("Dieses Pack sollte man ohne Umwege heimschicken!", TypedDependencyWordType.LEMMA));
	}
}
