package relation_linking;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNetEngine {

	Dictionary wordnet;
	private Map<String, ArrayList<String>> DBPediaSynsets;
	private Map<String, ArrayList<String>> FreebaseSynsets;
	private LexicalParsingEngine lpe = null;
	private OpenIEEngine openIE = null;
	private QueryStrippingEngine qse = null;
	private double similarity;

	public WordNetEngine(String path, double similarity) throws JWNLException, ClassNotFoundException, IOException {
		System.out.println("Initializing WordNet Search engine...");

		JWNL.initialize(new FileInputStream(path));
		wordnet = Dictionary.getInstance();

		DBPediaSynsets = getSynsetsForDBPedia();
		FreebaseSynsets = getSynsetsForFreebase();
		this.similarity = similarity;
	}

	public WordNetEngine(String path, LexicalParsingEngine lpe, double similarity)
			throws JWNLException, ClassNotFoundException, IOException {
		System.out.println("Initializing WordNet Search engine with lexical parser...");

		JWNL.initialize(new FileInputStream(path));
		wordnet = Dictionary.getInstance();

		DBPediaSynsets = getSynsetsForDBPedia();
		FreebaseSynsets = getSynsetsForFreebase();
		this.lpe = lpe;
		this.similarity = similarity;
	}

	public WordNetEngine(String path, OpenIEEngine openIE, double similarity)
			throws JWNLException, ClassNotFoundException, IOException {
		System.out.println("Initializing WordNet Search engine with OpenIE...");

		JWNL.initialize(new FileInputStream(path));
		wordnet = Dictionary.getInstance();

		DBPediaSynsets = getSynsetsForDBPedia();
		FreebaseSynsets = getSynsetsForFreebase();
		this.openIE = openIE;
		this.similarity = similarity;
	}

	public WordNetEngine(String path, QueryStrippingEngine qse, double similarity)
			throws JWNLException, ClassNotFoundException, IOException {
		System.out.println("Initializing WordNet Search engine with lexical parser...");

		JWNL.initialize(new FileInputStream(path));
		wordnet = Dictionary.getInstance();

		DBPediaSynsets = getSynsetsForDBPedia();
		FreebaseSynsets = getSynsetsForFreebase();
		this.qse = qse;
		this.similarity = similarity;
	}

	private ArrayList<String> getSynsetsFromWord(String relation) throws JWNLException {
		ArrayList<String> results = new ArrayList<String>();

		IndexWordSet indexWordSet = wordnet.lookupAllIndexWords(relation);
		IndexWord[] indexWords = indexWordSet.getIndexWordArray();

		for (IndexWord indexWord : indexWords) {
			for (Synset synset : indexWord.getSenses()) {
				Word[] words = synset.getWords();
				for (Word word : words) {
					if (!results.contains(word.getLemma()))
						results.add(word.getLemma());
				}
			}
		}

		return results;
	}

	@SuppressWarnings("unchecked")
	private Map<String, ArrayList<String>> getSynsets(String filename, boolean freebase)
			throws JWNLException, FileNotFoundException, IOException, ClassNotFoundException {
		File wordnetStore = new File(filename);

		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

		if (!wordnetStore.exists()) {
			ArrayList<String> relations;
			if (freebase) {
				FBCategoriesExtractor fbe = RelationLinkingEngine.getFBCategoriesExtractor();
				relations = fbe.getCategories();
			} else {
				DBPediaOntologyExtractor doe = RelationLinkingEngine.getDBPediaOntologyExtractor();
				relations = doe.getDBPediaRelations();
			}
			for (String relation : relations) {
				ArrayList<String> synsets = getSynsetsFromWord(relation);
				map.put(relation, synsets);
			}

			System.out.println("Writing synsets to file...");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
			oos.writeObject(map);
			oos.flush();
			oos.close();
		} else {
			System.out.println("Reading synsets from file...");
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
			map = (HashMap<String, ArrayList<String>>) ois.readObject();
			ois.close();
		}

		return map;
	}

	private Map<String, ArrayList<String>> getSynsetsForDBPedia()
			throws JWNLException, FileNotFoundException, IOException, ClassNotFoundException {
		System.out.println("Getting synsets for DBPedia...");
		return getSynsets("DBPediaSynsets", false);
	}

	private Map<String, ArrayList<String>> getSynsetsForFreebase()
			throws FileNotFoundException, ClassNotFoundException, JWNLException, IOException {
		System.out.println("Getting synsets for Freebase...");
		return getSynsets("FreebaseSynsets", true);
	}

	private Map<String, Double> getLexicalizedRelations(String sentence) {
		ArrayList<String> nouns = lpe.getNounsFromSentence(sentence);
		return new HashMap<String, Double>();
	}

	private String[] splitRelation(String relation) {
		return relation.split("\\s+");
	}

	private Map<String, Double> getOpenIERelations(String sentence) throws JWNLException {

		ArrayList<String> openIERelations = openIE.getRelations(sentence);
		Map<String, Double> results = new HashMap<String, Double>();

		for (String relation : openIERelations) {
			String[] words = splitRelation(relation);
			for (String word : words) {
				ArrayList<String> synsets = getSynsetsFromWord(word);

				Map<String, Double> relations = isDBPediaRelation(synsets);
				if (relations != null) {
					results.putAll(relations);
				}

				relations = isFBCategory(synsets);
				if (relations != null) {
					results.putAll(relations);
				}
			}
		}

		return results;
	}

	private Map<String, Double> getQueryStrippedRelations(String sentence) throws JWNLException {
		ArrayList<String> strippedWords = qse.getRelations(sentence);
		Map<String, Double> results = new HashMap<String, Double>();

		for (String relation : strippedWords) {
			ArrayList<String> synsets = getSynsetsFromWord(relation);

			Map<String, Double> relations = isDBPediaRelation(synsets);
			if (relations != null) {
				results.putAll(relations);
			}

			relations = isFBCategory(synsets);
			if (relations != null) {
				results.putAll(relations);
			}
		}

		return results;
	}

	private Map<String, Double> getRelations(ArrayList<String> synsets, Map<String, ArrayList<String>> map) {
		Map<String, Double> results = new HashMap<String, Double>();

		for (Entry<String, ArrayList<String>> mapEntry : map.entrySet()) {
			@SuppressWarnings("unchecked")
			ArrayList<String> relSynsets = (ArrayList<String>) synsets.clone();
			relSynsets.removeAll((Collection<?>) mapEntry.getValue());
			if (relSynsets.size() != synsets.size()) {
				System.out.println(relSynsets.size() + "/" + synsets.size());
				double number = (double) relSynsets.size() / (double) synsets.size();
				if (((double) (1 - number)) > similarity) {
					System.out.println(1 - number);
					results.put(mapEntry.getKey(), new Double(1 - number));
				}
			}
		}

		return results;
	}

	private Map<String, Double> isDBPediaRelation(ArrayList<String> synsets) {
		return getRelations(synsets, DBPediaSynsets);
	}

	private Map<String, Double> isFBCategory(ArrayList<String> synsets) {
		return getRelations(synsets, FreebaseSynsets);
	}

	public Map<String, Double> getRelations(String sentence) throws JWNLException {
		System.out.println("Getting WordNet relations...");

		if (lpe != null)
			return getLexicalizedRelations(sentence);

		if (openIE != null)
			return getOpenIERelations(sentence);

		if (qse != null)
			return getQueryStrippedRelations(sentence);

		Map<String, Double> results = new HashMap<String, Double>();

		Reader reader = new StringReader(sentence);

		for (Iterator<List<HasWord>> iterator = new DocumentPreprocessor(reader).iterator(); iterator.hasNext();) {
			List<HasWord> word = iterator.next();

			for (int i = 0; i < word.size(); i++) {
				String sWord = word.get(i).toString();

				ArrayList<String> synsets = getSynsetsFromWord(sWord);

				Map<String, Double> relations = isDBPediaRelation(synsets);
				if (relations != null) {
					results.putAll(relations);
				}

				relations = isFBCategory(synsets);
				if (relations != null) {
					results.putAll(relations);
				}
			}
		}

		return results;
	}

}
