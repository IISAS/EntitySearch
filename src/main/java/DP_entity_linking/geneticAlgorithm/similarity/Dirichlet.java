package DP_entity_linking.geneticAlgorithm.similarity;

import DP_entity_linking.geneticAlgorithm.GenSequence;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import java.util.Random;

/**
 * Created by miroslav.kudlac on 4/5/2016.
 */
public class Dirichlet implements GenSequence<Similarity> {
    private Similarity dirichlet;

    public Dirichlet() {
        dirichlet = new LMDirichletSimilarity();
    }

    @Override
    public void mutate(Random rnd) {
    }

    @Override
    public void create(Random rnd) {
    }

    @Override
    public Similarity get() {
        return dirichlet;
    }

    @Override
    public GenSequence<Similarity> clone() {
        return new Dirichlet();
    }

    @Override
    public String toString() {
        return "Dirichlet{" +
                "dirichlet=" + dirichlet +
                '}';
    }
}
