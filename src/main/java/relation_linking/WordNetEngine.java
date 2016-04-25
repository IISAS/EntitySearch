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

	public WordNetEngine(String path, OpenIEEngine openIE, double similarity) throws JWNLException, ClassNotFoundException, IOException {
		System.out.println("Initializing WordNet Search engine with OpenIE...");

		JWNL.initialize(new FileInputStream(path));
		wordnet = Dictionary.getInstance();

		DBPediaSynsets = getSynsetsForDBPedia();
		FreebaseSynsets = getSynsetsForFreebase();
		this.openIE = openIE;
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

	private ArrayList<String> getLexicalizedRelations(String sentence) {
		return new ArrayList<String>();
	}

	private String[] splitRelation(String relation) {
		return relation.split("\\s+");
	}

	private ArrayList<String> getOpenIERelations(String sentence) throws JWNLException {

		ArrayList<String> openIERelations = openIE.getRelations(sentence);
		ArrayList<String> results = new ArrayList<String>();

		for (String relation : openIERelations) {
			String[] words = splitRelation(relation);
			for (String word : words) {
				ArrayList<String> synsets = getSynsetsFromWord(word);

				ArrayList<String> relations = isDBPediaRelation(synsets);
				if (relations != null) {
					results.addAll(relations);
				}

				relations = isFBCategory(synsets);
				if (relations != null) {
					results.addAll(relations);
				}
			}
		}

		return results;
	}

	private ArrayList<String> getRelations(ArrayList<String> synsets, Map<String, ArrayList<String>> map) {
		ArrayList<String> results = new ArrayList<String>();

		for (Entry<String, ArrayList<String>> mapEntry : map.entrySet()) {
			@SuppressWarnings("unchecked")
			ArrayList<String> relSynsets = (ArrayList<String>) synsets.clone();
			relSynsets.removeAll((Collection<?>) mapEntry.getValue());
			if (relSynsets.size() != synsets.size()) {
				if (relSynsets.size() < similarity * synsets.size())
					results.add(mapEntry.getKey());
			}
		}

		return results;
	}

	private ArrayList<String> isDBPediaRelation(ArrayList<String> synsets) {
		return getRelations(synsets, DBPediaSynsets);
	}

	private ArrayList<String> isFBCategory(ArrayList<String> synsets) {
		return getRelations(synsets, FreebaseSynsets);
	}

	public ArrayList<String> getRelations(String sentence) throws JWNLException {
		System.out.println("Getting WordNet relations...");

		if (lpe != null)
			return getLexicalizedRelations(sentence);

		if (openIE != null)
			return getOpenIERelations(sentence);

		ArrayList<String> results = new ArrayList<String>();

		Reader reader = new StringReader(sentence);

		for (Iterator<List<HasWord>> iterator = new DocumentPreprocessor(reader).iterator(); iterator.hasNext();) {
			List<HasWord> word = iterator.next();

			for (int i = 0; i < word.size(); i++) {
				String sWord = word.get(i).toString();

				ArrayList<String> synsets = getSynsetsFromWord(sWord);

				ArrayList<String> relations = isDBPediaRelation(synsets);
				if (relations != null) {
					results.addAll(relations);
				}

				relations = isFBCategory(synsets);
				if (relations != null) {
					results.addAll(relations);
				}
			}
		}

		return results;
	}

}
