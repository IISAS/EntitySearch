package DP_entity_linking.geneticAlgorithm;

import DP_entity_linking.dataset.DataSet;
import DP_entity_linking.dataset.Record;
import DP_entity_linking.search.Configuration;
import DP_entity_linking.search.Search;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import util.XLS;

import java.io.IOException;
import java.util.*;

/**
 * Created by miroslav.kudlac on 4/19/2016.
 */
public class GeneticClass {
    private int sizeOfPopulation = 40;
    private int sizeOfGeneration = 15;
    private Random rnd;
    private XLS xls;
    private static Logger LOGGER = Logger.getLogger(GeneticClass.class);
    public GeneticClass(Random rnd) {
        this.rnd = rnd;
        xls = new XLS("populations.xls", sizeOfPopulation + 1, 20);

    }

    /**
     * GENETIC ALGORITHM SEARCH
     *
     * @throws IOException
     * @throws ParseException
     */
    public void doJob() throws IOException, ParseException {
        DataSet dataset = new DataSet("C:\\workspace\\webquestions.json");
        List<Record> records = dataset.loadWebquestions();
        records = records.subList(0, 1000);
        Search search = new Search();

        Chromosome defaultChromosome = new DefaultChromosome();
        List<Chromosome> population = createPopulation(sizeOfPopulation - 1);
        population.add(defaultChromosome);
        int fitness = 0;
        for (int j = 0; j < sizeOfGeneration; j++) {
            evaluatePopulation(population, search, records);
            sortPopulation(population);
            xls.setCell(j, 0, population.get(0).getFitness());
            xls.setCell(j, 1, population.get(0).get());
            population = generateNewPopulation(population);
        }
        xls.write();


    }

    private List<Chromosome> generateNewPopulation(List<Chromosome> population) {
        List<Chromosome> generatedPopulation = new ArrayList<>();
        List<Chromosome> mutatedPopulation;

        generatedPopulation.addAll(population.subList(0, 5));
        mutatedPopulation = mutatePopulation(population.subList(0, 5));
        generatedPopulation.addAll(mutatedPopulation);
        generatedPopulation.addAll(createPopulation(sizeOfPopulation - 10));

        return generatedPopulation;
    }

    private List<Chromosome> mutatePopulation(List<Chromosome> population) {
        List<Chromosome> part = new ArrayList<Chromosome>();
        for (Chromosome chromosome : population) {
            Chromosome newChromosome = (Chromosome) chromosome.clone();
            newChromosome.mutate(rnd);
            part.add(newChromosome);
        }
        return part;
    }

    private void evaluatePopulation(List<Chromosome> population, Search search, List<Record> records) throws IOException, ParseException {
        for (int i = 0; i < population.size(); i++) {
            search.start();
            Configuration conf = population.get(i).get();
            LOGGER.info(conf);
            for (Record record : records) {
                List<Document> a = search.processRecord(record, conf);
            }
            int fitness = search.getScore();
            population.get(i).setFitness(fitness);

        }
    }

    private List<Chromosome> createPopulation(int sizeOfPopulation) {
        List<Chromosome> population = new ArrayList<Chromosome>();
        for (int i = 0; i < sizeOfPopulation; i++) {
            Chromosome chromosome = new Chromosome();
            chromosome.create(rnd);
            population.add(chromosome);
        }
        return population;
    }

    private void sortPopulation(List<Chromosome> population) {
        Collections.sort(population, new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome a, Chromosome b) {
                return Integer.compare(b.getFitness(), a.getFitness());
            }
        });
    }

}
