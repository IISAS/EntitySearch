package DP_entity_linking.geneticAlgorithm.similarity;

import DP_entity_linking.geneticAlgorithm.GenSequence;
import org.apache.lucene.search.similarities.Similarity;

import java.util.Random;

/**
 * Created by miroslav.kudlac on 4/4/2016.
 */
public class DefaultSimilarity implements GenSequence<Similarity> {
    private Similarity defSim;
    public DefaultSimilarity() {
        defSim = new org.apache.lucene.search.similarities.DefaultSimilarity();
    }

    @Override
    public void mutate(Random rnd) {
    }

    @Override
    public void create(Random rnd) {
    }

    @Override
    public Similarity get() {
        return defSim;
    }

    @Override
    public GenSequence<Similarity> clone() {
        return new DefaultSimilarity();
    }

    @Override
    public String toString() {
        return "DefaultSimilarity{" +
                "defSim=" + defSim +
                '}';
    }
}