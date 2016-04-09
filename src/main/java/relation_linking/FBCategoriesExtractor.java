package relation_linking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

public class FBCategoriesExtractor {

	ArrayList<String> fbCategories = new ArrayList<String>();
	private Map<String, String> listOfCleanCategories = new HashMap<String, String>();

	public FBCategoriesExtractor() throws IOException, ClassNotFoundException {

		System.out.println("Initializing FBCategories extractor...");
		
		File fbStore = new File("FBStore");
		if (!fbStore.exists() || fbStore.isDirectory()) {
	        Directory directory = new MMapDirectory(new File("/workspace/erd/index_wikipedia"));
			IndexReader  indexReader = IndexReader.open(directory);
			Fields fields = MultiFields.getFields(indexReader);
			Terms terms = fields.terms("fb_category");
			TermsEnum iterator = terms.iterator(null);
			BytesRef byteRef = null;

			while ((byteRef = iterator.next()) != null) {
				String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
				term = term.substring(term.lastIndexOf(".") + 1, term.length());
				if (!fbCategories.contains(term) && !term.isEmpty()) {
					fbCategories.add(term);
					}
			}

			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("FBStore"));
			oos.writeObject(fbCategories);
			oos.flush();
			oos.close();
		} else {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("FBStore"));
			fbCategories = (ArrayList<String>) ois.readObject();
			ois.close();
		}
		
		cleanCategories();
	}

	public ArrayList<String> getCategories() {
		return this.fbCategories;
	}
	
	public String[] splitKey(String key) {
		String[] r = key.split("_");
		return r;
	}
	
	private void cleanCategories(){
		for (String category : fbCategories){
			if (category.contains("_")){
				String[] r = splitKey(category);
				for (int i=0;i<r.length;i++){
					listOfCleanCategories.put(category+i,r[i]);
				}
			}
		}
	}
	
	public Map<String, String> getCleanFBCategories(){
		return this.listOfCleanCategories;
	}

}
