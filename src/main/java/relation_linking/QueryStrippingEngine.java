package relation_linking;

import java.io.*;
import java.util.*;

import util.StopWords;

public class QueryStrippingEngine {

	private String filePath;
	private Map<String, String> entities = null;

	public QueryStrippingEngine(String filePath) throws FileNotFoundException, IOException, ClassNotFoundException {
		this.filePath = filePath;
		if (entities == null)
			getEntities();
	}

	@SuppressWarnings("unchecked")
	private void getEntities() throws FileNotFoundException, IOException, ClassNotFoundException {
		File entitiesFile = new File("src/main/resources/data/entitiesStore");
		entities = new HashMap<String, String>();

		if (entitiesFile.exists()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/main/resources/data/entitiesStore"));
			entities = (Map<String, String>) ois.readObject();
			ois.close();
		} else {
			try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.indexOf("QUESTION:") != -1) {
						String question = line.substring(line.indexOf(":") + 2);
						line = br.readLine();
						String entity = line.substring(line.indexOf(":") + 2);
						entity = entity.replaceAll("_", " ");
						System.out.println(question + ":" + entity);
						entities.put(question, entity);
					}
				}
			}
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/main/resources/data/entitiesStore"));
			oos.writeObject(entities);
			oos.flush();
			oos.close();
		}
	}

	public ArrayList<String> getRelations(String sentence) {
		String entity = entities.get(sentence);

		String[] words = sentence.split("\\s+");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("[^\\w]", "");
		}

		ArrayList<String> listOfWords = new ArrayList<String>();
		for (String word : words) {
			if (!word.isEmpty())
				listOfWords.add(word);
		}

		words = entity.split("\\s+");
		for (String word : words) {
			listOfWords.remove(word);
		}

		StopWords stopWords = new StopWords(
				new File("src/main/resources/data/stop-words_long.txt"));
		Set<String> stopW = stopWords.getStopWords();

		ArrayList<String> copy = new ArrayList<String>(listOfWords);
		for (String word : copy) {
			if (stopW.contains(word)) {
				listOfWords.remove(word);
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String word : listOfWords) {
			sb.append(word);
			sb.append(" ");
		}

		listOfWords.clear();
		listOfWords.add(sb.toString());

		return listOfWords;
	}

}
