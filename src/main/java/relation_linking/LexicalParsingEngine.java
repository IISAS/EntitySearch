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

	private Collection<TypedDependency> parseSentenceTDL(String text) {
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

			System.out.println(parse.taggedYield());

			if (gsf != null) {
				GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				tdl = gs.allTypedDependencies();
			}
		}
		return tdl;
	}

	private ArrayList<TaggedWord> parseSentenceTD(String text) {
		System.out.println("Parsing sentence...");

		ArrayList<TaggedWord> tw = new ArrayList<TaggedWord>();

		Reader reader = new StringReader(text);

		for (List<HasWord> sentence : new DocumentPreprocessor(reader)) {

			Tree parse = lp.apply(sentence);

			tw = parse.taggedYield();
		}
		return tw;
	}

	public ArrayList<String> getPairsFromSentence(String sentence) {
		Collection<TypedDependency> tdl = parseSentenceTDL(sentence);
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

	public ArrayList<String> getNounsFromSentence(String sentence) {
		ArrayList<TaggedWord> tw = parseSentenceTD(sentence);
		ArrayList<String> nouns = new ArrayList<String>();

		for (TaggedWord t : tw) {
			if (t.tag().startsWith("N")){
				nouns.add(t.value());
			}
		}

		return nouns;
	}
}
