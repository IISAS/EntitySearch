package relation_linking;

import java.io.*;
import java.util.*;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNetEngine {

	Dictionary wordnet;
	Map<String, ArrayList<String>> DBPediaSynsets;
	Map<String, ArrayList<String>> FreebaseSynsets;

	public WordNetEngine(String path) throws JWNLException, ClassNotFoundException, IOException {
		System.out.println("Initializing WordNet Search engine...");
		
		JWNL.initialize(new FileInputStream(path));
		wordnet = Dictionary.getInstance();

		DBPediaSynsets = getSynsetsForDBPedia();
		FreebaseSynsets = getSynsetsForFreebase();
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

	public ArrayList<String> getRelations(String sentence) {
		ArrayList<String> results = new ArrayList<String>();

		return results;
	}

}
