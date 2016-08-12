package hatespeech.detection.hsprocessor;

import hatespeech.detection.model.Category;
import hatespeech.detection.model.CategoryScore;
import hatespeech.detection.model.LIWCWord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LIWCDictionaryCounter {

	private Map<String, HashSet<Integer>> wordMap;
	private Set<Category> categories;
	private Map<Integer, Category> intCategoryLookupMap;

	private LIWCDictionaryCounter(){
		wordMap = new HashMap<String, HashSet<Integer>>();
		categories = new HashSet<Category>();
		intCategoryLookupMap = new HashMap<Integer, Category>();
	}

	public static LIWCDictionaryCounter createDictionaryFromFile(String path) {

		LIWCDictionaryCounter liwcDictionary = new LIWCDictionaryCounter();

		File dictionaryFile = new File(path);

		if(dictionaryFile.exists()) {
			try (FileInputStream fileIn= new FileInputStream(path); BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));){
				List<String> rawLIWC = new ArrayList<String>();
				String nextLine = "";
				while(true) {
					nextLine = buffer.readLine();
					if(nextLine == null){
						break;
					}
					rawLIWC.add(nextLine);
				}
				System.out.println("Read in "+rawLIWC.size()+" LIWC lines");

				int i = 1;
				//iterate until we hit the end of the category list
				while(!rawLIWC.get(i).equals("%")) {
					String[] splitCategory = rawLIWC.get(i).split("\\s+"); //split on all whitespace
					int currentID = Integer.parseInt(splitCategory[1]);
					String currentTitle = splitCategory[2];
					Category currentCategory = new Category(currentTitle);	
					currentCategory.setLIWCID(currentID);

					liwcDictionary.categories.add(currentCategory);
					liwcDictionary.intCategoryLookupMap.put(currentID, currentCategory);

					//System.out.println(currentCategory.getLIWCID() + " " + currentCategory.getTitle());

					i++;
				}

				//skip % character
				i++;

				//iterate until we hit end of word list
				for(; i < rawLIWC.size(); i++){
					String[] splitWord = rawLIWC.get(i).split("\\s+");			
					HashSet<Integer> currentCategories = new HashSet<Integer>();			
					for(int k = 1; k < splitWord.length; k++) {			
						currentCategories.add(Integer.parseInt(splitWord[k]));
					}

					liwcDictionary.wordMap.put(splitWord[0], currentCategories);
				}

				return liwcDictionary;

			} catch (IOException e) {
				System.err.println("Couldn't read from LIWC Dictionary file at "  + path);
			}

		} else {
			System.err.println("Couldn't find dictionary file");
		}

		return null;
	}

	private Set<Integer> getCategoriesForWord(String word){

		Set<Integer> wordCategories = wordMap.get(word);
		String tempWord = word;

		//remove last character until only one character left (e.g. a*) or wildcard match found
		for(int i = tempWord.length(); i > 0 && wordCategories == null; i--){
			//System.out.println("word: " + tempWord.subSequence(0, i) + "*");
			wordCategories = wordMap.get(tempWord.subSequence(0, i) + "*");
		}

		return wordCategories;
	}

	public List<CategoryScore> classifyMessage (String message) {

		if(message == null){
			return null;
		}

		//Initialise the categories into a HashMap with scores set to 0
		LinkedHashMap<Category, Integer> categoryScores = new LinkedHashMap<Category, Integer>();
		categories.forEach(c -> categoryScores.put(c, 0));

		//Iterate over every message, converting to lowercase and removing all but apostrophes

		String[] split = message.toLowerCase().split("[^0-9a-zA-ZäÄöÖüÜß]");

		//Iterate over every word in the message and update category scores as necessary
		for(String word : split) {
			Set<Integer> wordCategories = getCategoriesForWord(word);

			if(wordCategories == null){
				continue;
			}

			for(Integer categoryId : wordCategories){	
				Category currentCategory = intCategoryLookupMap.get(categoryId);
				if(currentCategory == null){
					continue;
				}		
				categoryScores.put(currentCategory, categoryScores.get(currentCategory) + 1);
			}
		}

		//remove zero scoring categories; map map-entry to CategoryScore; collect to ArrayList
		return categoryScores.entrySet().stream()
				.filter(e -> e.getValue() != 0)
				.map(e -> new CategoryScore(e.getKey(), e.getValue()))
				.collect(Collectors.toCollection(ArrayList::new));

	}			

	public Set<Category> getCategories()
	{
		return categories;
	}

	public static void main(String[] args) {
		LIWCDictionaryCounter liwcDict = LIWCDictionaryCounter.createDictionaryFromFile("../LIWC_German_modified.dic");

		//liwcDict.wordMap.forEach((k,v) -> System.out.println(k + " " + v));
		//System.out.println("words: " + liwcDict.wordMap.size());

		liwcDict.classifyMessage(null).forEach(cs -> System.out.println(cs.getCategory().getLIWCID() + " " + cs.getCategory().getTitle() + " " + cs.getScore()));
	}

}
