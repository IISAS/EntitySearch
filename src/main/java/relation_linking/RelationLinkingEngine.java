package relation_linking;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import DP_entity_linking.dataset.*;
import net.didion.jwnl.JWNLException;

public class RelationLinkingEngine {

	public enum METHOD_TYPE {
		DIRECT, GLOVE, WORDNET;
	}

	private boolean directCheck = false;
	private boolean checkGlove = false;
	private boolean checkWordNet = true;

	private boolean withOpenIE = false;
	private boolean withLexicalParser = true;
	private boolean withQueryStripping = false;
	private boolean withEveryWord = false;
	private boolean allOverSimilarity = true;

	private double similarity = 0.5;

	private String datasetPath = "/Users/fjuras/OneDriveBusiness/DPResources/webquestionsRelation.json";
	private String dbPediaOntologyPath = "/Users/fjuras/OneDriveBusiness/DPResources/dbpedia_2015-04.nt";
	private String gloveModelPath = "/Users/fjuras/OneDriveBusiness/DPResources/glove.6B/glove.6B.300d.txt";
	private String lexicalParserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private String outputPath = "/Users/fjuras/OneDriveBusiness/DPResources/Relations.csv";
	private String JWNLPropertiesPath = "file_properties.xml";
	private String entitySearchResultsFilePath = "/Users/fjuras/OneDriveBusiness/DPResources/resultsWebquestions.txt";

	private String outputUtteranceKey = "utterance";
	private String outputRelationKey = "relation";
	private String outputDetectedKey = "detected";
	private String outputFoundRelationsKey = "number of found";
	private String outputDetectedRelationsKey = "number of detected";
	private String outputSeparator = ";";
	private String outputDirectKey = "Direct";
	private String outputGloveKey = "GloVe";
	private String outputWordNetKey = "WordNet";
	private String outputTrueValue = "1";
	private String outputFalseValue = "0";
	private String outputNotFoundValue = "NAN";

	private static DBPediaOntologyExtractor doe;
	private static FBCategoriesExtractor fce;

	private FileWriter output;

	private DirectSearchEngine dse;
	private GloVeEngine glove;
	private WordNetEngine wordnet;

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

		output = new FileWriter(outputPath);
		printRow(outputUtteranceKey, outputRelationKey, outputDirectKey, outputGloveKey, outputWordNetKey,
				outputDetectedKey, outputDetectedRelationsKey, outputFoundRelationsKey);

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
			if (withLexicalParser) {
				glove = new GloVeEngine(gloveModelPath, similarity, lpe, allOverSimilarity);
			}
			if (withOpenIE) {
				glove = new GloVeEngine(gloveModelPath, similarity, openIE, allOverSimilarity);
			}
			if (withQueryStripping) {
				glove = new GloVeEngine(gloveModelPath, similarity, qse, allOverSimilarity);
			}
			if (withEveryWord) {
				glove = new GloVeEngine(gloveModelPath, similarity, allOverSimilarity);
			}
		}

		if (checkWordNet) {
			if (withLexicalParser) {
				wordnet = new WordNetEngine(JWNLPropertiesPath, lpe, similarity);
			}
			if (withOpenIE) {
				wordnet = new WordNetEngine(JWNLPropertiesPath, openIE, similarity);
			}
			if (withQueryStripping) {
				wordnet = new WordNetEngine(JWNLPropertiesPath, qse, similarity);
			}

			if (withEveryWord) {
				wordnet = new WordNetEngine(JWNLPropertiesPath, similarity);
			}
		}

		for (Record record : records) {
			System.out.println("Processing utterance: " + record.getUtterance());

			Map<String, Result> results = new HashMap<String, Result>();

			if (directCheck)
				results.putAll(addFoundRelations(dse.getRelations(record.getUtterance()), results, METHOD_TYPE.DIRECT,
						record));

			if (checkGlove)
				results.putAll(addFoundRelations(glove.getRelations(record.getUtterance()), results, METHOD_TYPE.GLOVE,
						record));

			if (checkWordNet)
				results.putAll(addFoundRelations(wordnet.getRelations(record.getUtterance()), results, METHOD_TYPE.WORDNET,
						record));

			printFoundRelations(results, record.getUtterance());

		}

		output.flush();
		output.close();
	}

	private void printRow(String utteranceValue, String relationValue, String directValue, String gloveValue,
			String wordNetValue, String detectedValue, String foundValue, String detectedNumberValue)
			throws IOException {
		output.append(utteranceValue);
		output.append(outputSeparator);
		output.append(relationValue);
		output.append(outputSeparator);
		output.append(directValue);
		output.append(outputSeparator);
		output.append(gloveValue);
		output.append(outputSeparator);
		output.append(wordNetValue);
		output.append(outputSeparator);
		output.append(detectedValue);
		output.append(outputSeparator);
		output.append(foundValue);
		output.append(outputSeparator);
		output.append(detectedNumberValue);
		output.append("\n");
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
			printRow(utterance, outputNotFoundValue, outputNotFoundValue, outputNotFoundValue, outputNotFoundValue,
					outputNotFoundValue, String.valueOf(numberOfDetected), String.valueOf(numberOfFound));
		} else {
			for (Entry<String, Result> relation : results.entrySet()) {
				Result result = relation.getValue();
				printRow(utterance, result.getName(), result.getDirectSearch().toString(), result.getGlove().toString(),
						result.getWordNet().toString(), valueForBool(result.isDetected()),
						String.valueOf(numberOfDetected), String.valueOf(numberOfFound));
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

	private Map<String, Result> addFoundRelations(Map<String, Double> relations, Map<String, Result> results,
			METHOD_TYPE methodType, Record record) {

		Result result;

		for (Entry<String, Double> relation : relations.entrySet()) {
			if (relation == null)
				continue;
			if (results.containsKey(relation.getKey().toLowerCase())) {
				result = results.get(relation.getKey().toLowerCase());
				switch (methodType) {
				case DIRECT:
					result.setDirectSearch(relation.getValue());
					break;
				case GLOVE:
					result.setGlove(relation.getValue());
					break;
				case WORDNET:
					result.setWordNet(relation.getValue());
					break;
				}
			} else {
				result = new Result(relation.getKey(), methodType, relation.getValue());
				results.put(relation.getKey().toLowerCase(), result);
			}

			result.setDetected(isRelationDetected(relation.getKey(), record));
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
