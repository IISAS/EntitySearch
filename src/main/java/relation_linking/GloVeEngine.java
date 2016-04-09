package relation_linking;

import word2vec.*;

/**
 * Created by agibsonccc on 10/9/14.
 */
public class GloVeEngine {

	public GenericWordSpace model;

	public GloVeEngine(boolean glove) {

		if (glove) {
			model = new GloVeSpace();
			model = GloVeSpace.load("/Users/fjuras/OneDriveBusiness/DPResources/glove.6B/glove.6B.50d.txt", false,
					false);
			System.out.println(model.sentenceVector("married to") + ":" + model.vector("spouse"));
			System.out.println(model.distanceSimilarity(model.sentenceVector("married to"), model.vector("spouse")));
		} else {
			model = new W2vSpace();
			model = W2vSpace.loadText(
					"/Users/fjuras/OneDriveBusiness/DPResources/freebase-vectors-skipgram1000-en.bin.gz", false, false);
			System.out.println(model.sentenceVector("married to") + ":" + model.vector("spouse"));
			System.out.println(model.distanceSimilarity(model.sentenceVector("married to"), model.vector("spouse")));
		}
	}

	public double getSimilarity(String sentence, String word) {
		System.out.println(sentence + " : " + word + " = ");
		double similarity = model.distanceSimilarity(model.sentenceVector(sentence), model.vector(word));
		System.out.println(similarity);
		return similarity;
	}

	public double getWordsSimilarity(String word1, String word2) {
		System.out.println(word1 + " : " +word2 + " = ");
		double similarity = model.distanceSimilarity(word1, word2);
		System.out.println(similarity);
		return similarity;
	}

	public boolean isWordInModel(String word) {
		return model.contains(word);
	}
	
	public boolean canBeSentenceVectorized(String sentence){
		return model.sentenceVector(sentence) == null ? false : true;
	}
}