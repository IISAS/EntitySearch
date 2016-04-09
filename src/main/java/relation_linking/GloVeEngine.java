package relation_linking;

import word2vec.*;

/**
 * Created by agibsonccc on 10/9/14.
 */
public class GloVeEngine {

	public GenericWordSpace model;

	public GloVeEngine(boolean glove) {

		System.out.println("Initializing Glove search engine...");
		
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
	
	
	// if (isSentenceSimilarToWords(sentence, glove)) {
	// matched = true;
	// output.println(sentence);
	// }
	//
	
	
//	private boolean isSentenceSimilarToWords(String sentence, GloVeEngine w2v) {
//		boolean matched = false;
//
//		for (String relation : doe.getLowerDBPediaRelations()) {
//			if (w2v.isWordInModel(relation) && w2v.canBeSentenceVectorized(sentence)
//					&& w2v.getSimilarity(sentence, relation) > 0.05) {
//				matched = true;
//				output.println("Word2VecSimilarityWith:" + relation);
//			}
//		}
//
//		return matched;
//	}
//
//	private boolean areWordsSimilar(String word) {
//		boolean matched = false;
//
//		for (String relation : doe.getLowerDBPediaRelations()) {
//			if (glove.isWordInModel(word) && glove.isWordInModel(relation)
//					&& glove.getWordsSimilarity(word, relation) > 0.28) {
//				matched = true;
//				output.println("Word2VecSimilarityWith:" + relation);
//			}
//		}
//		return matched;
//	}
	
//	
//	else if (areWordsSimilar(word.get(i).toString().toLowerCase())) {
//		matchedInSentence++;
//	}
	
	
}