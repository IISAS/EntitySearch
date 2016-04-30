package relation_linking;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import com.ibm.icu.text.DecimalFormat;

import DP_entity_linking.dataset.*;
import net.didion.jwnl.JWNLException;

public class RelationLinkingEngine {

	public enum METHOD_MAPPING_TYPE {
		DIRECT, GLOVE, WORDNET;
	}

	public enum METHOD_DETECTION_TYPE {
		ALL, OPENIE, LEXICALPARSER, QUERYSTRIPPING;
	}

	private boolean directCheck = true;
	private boolean checkGlove = true;
	private boolean checkWordNet = true;

	private boolean withOpenIE = true;
	private boolean withLexicalParser = true;
	private boolean withQueryStripping = true;
	private boolean withEveryWord = true;
	private boolean allOverSimilarity = true;

	private double similarity = 0.1;

	private String datasetPath = "/Users/fjuras/OneDriveBusiness/DPResources/webquestionsRelation.json";
	private String dbPediaOntologyPath = "/Users/fjuras/OneDriveBusiness/DPResources/dbpedia_2015-04.nt";
	private String gloveModelPath = "/Users/fjuras/OneDriveBusiness/DPResources/glove.6B/glove.6B.300d.txt";
	private String lexicalParserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private String csvOutputPath = "/Users/fjuras/OneDriveBusiness/DPResources/Relations.csv";
	private String trainOutputPath = "/Users/fjuras/OneDriveBusiness/DPResources/trainSet";
	private String JWNLPropertiesPath = "file_properties.xml";
	private String entitySearchResultsFilePath = "/Users/fjuras/OneDriveBusiness/DPResources/resultsWebquestions.txt";

	private String outputUtteranceKey = "utterance";
	private String outputRelationKey = "relation";
	private String outputDetectedKey = "detected";
	private String outputFoundRelationsKey = "number of found";
	private String outputDetectedRelationsKey = "number of detected";
	private String outputFromDetectedKey = "detected from";
	private String outputDetectedForKey = "detected for";
	private String outputFromDetectedAllKey = "detected from complete";
	private String outputSeparator = ";";
	private String outputDirectKey = "Direct";
	private String outputGloveLexicalKey = "GloVe_Lexical";
	private String outputGloveOpenIEKey = "GloVe_OpenIE";
	private String outputGloveStrippingKey = "GloVe_QuerryStripping";
	private String outputGloveAllKey = "GloVe_All";
	private String outputWordNetLexicalKey = "WordNet_Lexical";
	private String outputWordNetOpenIEKey = "WordNet_OpenIE";
	private String outputWordNetStrippingKey = "WordNet_QuerryStripping";
	private String outputWordNetAllKey = "WordNet_All";
	private String outputTrueValue = "1";
	private String outputFalseValue = "0";
	private String outputNotFoundValue = "-1";
	private String outputNewLine = "\n";

	private String outputTrainSeparator = " ";
	private String outputCategory = "|a";
	private String outputTrainValueSeparator = ":";

	private static DBPediaOntologyExtractor doe = null;
	private static FBCategoriesExtractor fce = null;

	private FileWriter csvOutput;
	private FileWriter trainOutput;

	private DirectSearchEngine dse = null;
	private GloVeEngine glove = null;
	private WordNetEngine wordnet = null;

