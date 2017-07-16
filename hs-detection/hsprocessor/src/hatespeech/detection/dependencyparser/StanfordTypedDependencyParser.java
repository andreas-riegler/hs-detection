package hatespeech.detection.dependencyparser;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.international.negra.NegraPennLanguagePack;

public class StanfordTypedDependencyParser {

	private String parserModel = "../germanPCFG.ser.gz";

	private LexicalizedParser lp;


	public StanfordTypedDependencyParser(){
		this.init();
	}

	public void init(){
		lp = LexicalizedParser.loadModel(parserModel);
	}

	public String parseString(String str){

		String[] sent = { "Das", "ist", "ein", "gro�er", "Hund", "." };
		List<CoreLabel> rawWords = toCoreLabelList(sent);

		Tree parse = lp.apply(rawWords);

		parse.pennPrint();
		
	    TreebankLanguagePack tlp = new NegraPennLanguagePack();
	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
	    System.out.println(tdl);
		
		return "";
	}

	
	public static List<CoreLabel> toCoreLabelList(String... words) {
		List<CoreLabel> sent = new ArrayList<>(words.length);
		for (String word : words) {
			CoreLabel cl = new CoreLabel();
			cl.setValue(word);
			cl.setWord(word);
			sent.add(cl);
		}
		return sent;
	}
	
	public static void main(String[] args) {
		
		StanfordTypedDependencyParser p = new StanfordTypedDependencyParser();
		
		p.parseString("");
	}
}
