package DP_entity_linking;

import DP_entity_linking.dataset.DataSet;
import DP_entity_linking.dataset.Record;
import DP_entity_linking.geneticAlgorithm.GeneticClass;
import DP_entity_linking.search.Configuration;
import DP_entity_linking.search.DefaultConfiguration;
import DP_entity_linking.search.ResultPreprocessing;
import DP_entity_linking.search.Search;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.queryparser.classic.ParseException;
import util.XLS;

import java.io.IOException;
import java.util.List;
import java.util.Random;
/**
 * Created by miroslav.kudlac on 11/22/2015.
 */
public class Main {
    private static Logger LOGGER = Logger.getLogger(Main.class);
    private XLS xls;


    public void normalStart() throws IOException, ParseException {
        DataSet dataset = new DataSet();
        List<Record> records = dataset.loadWebquestions();
        records = records.subList(0, 10);
        //records = records.subList(0, 2030);
        Configuration conf;
        ResultPreprocessing result = new ResultPreprocessing();
        conf = new DefaultConfiguration();
        Search search = new Search();
        search.start();

        for (Record record : records) {
            LOGGER.info("------------" + record.getUtterance() + "--------------");
            List<String> a = search.processRecord(record, conf);
            if (a.size() > 0) {
                List<List<String>> finalResultPreprocessed = result.results(record.getQuestion(), a);
                LOGGER.info("+++++++++++++++++" + finalResultPreprocessed + "++++++++++==");
            } else {
                LOGGER.info("NOT FOUND");
            }
        }
    }


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, ParseException {

        PropertyConfigurator.configure("data/log4j.properties");
        Random randnum = new Random();
        randnum.setSeed(123456789);
        GeneticClass geneticSolution = new GeneticClass(randnum);

        /*
         for(int i=0; i < 500; i++) {
             System.out.println(randnum.nextFloat());
         }
        */

        try {
            Main app = new Main();
            //geneticSolution.doJob();
            app.normalStart();

        } catch (IOException ex) {
            LOGGER.error(" error: ", ex);
        }

    }
}
