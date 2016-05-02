package DP_entity_linking.search;

import org.apache.lucene.search.similarities.Similarity;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by miroslav.kudlac on 11/22/2015.
 */
public class Configuration {
    private Map<String, Float> boost;
    private Similarity[] sims;
    private int numSearchRes = 20;

    @Override
    public String toString() {
        return "Configuration{" +
                "boost=" + boost +
                ", sims=" + Arrays.toString(sims) +
                ", numSearchRes=" + numSearchRes +
                '}';
    }

    public Configuration() {
    }

    public Map<String, Float> getBoost() {
        return boost;
    }

    public void setBoost(Map<String, Float> boost) {
        this.boost = boost;
    }

    public Similarity[] getSims() {
        return sims;
    }

    public void setSims(Similarity[] sims) {
        this.sims = sims;
    }

    public int getNumSearchRes() {
        return numSearchRes;
    }

    public void setNumSearchRes(int numSearchRes) {
        this.numSearchRes = numSearchRes;
    }

}
