package DP_entity_linking.geneticAlgorithm;

import DP_entity_linking.geneticAlgorithm.similarity.BM25;
import DP_entity_linking.geneticAlgorithm.similarity.DefaultSimilarity;
import DP_entity_linking.geneticAlgorithm.similarity.Dirichlet;
import DP_entity_linking.geneticAlgorithm.similarity.LmJelink;

/**
 * Created by miroslav.kudlac on 4/19/2016.
 */
public class DefaultChromosome extends Chromosome {
    public DefaultChromosome() {
        BM25 confValues = (BM25) this.bm;
        confValues.getValue1().setUsekByPostion(1.16f);
        confValues.getValue2().setUsekByPostion(0.7f);
        BoostParameters param = (BoostParameters) this.fields;
        param.getKonfiguracia().get("title").setUsekByPostion(0.14f);
        param.getKonfiguracia().get("anchor").setUsekByPostion(0.39999998f);
        param.getKonfiguracia().get("alt").setUsekByPostion(0.48f);
        param.getKonfiguracia().get("text").setUsekByPostion(0.97999996f);
        param.getKonfiguracia().get("abstract").setUsekByPostion(0.099999994f);
        param.getKonfiguracia().get("fb_name").setUsekByPostion(0.32f);
        param.getKonfiguracia().get("fb_alias").setUsekByPostion(0.53999996f);
        param.getKonfiguracia().get("section").setUsekByPostion(0.68f);
        param.getKonfiguracia().get("sentence").setUsekByPostion(0.68f);
        param.getKonfiguracia().get("abs").setUsekByPostion(0.58f);
        LmJelink confJel = (LmJelink) this.similarity;
        confJel.getValue().setUsekByPostion(0.000085f);
        Dirichlet confDir = (Dirichlet) this.dir;
        DefaultSimilarity def = (DefaultSimilarity) this.def;

    }
}
