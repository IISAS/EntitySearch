package relation_linking;

import java.io.*;
import java.util.*;

public class DBPediaOntologyExtractor {

	private ArrayList<String> listOfRelations = new ArrayList<String>();
	private Map<String, String> listOfCleanRelations = new HashMap<String, String>();
	private ArrayList<String> lowerCaseListOfRelations = new ArrayList<String>();

	@SuppressWarnings("unchecked")
	public DBPediaOntologyExtractor(String filePath) throws FileNotFoundException, IOException, ClassNotFoundException {

		System.out.println("Initializing DBPedia Ontology extractor...");
		
		File dbPediaStore = new File("DBPediaStore");

		if (!dbPediaStore.exists() || dbPediaStore.isDirectory()) {
			try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
				String line;
				String relation = new String();
				while ((line = br.readLine()) != null) {
					if (line.indexOf("<http://dbpedia.org/ontology/") != -1) {
						relation = getRelation(line);

						if (!listOfRelations.contains(relation) && !relation.isEmpty()) {
							listOfRelations.add(relation);
						}
					}
				}
			}
			
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("DBPediaStore"));
			oos.writeObject(listOfRelations);
			oos.flush();
			oos.close();
		} else {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("DBPediaStore"));
			listOfRelations = (ArrayList<String>) ois.readObject();
			ois.close();
		}
		cleanDBPediaTypes();
		toLowerCaseTypes();
	}

	private String getRelation(String line) {
		String ontologyLink = line.substring(line.indexOf("<"), line.indexOf(">") + 1);
		return ontologyLink.substring(ontologyLink.lastIndexOf("/") + 1, ontologyLink.indexOf(">"));
	}

	public ArrayList<String> getDBPediaRelations() {
		return this.listOfRelations;
	}
	
	public ArrayList<String> getLowerDBPediaRelations(){
		return this.lowerCaseListOfRelations;
	}
	
	public String[] splitKey(String key) {
		String[] r = key.split("(?=\\p{Upper})");
		return r;
	}
	
	private void cleanDBPediaTypes(){
		for (String relation : listOfRelations){
			if(!relation.equals(relation.toLowerCase())){
				String[] r = splitKey(relation);
				for (int i=0;i<r.length;i++){
					listOfCleanRelations.put(relation+i,r[i]);
				}
			}
		}
	}
	
	private void toLowerCaseTypes(){
		for (int i=0;i<listOfRelations.size();i++){
			lowerCaseListOfRelations.add(listOfRelations.get(i).toLowerCase());
		}
	}
	
	public Map<String, String> getCleanDBPediaTypes(){
		return this.listOfCleanRelations;
	}

}
