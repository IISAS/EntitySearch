package relation_linking;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import word2vec.*;

public class GloVeEngine {

	private DBPediaOntologyExtractor doe;
	private FBCategoriesExtractor fce;
	private LexicalParsingEngine lpe = null;

	private double similarity;

	private GloVeSpace model;

	public GloVeEngine(String modelPath, double similarity) {

		System.out.println("Initializing Glove search engine...");

		model = new GloVeSpace();
		model = GloVeSpace.load(modelPath, false, false);
		this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
	}

	public GloVeEngine(String modelPath, double similarity, LexicalParsingEngine lpe) {
		System.out.println("Initializing Glove search engine with lexical parser...");

		model = new GloVeSpace();
		model = GloVeSpace.load(modelPath, false, false);
		this.lpe = lpe;
		this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
	}

	private ArrayList<String> getLexicalizedRelations(String sentence) {
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<String> pairs = lpe.getPairsFromSentence(sentence);

		for (String pair : pairs) {

			String relation = isDBPediaRelation(pair);
			if (relation != null) {
				results.add(relation);
			}

			relation = isFBCategory(pair);
			if (relation != null) {
				results.add(relation);
			}

			relation = isInComposedDBPediaRelations(pair);
			if (relation != null) {
				results.add(relation);
			}

			relation = isInComposedFBRelations(pair);
			if (relation != null) {
				results.add(relation);
			}
		}

		return results;
	}

	public ArrayList<String> getRelations(String sentence) {
		System.out.println("Getting glove relations...");

		if (lpe != null)
			return getLexicalizedRelations(sentence);

		ArrayList<String> results = new ArrayList<String>();

		Reader reader = new StringReader(sentence);

		for (Iterator<List<HasWord>> iterator = new DocumentPreprocessor(reader).iterator(); iterator.hasNext();) {
			List<HasWord> word = iterator.next();

			for (int i = 0; i < word.size(); i++) {
				String sWord = word.get(i).toString();

				String relation = isDBPediaRelation(sWord);
				if (relation != null) {
					results.add(relation);
				}

				relation = isFBCategory(sWord);
				if (relation != null) {
					results.add(relation);
				}

				relation = isInComposedDBPediaRelations(sWord);
				if (relation != null) {
					results.add(relation);
				}

				relation = isInComposedFBRelations(sWord);
				if (relation != null) {
					results.add(relation);
				}
			}
		}

		return results;
	}

	private double getSentencesSimilarity(String sentence, String composedRelation) {
		double similarity = 0;
		if (canBeSentenceVectorized(sentence) && canBeSentenceVectorized(composedRelation)) {
			similarity = model.distanceSimilarity(model.sentenceVector(sentence),
					model.sentenceVector(composedRelation));
		}
		return similarity;
	}

	private double getSimilarity(String sentence, String word) {
		double similarity = 0;
		if (isWordInModel(word) && canBeSentenceVectorized(sentence))
			similarity = model.distanceSimilarity(model.sentenceVector(sentence), model.vector(word));
		return similarity;
	}

	private double getWordsSimilarity(String word1, String word2) {
		double similarity = model.distanceSimilarity(word1, word2);
		return similarity;
	}

	private boolean isWordInModel(String word) {
		return model.contains(word);
	}

	private boolean canBeSentenceVectorized(String sentence) {
		return model.sentenceVector(sentence) == null ? false : true;
	}

	private String makeSentenceFromSequence(String[] r) {
		StringBuilder sentence = new StringBuilder();
		for (int i = 0; i < r.length; i++) {
			sentence.append(r[i]);
			sentence.append(" ");
		}
		return sentence.toString();
	}

	private String findComposedRelation(String word, boolean Freebase, Map<String, String> cleanTypes) {
		double maxSimilarity = 0;
		String maxRelation = null;
		String key;

		for (Map.Entry<String, String> entry : cleanTypes.entrySet()) {
			key = entry.getKey();
			key = key.substring(0, key.length() - 1);
			String[] r = Freebase ? fce.splitKey(key) : doe.splitKey(key);
			String sentence = makeSentenceFromSequence(r);
			double tSim = 0;
			if (lpe != null) {
				tSim = getSentencesSimilarity(sentence, word);
			} else {
				tSim = getSimilarity(sentence, word);
			}
			if (tSim > similarity && tSim > maxSimilarity) {
				maxRelation = key;
				maxSimilarity = tSim;
			}

		}
		return maxRelation;
	}

	private String isInComposedDBPediaRelations(String word) {
		return findComposedRelation(word, false, doe.getCleanDBPediaTypes());
	}

	private String isInComposedFBRelations(String word) {
		return findComposedRelation(word, true, fce.getCleanFBCategories());
	}

	private String findRelation(String word, ArrayList<String> relations) {
		double maxSimilarity = 0;
		String maxRelation = null;

		for (String relation : relations) {

			double tSim = 0;
			if (lpe != null) {
				tSim = getSimilarity(word, relation);
			} else {
				if (isWordInModel(word) && isWordInModel(relation)) {
					tSim = getWordsSimilarity(word, relation);
				}
			}

			if (tSim > similarity && tSim > maxSimilarity) {
				maxRelation = relation;
				maxSimilarity = tSim;
			}
		}
		return maxRelation;
	}

	private String isDBPediaRelation(String word) {
		return findRelation(word, doe.getLowerDBPediaRelations());
	}

	private String isFBCategory(String word) {
		return findRelation(word, fce.getCategories());
	}
}