package relation_linking;

import edu.stanford.nlp.process.*;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class LexicalParsingEngine {
	
	LexicalizedParser lp;
	private PrintWriter output;

	public LexicalParsingEngine(String parserModel) throws FileNotFoundException, UnsupportedEncodingException {
		
		System.out.println("Initializing Lexical Parser...");
		lp = LexicalizedParser.loadModel(parserModel);
		output = new PrintWriter("/Users/fjuras/OneDriveBusiness/DPResources/LexicalParser.txt", "UTF-8");
	}

	public void parseSentence(String text) {

		output.println(text);
		TreebankLanguagePack tlp = lp.treebankLanguagePack();
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures()) {
			gsf = tlp.grammaticalStructureFactory();
		}

		Reader reader = new StringReader(text);

		for (List<HasWord> sentence : new DocumentPreprocessor(reader)) {
			Tree parse = lp.apply(sentence);
			parse.pennPrint();
			System.out.println();

			if (gsf != null) {
				GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();
				System.out.println(tdl);
				output.println(tdl);
				output.println();
			}
		}
	}
	
	public void endOfParsing(){
		output.close();
		output = null;
	}

}
