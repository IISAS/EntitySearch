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
	private OpenIEEngine openIE = null;
	private QueryStrippingEngine qse = null;

	private boolean allOverSimilarity;
	private double similarity;

	private GloVeSpace model;

	public GloVeEngine(String modelPath, double similarity, boolean allOverSimilarity) {

		System.out.println("Initializing Glove search engine...");

		model = new GloVeSpace();
		model = GloVeSpace.load(modelPath, false, false);
		this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
		this.allOverSimilarity = allOverSimilarity;
	}

	public GloVeEngine(String modelPath, double similarity, LexicalParsingEngine lpe, boolean allOverSimilarity) {
		System.out.println("Initializing Glove search engine with lexical parser...");

		model = new GloVeSpace();
		model = GloVeSpace.load(modelPath, false, false);
		this.lpe = lpe;
		this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
		this.allOverSimilarity = allOverSimilarity;
	}

	public GloVeEngine(String modelPath, double similarity, OpenIEEngine openIE, boolean allOverSimilarity) {
		System.out.println("Initializing Glove search engine with OpenIE...");

		model = new GloVeSpace();
		model = GloVeSpace.load(modelPath, false, false);
		this.openIE = openIE;
		this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
		this.allOverSimilarity = allOverSimilarity;
	}

	public GloVeEngine(String modelPath, double similarity, QueryStrippingEngine qse, boolean allOverSimilarity) {
		System.out.println("Initializing Glove search engine with Query stripping...");

		model = new GloVeSpace();
		model = GloVeSpace.load(modelPath, false, false);
		this.qse = qse;
		this.doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
		this.fce = RelationLinkingEngine.getFBCategoriesExtractor();
		this.similarity = similarity;
		this.allOverSimilarity = allOverSimilarity;
	}

	private ArrayList<String> getComposedRelations(ArrayList<String> sentenceParts) {
		ArrayList<String> results = new ArrayList<String>();

		for (String sentencePart : sentenceParts) {

			ArrayList<String> relations = isDBPediaRelation(sentencePart);
			if (relations != null) {
				results.addAll(relations);
			}

			relations = isFBCategory(sentencePart);
			if (relations != null) {
				results.addAll(relations);
			}

			relations = isInComposedDBPediaRelations(sentencePart);
			if (relations != null) {
				results.addAll(relations);
			}

			relations = isInComposedFBRelations(sentencePart);
			if (relations != null) {
				results.addAll(relations);
			}
		}

		return results;
	}

	private ArrayList<String> getOpenIERelations(String sentence) {
		return getComposedRelations(openIE.getRelations(sentence));
	}

	private ArrayList<String> getLexicalizedRelations(String sentence) {
		return getComposedRelations(lpe.getPairsFromSentence(sentence));
	}

	private ArrayList<String> getStrippedRelations(String sentence) {
		return getComposedRelations(qse.getRelations(sentence));
	}

	public ArrayList<String> getRelations(String sentence) {
		System.out.println("Getting glove relations...");

		if (lpe != null)
			return getLexicalizedRelations(sentence);

		if (openIE != null)
			return getOpenIERelations(sentence);

		if (qse != null)
			return getStrippedRelations(sentence);

		ArrayList<String> results = new ArrayList<String>();

		Reader reader = new StringReader(sentence);

		for (Iterator<List<HasWord>> iterator = new DocumentPreprocessor(reader).iterator(); iterator.hasNext();) {
			List<HasWord> word = iterator.next();

			for (int i = 0; i < word.size(); i++) {
				String sWord = word.get(i).toString();

				ArrayList<String> relations = isDBPediaRelation(sWord);
				if (relations != null) {
					results.addAll(relations);
				}

				relations = isFBCategory(sWord);
				if (relations != null) {
					results.addAll(relations);
				}

				relations = isInComposedDBPediaRelations(sWord);
				if (relations != null) {
					results.addAll(relations);
				}

				relations = isInComposedFBRelations(sWord);
				if (relations != null) {
					results.addAll(relations);
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

	private ArrayList<String> findComposedRelation(String word, boolean Freebase, Map<String, String> cleanTypes) {
		double maxSimilarity = 0;
		String maxRelation = null;
		String key;
		ArrayList<String> foundRelations = new ArrayList<String>();

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
			if (tSim > similarity) {
				if (tSim != Double.MAX_VALUE) {
					if (allOverSimilarity) {
						foundRelations.add(key);
					} else if (tSim > maxSimilarity) {
						maxRelation = key;
						maxSimilarity = tSim;
					}
				}
			}

		}

		if (!allOverSimilarity)
			foundRelations.add(maxRelation);
		
		System.out.println(maxSimilarity);
		return foundRelations;
	}

	private ArrayList<String> isInComposedDBPediaRelations(String word) {
		return findComposedRelation(word, false, doe.getCleanDBPediaTypes());
	}

	private ArrayList<String> isInComposedFBRelations(String word) {
		return findComposedRelation(word, true, fce.getCleanFBCategories());
	}

	private ArrayList<String> findRelation(String word, ArrayList<String> relations) {
		double maxSimilarity = 0;
		String maxRelation = null;
		ArrayList<String> foundResults = new ArrayList<String>();

		for (String relation : relations) {

			double tSim = 0;
			if (lpe != null) {
				tSim = getSimilarity(word, relation);
			} else {
				if (isWordInModel(word) && isWordInModel(relation)) {
					tSim = getWordsSimilarity(word, relation);
				}
			}

			if (tSim > similarity) {

				if (allOverSimilarity) {
					foundResults.add(relation);
				} else if (tSim > maxSimilarity) {
					maxRelation = relation;
					maxSimilarity = tSim;
				}
			}
		}

		if (!allOverSimilarity)
			foundResults.add(maxRelation);

		return foundResults;
	}

	private ArrayList<String> isDBPediaRelation(String word) {
		return findRelation(word, doe.getLowerDBPediaRelations());
	}

	private ArrayList<String> isFBCategory(String word) {
		return findRelation(word, fce.getCategories());
	}
}