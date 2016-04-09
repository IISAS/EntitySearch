package relation_linking;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import DP_entity_linking.dataset.*;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class RelationLinkingEngine {
	
	private static boolean checkGlove = true;
	private static boolean checkWordNet = true;
	private static boolean checkOpenIE = true;

	private static PrintWriter output;
	private static DBPediaOntologyExtractor doe;
	private static FBCategoriesExtractor fce;
	private static int numberOfMatches;
	private static GloVeEngine glove;

	public static void main(String[] args) throws Exception {

		DataSet dataset = new DataSet("/Users/fjuras/OneDriveBusiness/DPResources/webquestionsRelationDataset.json");
        List<Record> records = dataset.loadWebquestions();
		doe = new DBPediaOntologyExtractor("/Users/fjuras/OneDriveBusiness/DPResources/dbpedia_2015-04.nt");
		fce = new FBCategoriesExtractor();
		glove = new GloVeEngine(true);
		//wtv = new Word2VecEngine(false);
		//LexicalParsingEngine lp = new LexicalParsingEngine();

		output = new PrintWriter("/Users/fjuras/OneDriveBusiness/DPResources/Relations.txt", "UTF-8");
		numberOfMatches = 0;
		int numberOfRecords = 0;

		for (Record record : records) {
			numberOfRecords++;
			getRelations(record.getUtterance());
			// lp.parseSentence(record.getUtterance());
		}

		output.println(numberOfMatches + " of " + numberOfRecords);
		output.close();

		// lp.endOfParsing();
	}

	private static boolean isDBPediaRelation(String word) {
		return doe.getLowerDBPediaRelations().contains(word.toLowerCase());
	}

	private static boolean isFBCategory(String word) {
		return fce.getCategories().contains(word);
	}

	private static boolean isInComposedDBPediaRelations(HasWord word, List<HasWord> sentence) {
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
					output.println("ComposedDBPedia: " + key);
					return matched;
				}
			}
		}
		return matched;
	}

	private static boolean isInComposedFBRelations(HasWord word, List<HasWord> sentence) {
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
					output.println("ComposedFreebase: " + key);
					return matched;
				}
			}
		}
		return matched;
	}

	private static boolean isSentenceSimilarToWords(String sentence, GloVeEngine w2v) {
		boolean matched = false;

		for (String relation : doe.getLowerDBPediaRelations()) {
			if (w2v.isWordInModel(relation) && w2v.canBeSentenceVectorized(sentence) && w2v.getSimilarity(sentence, relation) > 0.05) {
					matched = true;
					output.println("Word2VecSimilarityWith:" + relation);
			}
		}

		return matched;
	}
	
	private static boolean areWordsSimilar(String word){
		boolean matched = false;
		
		for (String relation : doe.getLowerDBPediaRelations()){
			if (glove.isWordInModel(word) && glove.isWordInModel(relation) && glove.getWordsSimilarity(word, relation)>0.28){
				matched = true;
				output.println("Word2VecSimilarityWith:"+relation);
			}
		}
		return matched;
	}

	private static boolean getRelations(String sentence) throws FileNotFoundException, UnsupportedEncodingException {

		Reader reader = new StringReader(sentence);
		boolean matched = false;

//		if (isSentenceSimilarToWords(sentence, glove)) {
//			matched = true;
//			output.println(sentence);
//		}
//
		for (Iterator<List<HasWord>> iterator = new DocumentPreprocessor(reader).iterator(); iterator.hasNext();) {
			List<HasWord> word = iterator.next();
			matched = false;
			int matchedInSentence = 0;
			for (int i = 0; i < word.size(); i++) {
				if (isDBPediaRelation(word.get(i).toString())) {
					output.println("DBPedia: " + word.get(i));
					output.println(sentence);
					matched = true;
					matchedInSentence++;
				} else if (isFBCategory(word.get(i).toString())) {
					output.println("Freebase: " + word.get(i));
					output.println(sentence);
					matched = true;
					matchedInSentence++;
				} else if (isInComposedDBPediaRelations(word.get(i), word)) {
					output.println(sentence);
					matched = true;
					matchedInSentence++;
				} else if (isInComposedFBRelations(word.get(i), word)) {
					output.println(sentence);
					matched = true;
					matchedInSentence++;
				} else if (areWordsSimilar(word.get(i).toString().toLowerCase())){
					output.println(sentence);
					matched = true;
					matchedInSentence++;
				}
			}

			if (!matched) {
				output.println();
				output.println(sentence);
				output.println();
			} else {
				output.println(matchedInSentence);
				numberOfMatches++;
			}
		}
		
		return matched;
	}
}
