package relation_linking;

import relation_linking.RelationLinkingEngine.METHOD_DETECTION_TYPE;
import relation_linking.RelationLinkingEngine.METHOD_MAPPING_TYPE;

public class Result {

	private String name;
	private Double directSearchSimilarity = 0.00;
	private Double gloveAllSimilarity = 0.00;
	private Double gloveOpenIESimilarity = 0.00;
	private Double gloveLexicalParserSimilarity = 0.00;
	private Double gloveStrippingSimilarity = 0.00;
	private Double wordnetAllSimilarity = 0.00;
	private Double wordnetOpenIESimilarity = 0.00;
	private Double wordnetLexicalParserSimilarity = 0.00;
	private Double wordnetStrippingSimilarity = 0.00;
	private boolean detected = false;

	public Result(String name, METHOD_MAPPING_TYPE mappingType, METHOD_DETECTION_TYPE detectionType,
			Double similarity) {
		switch (mappingType) {
		case DIRECT:
			directSearchSimilarity = similarity;
			break;
		case GLOVE: {
			switch (detectionType) {
			case ALL:
				gloveAllSimilarity = similarity;
				break;
			case OPENIE:
				gloveOpenIESimilarity = similarity;
				break;
			case LEXICALPARSER:
				gloveLexicalParserSimilarity = similarity;
				break;
			case QUERYSTRIPPING:
				gloveStrippingSimilarity = similarity;
				break;
			default:
				break;
			}
		}
			break;
		case WORDNET: {
			switch (detectionType) {
			case ALL:
				wordnetAllSimilarity = similarity;
				break;
			case OPENIE:
				wordnetOpenIESimilarity = similarity;
				break;
			case LEXICALPARSER:
				wordnetLexicalParserSimilarity = similarity;
				break;
			case QUERYSTRIPPING:
				wordnetStrippingSimilarity = similarity;
				break;
			default:
				break;
			}
		}
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

	public Double getGloveAllSimilarity() {
		return gloveAllSimilarity;
	}

	public void setGloveAllSimilarity(Double gloveAllSimilarity) {
		this.gloveAllSimilarity = gloveAllSimilarity;
	}

	public Double getGloveOpenIESimilarity() {
		return gloveOpenIESimilarity;
	}

	public void setGloveOpenIESimilarity(Double gloveOpenIESimilarity) {
		this.gloveOpenIESimilarity = gloveOpenIESimilarity;
	}

	public Double getGloveLexicalParserSimilarity() {
		return gloveLexicalParserSimilarity;
	}

	public void setGloveLexicalParserSimilarity(Double gloveLexicalParserSimilarity) {
		this.gloveLexicalParserSimilarity = gloveLexicalParserSimilarity;
	}

	public Double getGloveStrippingSimilarity() {
		return gloveStrippingSimilarity;
	}

	public void setGloveStrippingSimilarity(Double gloveStrippingSimilarity) {
		this.gloveStrippingSimilarity = gloveStrippingSimilarity;
	}

	public Double getWordnetAllSimilarity() {
		return wordnetAllSimilarity;
	}

	public void setWordnetAllSimilarity(Double wordnetAllSimilarity) {
		this.wordnetAllSimilarity = wordnetAllSimilarity;
	}

	public Double getWordnetOpenIESimilarity() {
		return wordnetOpenIESimilarity;
	}

	public void setWordnetOpenIESimilarity(Double wordnetOpenIESimilarity) {
		this.wordnetOpenIESimilarity = wordnetOpenIESimilarity;
	}

	public Double getWordnetLexicalParserSimilarity() {
		return wordnetLexicalParserSimilarity;
	}

	public void setWordnetLexicalParserSimilarity(Double wordnetLexicalParserSimilarity) {
		this.wordnetLexicalParserSimilarity = wordnetLexicalParserSimilarity;
	}

	public Double getWordnetStrippingSimilarity() {
		return wordnetStrippingSimilarity;
	}

	public void setWordnetStrippingSimilarity(Double wordnetStrippingSimilarity) {
		this.wordnetStrippingSimilarity = wordnetStrippingSimilarity;
	}

}
