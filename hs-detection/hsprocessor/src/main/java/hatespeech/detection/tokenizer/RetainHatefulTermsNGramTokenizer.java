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

	//TODO: init again when tokenize is called
	
	public RetainHatefulTermsNGramTokenizer() {

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream("../hatefulTerms.txt"), "UTF-8"));
			stemmer = new SnowballStemmer("german");
			hatefulTermsSet = new HashSet<String>();

			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				hatefulTermsSet.add(stemmer.stem(line).toLowerCase());
			}

			System.out.println("size: " + hatefulTermsSet.size());
			
			currentToken = getNextValidToken();
			hasNext = currentToken != null;		
			nextToken = getNextValidToken();

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
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
		System.out.println("nextElement");
		String tempCurrentToken = currentToken;
		currentToken = nextToken;
		hasNext = currentToken != null;
		nextToken = getNextValidToken();
		
		System.out.println(tempCurrentToken);
		return tempCurrentToken;
	}

	@Override
	public boolean hasMoreElements() {
		System.out.println("hasMoreElements");
		return hasNext;
	}
	
	@Override
	public void tokenize(String s) {
		System.out.println("tokenize");
		super.tokenize(s);
	}


	private String getNextValidToken(){
		System.out.println("has1: " + super.hasMoreElements());
		while(super.hasMoreElements()){
			System.out.println("has2: " + super.hasMoreElements());
			String token = super.nextElement();
			System.out.println("token: " + token);
			if((token.contains(" ") || filterUnigramsToo) && !token.isEmpty()){
				String splitToken[] = token.split(" ");
				for(int i = 0; i < splitToken.length; i++){
					if(tokenFormatTypedDependencies){
						int firstParenthesisIndex = splitToken[i].indexOf("(");
						int commaIndex = splitToken[i].indexOf(",");
						int secondParenthesisIndex = splitToken[i].indexOf(")");

						String firstWord = splitToken[i].substring(firstParenthesisIndex + 1, commaIndex);
						String secondWord = splitToken[i].substring(commaIndex + 1, secondParenthesisIndex);

						if(hatefulTermsSet.contains(stemmer.stem(firstWord).toLowerCase()) ||
								hatefulTermsSet.contains(stemmer.stem(secondWord).toLowerCase())){
							return token;
						}
					}
					else{
						if(hatefulTermsSet.contains(stemmer.stem(splitToken[i]).toLowerCase())){
							return token;
						}
					}
				}
			}
		}
		return null;
	}

}
