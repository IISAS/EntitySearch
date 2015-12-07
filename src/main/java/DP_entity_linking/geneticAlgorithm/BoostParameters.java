package DP_entity_linking.geneticAlgorithm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by miroslav.kudlac on 11/22/2015.
 */
public class BoostParameters implements SekvenciaGenov<Map<String, Float>>, Serializable {
    Map<String, Interval> konfiguracia;

    public BoostParameters() {
        create(null);
    }

    public void create(Random rnd) {
        konfiguracia = new HashMap();
        konfiguracia.put("title", new Interval(0.0f, 1.0f, 100)); // n�zov ?l�nku vo Wikip�dii,
        konfiguracia.put("alt", new Interval(0.0f, 1.0f, 100));  //  alternat�vne men� ?l�nku,
        konfiguracia.put("text", new Interval(0.0f, 1.0f, 100)); //  text ?l�nku,
        konfiguracia.put("sentence", new Interval(0.0f, 1.0f, 100)); //  prv� veta ?l�nku,
        konfiguracia.put("abstract", new Interval(0.0f, 1.0f, 100)); //  abstrakt textu ?l�nku,
        konfiguracia.put("section", new Interval(0.0f, 1.0f, 100)); //  sekcia hlavi?ky ?l�nku,
        konfiguracia.put("fb_name", new Interval(0.0f, 1.0f, 100)); //  Freebase n�zov ?l�nku,
        konfiguracia.put("fb_alias", new Interval(0.0f, 1.0f, 100)); //  alternat�vne n�zvy ?l�nku vo Freebase,

        for (Map.Entry<String, Interval> entry : konfiguracia.entrySet()) {
            if(rnd == null) {
                entry.getValue().setUsek(100); // nastavime vsetkym max hodnotu
            } else {
                entry.getValue().create(rnd); // nastavujeme hodnoty nahodne
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
            // Ked je hodnota vacsia ako 0 tak ju pridam do boostu
            if(x > 0.0f) {
                mapa.put(entry.getKey(), x);
            }
        }
        return mapa;
    }
}