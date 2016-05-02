package DP_entity_linking.geneticAlgorithm;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by miroslav.kudlac on 11/22/2015.
 */
public class Interval implements GenSequence<Float>, Serializable {
    float min; // 0.0001f;
    float max; // 1.00f;
    float probability;
    int pocetUsekov; // na kolko usekov to rozdelime?
    int aktualnyUsek;

    public Interval(float min, float max, int pocetUsekov, float probability) {
        this.min = min;
        this.max = max;
        this.probability = probability;
        this.pocetUsekov = pocetUsekov;
    }

    private float interpolacia(float t) {
        return (1 - t) * min + t * max;
    }

     public void setUsek(int x) {
        this.aktualnyUsek = x % (pocetUsekov + 1);
    }

    public void setUsekByPostion(float value) {
        if(value < min || value > max) {
            throw new IllegalStateException();
        }
        for(int usek=0; usek < pocetUsekov; usek++) {
            float position = getPosition(usek);
            if(position > value) {
                setUsek(usek);
                return;
            }
        }
        throw new IllegalStateException();
    }

    public int getUsek() {
        return aktualnyUsek;
    }

    public float getCurrentPosition() {
        return getPosition(this.aktualnyUsek);
    }

    public float getPosition(int usek) {
        float delta = 1.0f / ((float) this.pocetUsekov);
        float pozicia = delta * ((float) usek);
        if (pozicia > 1.0f) {
            pozicia = 1.0f;
        }
        return interpolacia(pozicia);
    }

    public void mutate(Random rnd) {
        float a = rnd.nextFloat();
        if (a <= this.probability) {
            int nextInt = rnd.nextInt(this.pocetUsekov);
            setUsek(nextInt);
        }
    }

    public void create(Random rnd) {
        setUsek(rnd.nextInt(this.pocetUsekov));
    }

    public Float get() {
        return getCurrentPosition();
    }

    @Override
    public GenSequence<Float> clone() {
        Interval clonedInterval = new Interval(this.min, this.max, this.pocetUsekov, this.probability);
        return clonedInterval;
    }

    @Override
    public String toString() {
        return "Interval{" +
                "min=" + min +
                ", max=" + max +
                ", probability=" + probability +
                ", pocetUsekov=" + pocetUsekov +
                ", aktualnyUsek=" + aktualnyUsek +
                '}';
    }
}