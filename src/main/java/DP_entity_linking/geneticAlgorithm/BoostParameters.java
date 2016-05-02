package DP_entity_linking.geneticAlgorithm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by miroslav.kudlac on 11/22/2015.
 */
public class BoostParameters implements GenSequence<Map<String, Float>>, Serializable {
    Map<String, Interval> konfiguracia;

    public BoostParameters() {
        konfiguracia = new HashMap<>();
        konfiguracia.put("title", new Interval(0.0f, 1.0f, 100, 0.7f)); // n�zov ?l�nku vo Wikip�dii,
        konfiguracia.put("alt", new Interval(0.0f, 1.0f, 100, 0.7f));  //  alternat�vne men� ?l�nku,
        konfiguracia.put("text", new Interval(0.0f, 1.0f, 100, 0.1f)); //  text ?l�nku,
        konfiguracia.put("sentence", new Interval(0.0f, 1.0f, 100, 0.2f)); //  prv� veta ?l�nku,
        konfiguracia.put("abstract", new Interval(0.0f, 1.0f, 100, 0.1f)); //  abstrakt textu ?l�nku,
        konfiguracia.put("section", new Interval(0.0f, 1.0f, 100, 0.1f)); //  sekcia hlavi?ky ?l�nku,
        konfiguracia.put("fb_name", new Interval(0.0f, 1.0f, 100, 0.6f)); //  Freebase n�zov ?l�nku,
        konfiguracia.put("fb_alias", new Interval(0.0f, 1.0f, 100, 0.5f)); //  alternat�vne n�zvy ?l�nku vo Freebase,
        konfiguracia.put("anchor", new Interval(0.0f, 1.0f, 100, 0.2f));
        konfiguracia.put("abs", new Interval(0.0f, 1.0f, 100, 0.1f));

    }

    public void create(Random rnd) {
        for (Map.Entry<String, Interval> entry : konfiguracia.entrySet()) {
            if(rnd == null) {
                entry.getValue().setUsek(100); // set max value
            } else {
                entry.getValue().create(rnd); // set random value
            }
        }
    }

    public void mutate(Random rnd) {
        for (Map.Entry<String, Interval> entry : konfiguracia.entrySet()) {
            entry.getValue().mutate(rnd);
        }

    }

    public Map<String, Float> get() {
        Map<String, Float> mapa = new HashMap();
        for (Map.Entry<String, Interval> entry : konfiguracia.entrySet()) {
            float x = entry.getValue().get();
            // If value is bigger than 0 add to boost
            if(x > 0.0f) {
                mapa.put(entry.getKey(), x);
            }
        }
        return mapa;
    }

    @Override
    public GenSequence<Map<String, Float>> clone() {
        BoostParameters cloneBoost = new BoostParameters();
        for (Map.Entry<String, Interval> entry : konfiguracia.entrySet()) {
            cloneBoost.konfiguracia.put(entry.getKey(), (Interval) entry.getValue().clone());
        }
        return cloneBoost;
    }

    public Map<String, Interval> getKonfiguracia() {
        return konfiguracia;
    }

    @Override
    public String toString() {
        return "BoostParameters{" +
                "konfiguracia=" + konfiguracia +
                '}';
    }
}