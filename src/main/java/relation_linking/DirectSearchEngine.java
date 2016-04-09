package relation_linking;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class DirectSearchEngine {

	private DBPediaOntologyExtractor doe;
	private FBCategoriesExtractor fce;
	private int matchedInSentence;

	public DirectSearchEngine(DBPediaOntologyExtractor doe, FBCategoriesExtractor fce) {

		System.out.println("Initializing Direct search engine...");

		this.doe = doe;
		this.fce = fce;
	}

	protected int getMatchedInSentence() {
		return matchedInSentence;
	}

	protected ArrayList<String> getRelations(String sentence)
			throws FileNotFoundException, UnsupportedEncodingException {

		System.out.println("Getting direct search relations...");
		
		ArrayList<String> results = new ArrayList<String>();

		Reader reader = new StringReader(sentence);

		for (Iterator<List<HasWord>> iterator = new DocumentPreprocessor(reader).iterator(); iterator.hasNext();) {
			List<HasWord> word = iterator.next();

			matchedInSentence = 0;

			for (int i = 0; i < word.size(); i++) {
				String sWord = word.get(i).toString();
				if (isDBPediaRelation(sWord)) {
					results.add(sWord);
					matchedInSentence++;
				}
				if (isFBCategory(sWord)) {
					results.add(sWord);
					matchedInSentence++;
				}
				if (isInComposedDBPediaRelations(word.get(i), word)) {
					results.add(sWord);
					matchedInSentence++;
				}
				if (isInComposedFBRelations(word.get(i), word)) {
					results.add(sWord);
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

	private boolean isInComposedDBPediaRelations(HasWord word, List<HasWord> sentence) {
		boolean matched = false;
		String key = new String();

		for (Map.Entry<String, String> entry : doe.getCleanDBPediaTypes().entrySet()) {
			if (entry.getValue().toLowerCase().equals(word.toString().toLowerCase())) {
				int wordIndex = sentence.indexOf(word);
				key = entry.getKey();
				key = key.substring(0, key.length() - 1);
				String[] r = doe.splitKey(key);
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
					return matched;
				}
			}
		}
		return matched;
	}

	private boolean isInComposedFBRelations(HasWord word, List<HasWord> sentence) {
		boolean matched = false;
		String key = new String();

		for (Map.Entry<String, String> entry : fce.getCleanFBCategories().entrySet()) {
			if (entry.getValue().equals(word.toString().toLowerCase())) {
				int wordIndex = sentence.indexOf(word);
				key = entry.getKey();
				key = key.substring(0, key.length() - 1);
				String[] r = fce.splitKey(key);
				if (word.toString().toLowerCase().equals(r[0])) {
					matched = true;
					for (int i = 1; i < r.length; i++) {
						if (sentence.size() < r.length + wordIndex) {
							matched = false;
							break;
						} else if (!r[i].equals(sentence.get(wordIndex + i).toString().toLowerCase())) {
							matched = false;
							break;
						}
					}
				}
				if (matched) {
					return matched;
				}
			}
		}
		return matched;
	}
}
