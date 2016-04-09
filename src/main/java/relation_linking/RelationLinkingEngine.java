package relation_linking;

import java.io.*;
import java.util.*;

import DP_entity_linking.dataset.*;

public class RelationLinkingEngine {

	private enum METHOD_TYPE {
		DIRECT, GLOVE, WORDNET, OPENIE;
	}

	private static boolean directCheck = false;
	private static boolean checkGlove = true;
	private static boolean checkWordNet = false;
	private static boolean checkOpenIE = false;

	private static boolean withLexicalParser = true;
	
	private static double similarity = 0.9;

	private static String datasetPath = "/Users/fjuras/OneDriveBusiness/DPResources/webquestionsRelationDataset.json";
	private static String dbPediaOntologyPath = "/Users/fjuras/OneDriveBusiness/DPResources/dbpedia_2015-04.nt";
	private static String gloveModelPath = "/Users/fjuras/OneDriveBusiness/DPResources/glove.6B/glove.6B.50d.txt";
	private static String lexicalParserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private static String outputPath = "/Users/fjuras/OneDriveBusiness/DPResources/Relations.csv";

	private static String outputUtteranceKey = "utterance";
	private static String outputRelationKey = "relation";
	private static String outputDetectedKey = "detected";
	private static String outputSeparator = ";";
	private static String outputDirectKey = "Direct";
	private static String outputGloveKey = "GloVe";
	private static String outputWordNetKey = "WordNet";
	private static String outputOpenIEKey = "OpenIE";
	private static String outputTrueValue = "1";
	private static String outputFalseValue = "0";

	private static DBPediaOntologyExtractor doe;
	private static FBCategoriesExtractor fce;

	private static FileWriter output;

	private static DirectSearchEngine dse;
	private static GloVeEngine glove;

	public static void main(String[] args) throws Exception {

		System.out.println("Reading dataset...");
		DataSet dataset = new DataSet(datasetPath);
		List<Record> records = dataset.loadWebquestions();

		output = new FileWriter(outputPath);
		printRow(outputUtteranceKey, outputRelationKey, outputDirectKey, outputGloveKey, outputWordNetKey,
				outputOpenIEKey, outputDetectedKey);

		doe = new DBPediaOntologyExtractor(dbPediaOntologyPath);
		fce = new FBCategoriesExtractor();

		if (directCheck)
			dse = new DirectSearchEngine();

		if (checkGlove) {
			if (withLexicalParser) {
				LexicalParsingEngine lpe = new LexicalParsingEngine(lexicalParserModel);
				glove = new GloVeEngine(gloveModelPath, similarity, lpe);
			} else {
				glove = new GloVeEngine(gloveModelPath, similarity);
			}
		}

		if (checkWordNet)
			System.out.println("ToDo");

		if (checkOpenIE)
			System.out.println("ToDo");

		for (Record record : records) {
			System.out.println("Processing utterance: " + record.getUtterance());
			if (directCheck)
				printFoundRelations(dse.getRelations(record.getUtterance()), METHOD_TYPE.DIRECT, record.getUtterance());

			if (checkGlove)
				printFoundRelations(glove.getRelations(record.getUtterance()), METHOD_TYPE.GLOVE, record.getUtterance());
			
			if (checkWordNet)
				System.out.println("ToDo");

			if (checkOpenIE)
				System.out.println("ToDo");
		}

		output.flush();
		output.close();
	}

	private static String isRelationDetected(String relation) {
		return outputFalseValue;
	}

	private static void printRow(String utteranceValue, String relationValue, String directValue, String gloveValue,
			String wordNetValue, String openIEValue, String detectedValue) throws IOException {
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
		output.append(openIEValue);
		output.append(outputSeparator);
		output.append(detectedValue);
		output.append("\n");
	}

	private static void printFoundRelations(ArrayList<String> relations, METHOD_TYPE methodType, String utterance)
			throws IOException {
		System.out.println("Printing relations...");
		for (String relation : relations) {
			switch (methodType) {
			case DIRECT:
				printRow(utterance, relation, outputTrueValue, outputFalseValue, outputFalseValue, outputFalseValue,
						isRelationDetected(relation));
				break;
			case GLOVE:
				printRow(utterance, relation, outputFalseValue, outputTrueValue, outputFalseValue, outputFalseValue,
						isRelationDetected(relation));
				break;
			case WORDNET:
				printRow(utterance, relation, outputFalseValue, outputFalseValue, outputTrueValue, outputFalseValue,
						isRelationDetected(relation));
				break;
			case OPENIE:
				printRow(utterance, relation, outputFalseValue, outputFalseValue, outputFalseValue, outputTrueValue,
						isRelationDetected(relation));
				break;
			}
		}
	}
	
	public static DBPediaOntologyExtractor getDBPediaOntologyExtractor(){
		return doe;
	}
	
	public static FBCategoriesExtractor getFBCategoriesExtractor(){
		return fce;
	}
}
