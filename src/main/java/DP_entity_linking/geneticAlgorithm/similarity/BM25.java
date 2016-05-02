package DP_entity_linking.geneticAlgorithm.similarity;

import DP_entity_linking.geneticAlgorithm.GenSequence;
import DP_entity_linking.geneticAlgorithm.Interval;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

import java.util.Random;

/**
 * Created by miroslav.kudlac on 4/4/2016.
 */
public class BM25 implements GenSequence<Similarity> {
    private Interval value1;
    private Interval value2;

    public BM25() {
        this.value1 = new Interval(1.1f, 1.4f, 100, 1f);
        this.value2 = new Interval(0.65f, 0.8f, 100, 0.8f);
    }

    @Override
    public void mutate(Random rnd) {
        value1.mutate(rnd);
        value2.mutate(rnd);
    }

    @Override
    public void create(Random rnd) {
        this.value1.create(rnd);
        this.value2.create(rnd);
    }

    @Override
    public Similarity get() {
        return new BM25Similarity(value1.getCurrentPosition(), value2.getCurrentPosition());
    }

    @Override
    public GenSequence<Similarity> clone() {
        BM25 clonedBm = new BM25();
        clonedBm.value1 = (Interval) this.value1.clone();
        clonedBm.value2 = (Interval) this.value2.clone();

        return clonedBm;
    }

    public Interval getValue1() {
        return value1;
    }

    public void setValue1(Interval value1) {
        this.value1 = value1;
    }

    public Interval getValue2() {
        return value2;
    }

    public void setValue2(Interval value2) {
        this.value2 = value2;
    }

    @Override
    public String toString() {
        return "BM25{" +
                "value1=" + value1 +
                ", value2=" + value2 +
                '}';
    }
}
