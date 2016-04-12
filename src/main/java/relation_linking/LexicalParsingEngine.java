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

	private Collection<TypedDependency> parseSentence(String text) {
		System.out.println("Parsing sentence...");

		Collection<TypedDependency> tdl = null;
		TreebankLanguagePack tlp = lp.treebankLanguagePack();
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures()) {
			gsf = tlp.grammaticalStructureFactory();
		}

		Reader reader = new StringReader(text);

		for (List<HasWord> sentence : new DocumentPreprocessor(reader)) {
			Tree parse = lp.apply(sentence);

			if (gsf != null) {
				GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				tdl = gs.allTypedDependencies();
			}
		}
		return tdl;
	}

	public ArrayList<String> getPairsFromSentence(String sentence) {
		Collection<TypedDependency> tdl = parseSentence(sentence);
		ArrayList<String> pairs = new ArrayList<String>();

		for (TypedDependency td : tdl) {
			StringBuilder sb = new StringBuilder();
			sb.append(td.gov().originalText());
			sb.append(" ");
			sb.append(td.dep().originalText());
			pairs.add(sb.toString());
		}

		return pairs;
	}
}
