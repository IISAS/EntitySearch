package DP_entity_linking.search;

import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import util.LineReader;

import java.io.*;
import java.util.Locale;

/**
 * Created by miroslav.kudlac on 10/4/2015.
 */
public class CountrySynonyms {
    private static String FILE_NAME = "data/data/substitution_map.txt";

    public static SynonymMap buildMap() throws IOException {
        LineReader lr = new LineReader(FILE_NAME);
        String mline;
        SynonymMap.Builder builder = new SynonymMap.Builder(true);
        while ((mline = lr.getLine()) != null) {
            String[] tokens = mline.split("\t");
            builder.add(new CharsRef(tokens[0]), new CharsRef(tokens[1]), false); //include ORIG word
        }
        lr.close();
        return builder.build();
    }

    public static void fillMap() throws IOException {
        FileWriter fstream = new FileWriter(FILE_NAME, true);
        BufferedWriter fbw = new BufferedWriter(fstream);
        String[] locales = Locale.getISOCountries();
        for (String locale : locales) {
            Locale obj = new Locale("", locale);
            String row = obj.getCountry().toLowerCase() + "\t" + obj.getDisplayCountry().toLowerCase() + "\n";
            fbw.write(row);
        }
        fbw.flush();
        fbw.close();
    }
}
