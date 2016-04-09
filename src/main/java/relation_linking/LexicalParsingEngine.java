package relation_linking;

import edu.stanford.nlp.process.*;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class LexicalParsingEngine {
	
	LexicalizedParser lp;

	public LexicalParsingEngine(String parserModel) throws FileNotFoundException, UnsupportedEncodingException {
		
		System.out.println("Initializing Lexical Parser...");
		lp = LexicalizedParser.loadModel(parserModel);
	}

	public void parseSentence(String text) {
		System.out.println("Parsing sentence...");
		
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
			}
		}
	}
	

}