	public RelationLinkingEngine() {
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException, JWNLException {

		RelationLinkingEngine rle = new RelationLinkingEngine();
		rle.runDetection();
	}

	private void runDetection() throws IOException, ClassNotFoundException, JWNLException {
		System.out.println("Reading dataset...");
		DataSet dataset = new DataSet(datasetPath);
		List<Record> records = dataset.loadWebquestions();

		csvOutput = new FileWriter(csvOutputPath);
		trainOutput = new FileWriter(trainOutputPath);
		printCSVRow(outputUtteranceKey, outputRelationKey, outputDirectKey, outputGloveLexicalKey, outputGloveOpenIEKey,
				outputGloveStrippingKey, outputGloveAllKey, outputWordNetLexicalKey, outputWordNetOpenIEKey,
				outputWordNetStrippingKey, outputWordNetAllKey, outputDetectedKey, outputDetectedRelationsKey,
				outputFoundRelationsKey, outputFromDetectedKey, outputDetectedForKey, outputFromDetectedAllKey);

		doe = new DBPediaOntologyExtractor(dbPediaOntologyPath);
		fce = new FBCategoriesExtractor();

		LexicalParsingEngine lpe = null;
		OpenIEEngine openIE = null;
		QueryStrippingEngine qse = null;
		if (withLexicalParser)
			lpe = new LexicalParsingEngine(lexicalParserModel);
		if (withOpenIE)
			openIE = new OpenIEEngine();
		if (withQueryStripping)
			qse = new QueryStrippingEngine(entitySearchResultsFilePath);

		if (directCheck)
			dse = new DirectSearchEngine();

		if (checkGlove) {
			glove = GloVeEngine.getInstance();
			if (withLexicalParser) {
				glove.init(gloveModelPath, similarity, lpe, allOverSimilarity);
			}
			if (withOpenIE) {
				glove.init(gloveModelPath, similarity, openIE, allOverSimilarity);
			}
			if (withQueryStripping) {
				glove.init(gloveModelPath, similarity, qse, allOverSimilarity);
			}
			if (withEveryWord) {
				glove.init(gloveModelPath, similarity, allOverSimilarity);
			}
		}

		if (checkWordNet) {
			wordnet = WordNetEngine.getInstance();
			if (withLexicalParser) {
				wordnet.init(JWNLPropertiesPath, lpe, similarity);
			}
			if (withOpenIE) {
				wordnet.init(JWNLPropertiesPath, openIE, similarity);
			}
			if (withQueryStripping) {
				wordnet.init(JWNLPropertiesPath, qse, similarity);
			}

			if (withEveryWord) {
				wordnet.init(JWNLPropertiesPath, similarity);
			}
		}

		for (Record record : records) {
			System.out.println("Processing utterance: " + record.getUtterance());

			Map<String, Result> results = new HashMap<String, Result>();

			if (directCheck)
				results.putAll(addFoundRelations(dse.getRelations(record.getUtterance()), results,
						METHOD_MAPPING_TYPE.DIRECT, null, record));

			if (checkGlove) {
				if (withLexicalParser) {
					results.putAll(addFoundRelations(
							glove.getRelations(record.getUtterance(), METHOD_DETECTION_TYPE.LEXICALPARSER), results,
							METHOD_MAPPING_TYPE.GLOVE, METHOD_DETECTION_TYPE.LEXICALPARSER, record));
				}
				if (withOpenIE) {
					results.putAll(
							addFoundRelations(glove.getRelations(record.getUtterance(), METHOD_DETECTION_TYPE.OPENIE),
									results, METHOD_MAPPING_TYPE.GLOVE, METHOD_DETECTION_TYPE.OPENIE, record));
				}
				if (withQueryStripping) {
					results.putAll(addFoundRelations(
							glove.getRelations(record.getUtterance(), METHOD_DETECTION_TYPE.QUERYSTRIPPING), results,
							METHOD_MAPPING_TYPE.GLOVE, METHOD_DETECTION_TYPE.QUERYSTRIPPING, record));
				}

				if (withEveryWord) {
					results.putAll(
							addFoundRelations(glove.getRelations(record.getUtterance(), METHOD_DETECTION_TYPE.ALL),
									results, METHOD_MAPPING_TYPE.GLOVE, METHOD_DETECTION_TYPE.ALL, record));
				}
			}
			if (checkWordNet) {
				if (withLexicalParser) {
					results.putAll(addFoundRelations(
							wordnet.getRelations(record.getUtterance(), METHOD_DETECTION_TYPE.LEXICALPARSER), results,
							METHOD_MAPPING_TYPE.WORDNET, METHOD_DETECTION_TYPE.LEXICALPARSER, record));
				}
				if (withOpenIE) {
					results.putAll(
							addFoundRelations(wordnet.getRelations(record.getUtterance(), METHOD_DETECTION_TYPE.OPENIE),
									results, METHOD_MAPPING_TYPE.WORDNET, METHOD_DETECTION_TYPE.OPENIE, record));
				}
				if (withQueryStripping) {
					results.putAll(addFoundRelations(
							wordnet.getRelations(record.getUtterance(), METHOD_DETECTION_TYPE.QUERYSTRIPPING), results,
							METHOD_MAPPING_TYPE.WORDNET, METHOD_DETECTION_TYPE.QUERYSTRIPPING, record));
				}

				if (withEveryWord) {
					results.putAll(
							addFoundRelations(wordnet.getRelations(record.getUtterance(), METHOD_DETECTION_TYPE.ALL),
									results, METHOD_MAPPING_TYPE.WORDNET, METHOD_DETECTION_TYPE.ALL, record));
				}
			}

			printFoundRelations(results, record.getUtterance());

		}

		csvOutput.flush();
		trainOutput.close();
	}

	private void printCSVRow(String utteranceValue, String relationValue, String directValue, String gloveLexicalValue,
			String gloveOpenIEValue, String gloveStrippingValue, String gloveAllValue, String wordNetLexicalValue,
			String wordNetOpenIEValue, String wordNetStrippingValue, String wordNetAllValue, String detectedValue,
			String foundValue, String detectedNumberValue, String outputFromDetectedValue,
			String outputDetectedForValue, String outputFromDetectedAllValue) throws IOException {

		csvOutput.append(utteranceValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(relationValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(directValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(gloveLexicalValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(gloveOpenIEValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(gloveStrippingValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(gloveAllValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(wordNetLexicalValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(wordNetOpenIEValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(wordNetStrippingValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(wordNetAllValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(detectedValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(foundValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(detectedNumberValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(outputFromDetectedValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(outputDetectedForValue);
		csvOutput.append(outputSeparator);
		csvOutput.append(outputFromDetectedAllValue);
		csvOutput.append(outputNewLine);
	}

	private void printTrainRow(boolean found, String relationName, Double direct, Double gloveLexical,
			Double gloveOpenIE, Double gloveStripping, Double gloveAll, Double wordnetLexical, Double wordnetOpenIE,
			Double wordnetStripping, Double wordnetAll) throws IOException {

		DecimalFormat formatter = new DecimalFormat("#0.00");

		if (found)
			trainOutput.append(outputTrueValue);
		else
			trainOutput.append(outputNotFoundValue);
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputCategory);
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(relationName);
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputDirectKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(direct));
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputGloveLexicalKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(gloveLexical));
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputGloveOpenIEKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(gloveOpenIE));
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputGloveStrippingKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(gloveStripping));
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputGloveAllKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(gloveAll));
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputWordNetLexicalKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(wordnetLexical));
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputWordNetOpenIEKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(wordnetOpenIE));
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputWordNetStrippingKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(wordnetStripping));
		trainOutput.append(outputTrainSeparator);
		trainOutput.append(outputWordNetAllKey);
		trainOutput.append(outputTrainValueSeparator);
		trainOutput.append(formatter.format(wordnetAll));
		trainOutput.append(outputNewLine);
	}

	private int getNumberOfDetected(Map<String, Result> results) {
		int detected = 0;
		for (Entry<String, Result> result : results.entrySet()) {
			if (result.getValue().isDetected()) {
				detected++;
			}
		}
		return detected;
	}

	private String valueForBool(boolean bool) {
		return bool ? outputTrueValue : outputFalseValue;
	}

	private void printFoundRelations(Map<String, Result> results, String utterance) throws IOException {
		System.out.println("Printing relations...");

		int numberOfDetected = getNumberOfDetected(results);
		int numberOfFound = results.size();

		if (results.isEmpty()) {
			printCSVRow(utterance, outputNotFoundValue, outputNotFoundValue, outputNotFoundValue, outputNotFoundValue,
					outputNotFoundValue, outputNotFoundValue, outputNotFoundValue, outputNotFoundValue,
					outputNotFoundValue, outputNotFoundValue, outputNotFoundValue, String.valueOf(numberOfDetected),
					String.valueOf(numberOfFound), outputNotFoundValue, outputNotFoundValue, outputNotFoundValue);
		} else {
			for (Entry<String, Result> relation : results.entrySet()) {
				Result result = relation.getValue();
				DecimalFormat formatter = new DecimalFormat("#0.00");
				printCSVRow(utterance, result.getName(), formatter.format(result.getDirectSearch()),
						formatter.format(result.getGloveLexicalParserSimilarity()),
						formatter.format(result.getGloveOpenIESimilarity()),
						formatter.format(result.getGloveStrippingSimilarity()),
						formatter.format(result.getGloveAllSimilarity()),
						formatter.format(result.getWordnetLexicalParserSimilarity()),
						formatter.format(result.getWordnetOpenIESimilarity()),
						formatter.format(result.getWordnetStrippingSimilarity()),
						formatter.format(result.getWordnetAllSimilarity()), valueForBool(result.isDetected()),
						String.valueOf(numberOfDetected), String.valueOf(numberOfFound),
						result.getNumberOfRelations().toString(), result.getNumberOfAllRelations().toString(),
						result.getDetectedFor());
				printTrainRow(result.isDetected(), result.getName(), result.getDirectSearch(),
						result.getGloveLexicalParserSimilarity(), result.getGloveOpenIESimilarity(),
						result.getGloveStrippingSimilarity(), result.getGloveAllSimilarity(),
						result.getWordnetLexicalParserSimilarity(), result.getWordnetOpenIESimilarity(),
						result.getWordnetStrippingSimilarity(), result.getWordnetAllSimilarity());
			}
		}
	}

	private boolean isRelationDetected(String relation, Record record) {
		Map<String, ArrayList<String>> relations = record.getRelations();

		for (Entry<String, ArrayList<String>> rel : relations.entrySet()) {
			for (String r : rel.getValue())
				if (r.toLowerCase().compareTo(relation.toLowerCase()) == 0)
					return true;
		}
		return false;
	}

	private Integer getNumberOfRelations(Record record, boolean all) {
		Map<String, ArrayList<String>> relations = record.getRelations();
		if (all)
			return new Integer(relations.size());

		Integer number = new Integer(0);
		for (Entry<String, ArrayList<String>> entry : relations.entrySet()) {
			number += entry.getValue().size();
		}
		return number;
	}

	private String getDetectedFor(Record record, String relation) {
		Map<String, ArrayList<String>> relations = record.getRelations();

		for (Entry<String, ArrayList<String>> entry : relations.entrySet()) {
			for (String rel : entry.getValue())
				if (rel.toLowerCase().equals(relation))
					return entry.getKey();
		}
		return null;
	}

	private Map<String, Result> addFoundRelations(Map<String, Double> relations, Map<String, Result> results,
			METHOD_MAPPING_TYPE mappingType, METHOD_DETECTION_TYPE detectionType, Record record) {

		Result result;

		for (Entry<String, Double> relation : relations.entrySet()) {
			if (relation == null)
				continue;
			if (results.containsKey(relation.getKey().toLowerCase())) {
				result = results.get(relation.getKey().toLowerCase());
				switch (mappingType) {
				case DIRECT:
					result.setDirectSearch(relation.getValue());
					break;
				case GLOVE: {
					switch (detectionType) {
					case ALL:
						result.setGloveAllSimilarity(relation.getValue());
						break;
					case OPENIE:
						result.setGloveOpenIESimilarity(relation.getValue());
						break;
					case LEXICALPARSER:
						result.setGloveLexicalParserSimilarity(relation.getValue());
						break;
					case QUERYSTRIPPING:
						result.setGloveStrippingSimilarity(relation.getValue());
						break;
					default:
						break;
					}
				}
					break;
				case WORDNET:
					switch (detectionType) {
					case ALL:
						result.setWordnetAllSimilarity(relation.getValue());
						break;
					case OPENIE:
						result.setWordnetOpenIESimilarity(relation.getValue());
						break;
					case LEXICALPARSER:
						result.setWordnetLexicalParserSimilarity(relation.getValue());
						break;
					case QUERYSTRIPPING:
						result.setWordnetStrippingSimilarity(relation.getValue());
						break;
					default:
						break;
					}
					break;
				}
			} else {
				result = new Result(relation.getKey(), mappingType, detectionType, relation.getValue());
				result.setDetectedFor(getDetectedFor(record, relation.getKey()));
				results.put(relation.getKey().toLowerCase(), result);
			}

			result.setDetected(isRelationDetected(relation.getKey(), record));
			result.setNumberOfRelations(getNumberOfRelations(record, false));
			result.setNumberOfAllRelations(getNumberOfRelations(record, true));
			results.replace(relation.getKey().toLowerCase(), result);
		}

		return results;
	}

	public static DBPediaOntologyExtractor getDBPediaOntologyExtractor() {
		return doe;
	}

	public static FBCategoriesExtractor getFBCategoriesExtractor() {
		return fce;
	}
}
