package relation_linking;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import word2vec.*;

public class GloVeEngine {

	private DBPediaOntologyExtractor doe = null;
	private FBCategoriesExtractor fce = null;
	private LexicalParsingEngine lpe = null;
	private OpenIEEngine openIE = null;
	private QueryStrippingEngine qse = null;

	private boolean allOverSimilarity;
	private double similarity;

	private GloVeSpace model = null;

	public GloVeEngine(String modelPath, double similarity, boolean allOverSimilarity) {

		System.out.println("Initializing Glove search engine...");

		if (model == null) {
			model = new GloVeSpace();
			model = GloVeSpace.load(modelPath, false, false);
		}
		if (this.doe == null)
			this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		if (this.fce == null)
			this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
		this.allOverSimilarity = allOverSimilarity;
	}

	public GloVeEngine(String modelPath, double similarity, LexicalParsingEngine lpe, boolean allOverSimilarity) {
		System.out.println("Initializing Glove search engine with lexical parser...");

		if (model == null) {
			model = new GloVeSpace();
			model = GloVeSpace.load(modelPath, false, false);
		}
		if (this.lpe == null)
			this.lpe = lpe;
		if (this.doe == null)
			this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		if (this.fce == null)
			this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
		this.allOverSimilarity = allOverSimilarity;
	}

	public GloVeEngine(String modelPath, double similarity, OpenIEEngine openIE, boolean allOverSimilarity) {
		System.out.println("Initializing Glove search engine with OpenIE...");

		if (model == null) {
			model = new GloVeSpace();
			model = GloVeSpace.load(modelPath, false, false);
		}
		if (this.openIE == null)
			this.openIE = openIE;
		if (this.doe == null)
			this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		if (this.fce == null)
			this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
		this.allOverSimilarity = allOverSimilarity;
	}

	public GloVeEngine(String modelPath, double similarity, QueryStrippingEngine qse, boolean allOverSimilarity) {
		System.out.println("Initializing Glove search engine with Query stripping...");

		if (model == null) {
			model = new GloVeSpace();
			model = GloVeSpace.load(modelPath, false, false);
		}
		if (this.qse == null)
			this.qse = qse;
		if (this.doe == null)
			this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		if (this.fce == null)
			this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
		this.allOverSimilarity = allOverSimilarity;
	}

	private Map<String, Double> getComposedRelations(ArrayList<String> sentenceParts) {
		Map<String, Double> results = new HashMap<String, Double>();

		for (String sentencePart : sentenceParts) {

			Map<String, Double> relations = isDBPediaRelation(sentencePart);
			if (relations != null) {
				results.putAll(relations);
			}

			relations = isFBCategory(sentencePart);
			if (relations != null) {
				results.putAll(relations);
			}

			relations = isInComposedDBPediaRelations(sentencePart);
			if (relations != null) {
				results.putAll(relations);
			}

			relations = isInComposedFBRelations(sentencePart);
			if (relations != null) {
				results.putAll(relations);
			}
		}

		return results;
	}

	private Map<String, Double> getOpenIERelations(String sentence) {
		return getComposedRelations(openIE.getRelations(sentence));
	}

	private Map<String, Double> getLexicalizedRelations(String sentence) {
		return getComposedRelations(lpe.getPairsFromSentence(sentence));
	}

	private Map<String, Double> getStrippedRelations(String sentence) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> words = qse.getRelations(sentence);

		for (String word : words) {
			sb.append(word);
			sb.append(" ");
		}

		words.clear();
		words.add(sb.toString());

