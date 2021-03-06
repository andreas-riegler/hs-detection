package hatespeech.detection.tokenizer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.core.tokenizers.Tokenizer;

public class RetainHatefulTermsNGramTokenizer extends NGramTokenizer{

	private BufferedReader reader;
	private Set<String> hatefulTermsSet;
	private SnowballStemmer stemmer;
	private boolean hasNext;
	private String currentToken, nextToken;

	private boolean filterUnigramsToo;
	private boolean tokenFormatTypedDependencies;
	private boolean exactMatch;

	public RetainHatefulTermsNGramTokenizer() {

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream("resources/wordlists/hatefulTerms.txt"), "UTF-8"));
			stemmer = new SnowballStemmer("german");
			hatefulTermsSet = new HashSet<String>();

			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				hatefulTermsSet.add(stemmer.stem(line).toLowerCase());
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}


	public boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}

	public boolean isFilterUnigramsToo() {
		return filterUnigramsToo;
	}

	public void setFilterUnigramsToo(boolean filterUnigramsToo) {
		this.filterUnigramsToo = filterUnigramsToo;
	}

	public boolean isTokenFormatTypedDependencies() {
		return tokenFormatTypedDependencies;
	}

	public void setTokenFormatTypedDependencies(boolean tokenFormatTypedDependencies) {
		this.tokenFormatTypedDependencies = tokenFormatTypedDependencies;
	}


	@Override
	public String nextElement() {
		String tempCurrentToken = currentToken;
		currentToken = nextToken;
		hasNext = currentToken != null;
		nextToken = getNextValidToken();

		return tempCurrentToken;
	}

	@Override
	public boolean hasMoreElements() {
		return hasNext;
	}

	@Override
	public void tokenize(String s) {
		super.tokenize(s);
		reinitializeTokenizer();
	}

	private void reinitializeTokenizer(){
		currentToken = getNextValidToken();
		hasNext = currentToken != null;		
		nextToken = getNextValidToken();
	}

	private String getNextValidToken(){		
		while(super.hasMoreElements()){
			String token = super.nextElement();
			if((token.contains(" ") || filterUnigramsToo) && !token.isEmpty()){
				String splitToken[] = token.split(" ");
				for(int i = 0; i < splitToken.length; i++){
					if(tokenFormatTypedDependencies){
						int firstParenthesisIndex = splitToken[i].indexOf("(");
						int commaIndex = splitToken[i].indexOf(",");
						int secondParenthesisIndex = splitToken[i].indexOf(")");

						String firstWord = splitToken[i].substring(firstParenthesisIndex + 1, commaIndex);
						String secondWord = splitToken[i].substring(commaIndex + 1, secondParenthesisIndex);

						if(checkStemmedMatch(firstWord) || checkStemmedMatch(secondWord)){
							return token;
						}
					}
					else{
						if(checkStemmedMatch(splitToken[i])){
							return token;
						}
					}
				}
			}
			else if(!token.isEmpty()){
				return token;
			}
		}
		return null;
	}

	private boolean checkStemmedMatch(String word){
		if(exactMatch){
			if(hatefulTermsSet.contains(stemmer.stem(word).toLowerCase())){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			for(String hatefulTerm : hatefulTermsSet){
				if(stemmer.stem(word).toLowerCase().contains(hatefulTerm)){
					return true;
				}
			}
			return false;
		}
	}

}
