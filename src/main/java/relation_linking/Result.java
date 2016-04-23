package relation_linking;

import relation_linking.RelationLinkingEngine.METHOD_TYPE;

public class Result {

	private String name;
	private boolean directSearch = false;
	private boolean glove = false;
	private boolean wordnet = false;
	private boolean openie = false;
	private boolean detected = false;

	public Result(String name, METHOD_TYPE methodType) {
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
		
		this.setName(name);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