		return getComposedRelations(words);
	}

	public Map<String, Double> getRelations(String sentence) {
		System.out.println("Getting glove relations...");

		if (lpe != null)
			return getLexicalizedRelations(sentence);

		if (openIE != null)
			return getOpenIERelations(sentence);

		if (qse != null)
			return getStrippedRelations(sentence);

		Map<String, Double> results = new HashMap<String, Double>();

		Reader reader = new StringReader(sentence);

		for (Iterator<List<HasWord>> iterator = new DocumentPreprocessor(reader).iterator(); iterator.hasNext();) {
			List<HasWord> word = iterator.next();

			for (int i = 0; i < word.size(); i++) {
				String sWord = word.get(i).toString();

				Map<String, Double> relations = isDBPediaRelation(sWord);
				if (relations != null) {
					results.putAll(relations);
				}

				relations = isFBCategory(sWord);
				if (relations != null) {
					results.putAll(relations);
				}

				relations = isInComposedDBPediaRelations(sWord);
				if (relations != null) {
					results.putAll(relations);
				}

				relations = isInComposedFBRelations(sWord);
				if (relations != null) {
					results.putAll(relations);
				}
			}
		}

		return results;
	}

	private double getSentencesSimilarity(String sentence, String composedRelation) {
		double similarity = 0;
		if (canBeSentenceVectorized(sentence) && canBeSentenceVectorized(composedRelation)) {
			similarity = model.cosineSimilarity(model.sentenceVector(sentence), model.sentenceVector(composedRelation));
		}
		return similarity;
	}

	private double getSimilarity(String sentence, String word) {
		double similarity = 0;
		if (isWordInModel(word) && canBeSentenceVectorized(sentence))
			similarity = model.cosineSimilarity(model.sentenceVector(sentence), model.vector(word));
		return similarity;
	}

	private double getWordsSimilarity(String word1, String word2) {
		double similarity = model.cosineSimilarity(word1, word2);
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

	private Map<String, Double> findComposedRelation(String word, boolean Freebase, Map<String, String> cleanTypes) {
		double maxSimilarity = 0;
		String maxRelation = null;
		String key;
		Map<String, Double> foundRelations = new HashMap<String, Double>();

		for (Map.Entry<String, String> entry : cleanTypes.entrySet()) {
			key = entry.getKey();
			key = key.substring(0, key.length() - 1);
			String[] r = Freebase ? fce.splitKey(key) : doe.splitKey(key);
			String sentence = makeSentenceFromSequence(r);
			double tSim = 0;
			if (lpe != null || qse != null || openIE != null) {
				tSim = getSentencesSimilarity(sentence, word);
			} else {
				tSim = getSimilarity(sentence, word);
			}
			if (tSim > similarity) {
				if (Double.isFinite(tSim)) {
					if (allOverSimilarity) {
						foundRelations.put(key, tSim);
					} else if (tSim > maxSimilarity) {
						maxRelation = key;
						maxSimilarity = tSim;
					}
				}
			}

		}

		if (!allOverSimilarity)
			foundRelations.put(maxRelation, maxSimilarity);

		return foundRelations;
	}

	private Map<String, Double> isInComposedDBPediaRelations(String word) {
		return findComposedRelation(word, false, doe.getCleanDBPediaTypes());
	}

	private Map<String, Double> isInComposedFBRelations(String word) {
		return findComposedRelation(word, true, fce.getCleanFBCategories());
	}

	private Map<String, Double> findRelation(String word, ArrayList<String> relations) {
		double maxSimilarity = 0;
		String maxRelation = null;
		Map<String, Double> foundResults = new HashMap<String, Double>();

		for (String relation : relations) {

			double tSim = 0;
			if (lpe != null || qse != null || openIE != null) {
				tSim = getSimilarity(word, relation);
			} else {
				if (isWordInModel(word) && isWordInModel(relation)) {
					tSim = getWordsSimilarity(word, relation);
				}
			}

			if (tSim > similarity) {
				if (Double.isFinite(tSim)) {

					if (allOverSimilarity) {
						foundResults.put(relation, tSim);
					} else if (tSim > maxSimilarity) {
						maxRelation = relation;
						maxSimilarity = tSim;
					}
				}
			}
		}

		if (!allOverSimilarity)
			foundResults.put(maxRelation, maxSimilarity);

		return foundResults;
	}

	private Map<String, Double> isDBPediaRelation(String word) {
		return findRelation(word, doe.getLowerDBPediaRelations());
	}

	private Map<String, Double> isFBCategory(String word) {
		return findRelation(word, fce.getCategories());
	}
}
