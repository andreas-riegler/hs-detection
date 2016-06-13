package hatespeech.detection.hsprocessor;

import java.util.List;
import java.util.Set;

import hatespeech.detection.model.Category;
import hatespeech.detection.model.CategoryScore;
import hatespeech.detection.model.SpellCheckedMessage;

public class FeatureExtractor {
	
	private static SpellCorrector spellCorr;
	private static SpellCheckedMessage checkedMessage;
	private static LIWCDictionary liwcDic;
	
	public FeatureExtractor()
	{
		spellCorr=new SpellCorrector();
		liwcDic=LIWCDictionary.loadDictionaryFromFile("../dictionary.obj");
	}
	
	public static String getTypedDependencies(String message){
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
}
