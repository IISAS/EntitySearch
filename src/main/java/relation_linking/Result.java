package relation_linking;

import relation_linking.RelationLinkingEngine.METHOD_TYPE;

public class Result {

	private String relation;
	private boolean directSearch = false;
	private boolean glove = false;
	private boolean wordnet = false;
	private boolean openie = false;
	private boolean detected = false;
	private int number = 0;
	private int numberOfFound = 0;
	private int numberOfDetected = 0;

	public Result(String relation, METHOD_TYPE methodType, int number) {
		this.relation = relation;
		this.number = number;
		switch (methodType) {
		case DIRECT:
			directSearch = true;
			break;
		case GLOVE:
			glove = true;
			break;
		case WORDNET:
			wordnet = true;
			break;
		case OPENIE:
			openie = true;
			break;
		default:
			break;
		}
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public boolean isDirectSearch() {
		return directSearch;
	}

	public void setDirectSearch(boolean directSearch) {
		this.directSearch = directSearch;
	}

	public boolean isGlove() {
		return glove;
	}

	public void setGlove(boolean glove) {
		this.glove = glove;
	}

	public boolean isWordNet() {
		return wordnet;
	}

	public void setWordNet(boolean wordnet) {
		this.wordnet = wordnet;
	}

	public boolean isOpenie() {
		return openie;
	}

	public void setOpenie(boolean openie) {
		this.openie = openie;
	}

	public boolean isDetected() {
		return detected;
	}

	public void setDetected(boolean detected) {
		this.detected = detected;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getNumberOfFound() {
		return numberOfFound;
	}

	public void setNumberOfFound(int numberOfFound) {
		this.numberOfFound = numberOfFound;
	}

	public int getNumberOfDetected() {
		return numberOfDetected;
	}

	public void setNumberOfDetected(int numberOfDetected) {
		this.numberOfDetected = numberOfDetected;
	}

}
