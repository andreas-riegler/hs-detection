package hatespeech.detection.hsprocessor;

import hatespeech.detection.model.SpellCheckedMessage;

import java.io.IOException;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AustrianGerman;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

public class SpellCorrector {

	private JLanguageTool langTool;
	public SpellCorrector()
	{
		German lang= new GermanyGerman();
		langTool= new JLanguageTool(lang);
		
		langTool.disableCategory("Typographie");
		langTool.disableCategory("Groﬂ-/Kleinschreibung");
		langTool.disableRule("UNPAIRED_BRACKETS");
		langTool.disableRule("UPPERCASE_SENTENCE_START");
		langTool.disableRule("DE_CASE");
		langTool.disableRule("AUSLASSUNGSPUNKTE");
		langTool.disableRule("WHITESPACE_RULE");
		langTool.disableRule("COMMA_PARENTHESIS_WHITESPACE");
		langTool.disableRule("SUBJUNKTION_KOMMA");
		langTool.disableRule("DE_DOUBLE_PUNCTUATION");
		langTool.disableRule("DE_SENTENCE_WHITESPACE");
		
	}
	public SpellCheckedMessage findMistakes(String message)
	{
		int upperCaseMistakes=0,exclMarkMistakes=0,mistakes;
		double weightedMistakes=0;
		List<RuleMatch> matches=null;
		
		try {
			matches =langTool.check(message);
			
			for (RuleMatch match : matches) {
				
					/**
				  System.out.println("Potential error at line " +
				      match.getLine() + ", column " +
				      match.getColumn() + ": " + match.getMessage());
				  System.out.println("Suggested correction: " +
				      match.getSuggestedReplacements());
				  System.out.println("Rule-ID: "+match.getRule().getId()+" Category: "+match.getRule().getCategory());
				  String mistake=message.substring(match.getFromPos(),match.getToPos());
				  System.out.println(mistake);
				  **/
				  if(match.getRule().getId().equals("DOPPELTES_AUSRUFEZEICHEN"))
				  {
					  exclMarkMistakes++;
				  }
				 /**
				if (match.getSuggestedReplacements().size() > 0) {
					if (match.getSuggestedReplacements().get(0).toLowerCase()
							.equals(mistake)) {
						upperCaseMistakes++;
					}
				}**/
				
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		mistakes=matches.size()-upperCaseMistakes;
		weightedMistakes=(double)mistakes/(double)message.split(" ").length;
		
		
		return new SpellCheckedMessage(weightedMistakes,mistakes,exclMarkMistakes);
	}
	
	
}
