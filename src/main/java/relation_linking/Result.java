package relation_linking;

import relation_linking.RelationLinkingEngine.METHOD_TYPE;

public class Result {

	private String name;
	private Double directSearchSimilarity = 0.0;
	private Double gloveSimilarity = 0.0;
	private Double wordnetSimilarity = 0.0;
	private boolean detected = false;

	public Result(String name, METHOD_TYPE methodType, Double similarity) {
		switch (methodType) {
		case DIRECT:
			directSearchSimilarity = similarity;
			break;
		case GLOVE:
			gloveSimilarity = similarity;
			break;
		case WORDNET:
			wordnetSimilarity = similarity;
			break;
		default:
			break;
		}
		
		this.setName(name);
	}

	public Double getDirectSearch() {
		return directSearchSimilarity;
	}

	public void setDirectSearch(Double directSearch) {
		this.directSearchSimilarity = directSearch;
	}

	public Double getGlove() {
		return gloveSimilarity;
	}

	public void setGlove(Double glove) {
		this.gloveSimilarity = glove;
	}

	public Double getWordNet() {
		return wordnetSimilarity;
	}

	public void setWordNet(Double wordnet) {
		this.wordnetSimilarity = wordnet;
	}

	public boolean isDetected() {
		return detected;
	}

	public void setDetected(boolean detected) {
		this.detected = detected;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
