package DP_entity_linking.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import util.StopWords;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by miroslav.kudlac on 10/4/2015.
 */
public class MyAnalyzer extends Analyzer {
    private SynonymMap synonyms;
    private StopWords stopWords;

    public MyAnalyzer() throws IOException {
        synonyms = CountrySynonyms.buildMap();
        stopWords = new StopWords(new File("data/data/stop-words_long.txt"));
    }

    /* This is the only function that we need to override for our analyzer.
     * It takes in a java.io.Reader object and saves the tokenizer and list
     * of token filters that operate on it.
     */


    @Override
    protected TokenStreamComponents createComponents(String field, Reader reader) {
        CharArraySet stopSet = CharArraySet.copy(Version.LUCENE_43, stopWords.getStopWords());

        Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_43, reader);
        TokenStream filter = new EmptyStringTokenFilter(tokenizer);
        filter = new LowerCaseFilter(Version.LUCENE_43, filter);
        filter = new StopFilter(Version.LUCENE_43, filter, stopSet);
        filter = new SynonymFilter(filter, synonyms, true);
        //filter = new EnglishMinimalStemFilter(filter);
        return new TokenStreamComponents(tokenizer, filter);
    }
}

