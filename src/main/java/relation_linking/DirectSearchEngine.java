package relation_linking;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class DirectSearchEngine {

	private DBPediaOntologyExtractor doe;
	private FBCategoriesExtractor fce;
	private int matchedInSentence;

	public DirectSearchEngine() {

		System.out.println("Initializing Direct search engine...");

		this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
	}

	protected int getMatchedInSentence() {
		return matchedInSentence;
	}

	protected Map<String, Double> getRelations(String sentence)
			throws FileNotFoundException, UnsupportedEncodingException {

		System.out.println("Getting direct search relations...");

		Map<String, Double> results = new HashMap<String, Double>();

		Reader reader = new StringReader(sentence);

		for (Iterator<List<HasWord>> iterator = new DocumentPreprocessor(reader).iterator(); iterator.hasNext();) {
			List<HasWord> word = iterator.next();

			matchedInSentence = 0;

			for (int i = 0; i < word.size(); i++) {
				String sWord = word.get(i).toString();
				if (isDBPediaRelation(sWord)) {
					results.put(sWord, new Double(1.00));
					matchedInSentence++;
				}
				if (isFBCategory(sWord)) {
					results.put(sWord, new Double(1.00));
					matchedInSentence++;
				}

				String matched = isInComposedDBPediaRelations(word.get(i), word);
				if (matched != null) {
					results.put(matched, new Double(1.00));
					matchedInSentence++;
				}

				matched = isInComposedFBRelations(word.get(i), word);
				if (matched != null) {
					results.put(matched, new Double(1.00));
					matchedInSentence++;
				}
			}
		}

		return results;
	}

	private boolean isDBPediaRelation(String word) {
		return doe.getLowerDBPediaRelations().contains(word.toLowerCase());
	}

	private boolean isFBCategory(String word) {
		return fce.getCategories().contains(word);
	}

	private String findComposedRelation(HasWord word, List<HasWord> sentence, boolean Freebase,
			Map<String, String> cleanTypes) {
		boolean matched = false;
		String key = new String();

		for (Map.Entry<String, String> entry : cleanTypes.entrySet()) {
			if (entry.getValue().toLowerCase().equals(word.toString().toLowerCase())) {
				int wordIndex = sentence.indexOf(word);
				key = entry.getKey();
				key = key.substring(0, key.length() - 1);
				String[] r = Freebase ? fce.splitKey(key) : doe.splitKey(key);
				if (word.toString().toLowerCase().equals(r[0].toLowerCase())) {
					matched = true;
					for (int i = 1; i < r.length; i++) {
						if (sentence.size() < r.length + wordIndex) {
							matched = false;
							break;
						} else if (!r[i].toLowerCase().equals(sentence.get(wordIndex + i).toString().toLowerCase())) {
							matched = false;
							break;
						}
					}
				}
				if (matched) {
					return key;
				}
			}
		}
		return null;
	}

	private String isInComposedDBPediaRelations(HasWord word, List<HasWord> sentence) {
		return findComposedRelation(word, sentence, false, doe.getCleanDBPediaTypes());
	}

	private String isInComposedFBRelations(HasWord word, List<HasWord> sentence) {
		return findComposedRelation(word, sentence, true, fce.getCleanFBCategories());
	}
}
