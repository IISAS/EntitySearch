package DP_entity_linking.search;

/**
 * Created by miroslav.kudlac on 10/3/2015.
 */

import DP_entity_linking.dataset.Record;
import DP_entity_linking.evalution.IEvaluation;
import DP_entity_linking.evalution.SimpleEvaluation;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class Search {
    private static Logger LOGGER = Logger.getLogger(Search.class);

    private Analyzer analyzer;
    private IEvaluation iEvaluation;
    private IndexSearcher indexSearcher;
    private SearchStatistics statistics;
    private BackMapping backMapping;
    private BackMappingInterface backMapping1;
    private BackMappingInterface backMapping2;

    public Search() throws IOException {
        Directory directory = new MMapDirectory(new File("data/index_wikipedia"));
        backMapping = new BackMapping();
        analyzer = new MyAnalyzer();
        IndexReader  indexreader = IndexReader.open(directory);
        indexSearcher = new IndexSearcher(indexreader);
        iEvaluation = new SimpleEvaluation(indexSearcher);
        backMapping2 = new BackMapping2();
        backMapping1 = new BackMapping1();

    }

    public void start() {
        statistics = new SearchStatistics();
    }

    public List<Document> processRecord(Record r, Configuration conf) throws IOException, ParseException {
        if (conf == null){
            conf = new DefaultConfiguration();
        }
        TokenStream stream = analyzer.tokenStream(null, new StringReader(r.getUtterance()));
        //TokenStream stream = analyzer.tokenStream(null, new StringReader(r.getAnswer()));
        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);

        stream.reset();
        StringBuilder strBuilder = new StringBuilder();
        while (stream.incrementToken()) {
            strBuilder.append(cattr.toString() + " ");
        }
        stream.end();
        stream.close();
        String newQuery = strBuilder.toString().trim();
        return this.retrieve(r, newQuery, conf);
    }

    public int getScore() {
        statistics.print();
        return statistics.getScore();
    }

    public int getCountBackMapped() {
        return statistics.getCountBackMapped();
    }

    /**
     *
     * @param map
     * @return
     */
    private String[] prepareFields( Map<String, Float> map) {
        int size = map.size();
        String[] fields = new String[size];
        int i = 0;
        for( Map.Entry<String, Float> entry : map.entrySet()) {
            fields[i] = entry.getKey();
            i++;
        }
        return fields;
    }

    /**
     *
     * @param ids
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public List<String> mapFbId(String ids) throws ParseException, IOException {
        List<String> titles = new ArrayList<>();
        String result = "";
        String[] idArray = ids.split(",");
        for (String id : idArray){
            id = id.substring(0, id.indexOf("{"));
            String title = null;
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
            QueryParser queryParser = new QueryParser(Version.LUCENE_43, "fb_id_erd", analyzer);
            Query query = queryParser.parse(id);
            Map<String, Float> boost = new HashMap<>();
            boost.put("fb_id_erd", 1f);
            String[] fields = prepareFields(boost);
            //MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_43, fields, analyzer, boost);
            indexSearcher.setSimilarity(new DefaultSimilarity());
            TopDocs results = indexSearcher.search(query, 1);
            ScoreDoc[] hits = results.scoreDocs;
            for (int i = 0; i < hits.length; i++) {
                Document doc = indexSearcher.doc(hits[i].doc);
                title = doc.get("title");
                titles.add(title);
            }
        };
        return titles;
    }

    /**
     *
     * @param queryIn
     * @param conf
     * @return
     * @throws ParseException
     */
    private final Query buildLuceneQuery(final String queryIn, Configuration conf) throws ParseException {
        Query queryL = null;
        String query = queryIn;

        Map<String, Float> boost = conf.getBoost();
        String[] fields = prepareFields(boost);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_43, fields, analyzer, boost);

        queryL = parser.parse(query);
        //BooleanQuery categoryQuery = new BooleanQuery();
        //BooleanQuery mainQuery = new BooleanQuery();
        /*TermQuery catQuery1 = new TermQuery(new Term("db_category", "person"));
        TermQuery catQuery2 = new TermQuery(new Term("db_category", "place"));
        TermQuery catQuery3 = new TermQuery(new Term("db_category", "country"));
        TermQuery catQuery4 = new TermQuery(new Term("db_category", "movie"));
        TermQuery catQuery5 = new TermQuery(new Term("db_category", "language"));
        categoryQuery.add(new BooleanClause(catQuery1, BooleanClause.Occur.SHOULD));
        categoryQuery.add(new BooleanClause(catQuery2, BooleanClause.Occur.SHOULD));
        categoryQuery.add(new BooleanClause(catQuery3, BooleanClause.Occur.SHOULD));
        categoryQuery.add(new BooleanClause(catQuery4, BooleanClause.Occur.SHOULD));
        categoryQuery.add(new BooleanClause(catQuery5, BooleanClause.Occur.SHOULD));
        mainQuery.add(new BooleanClause(queryL, BooleanClause.Occur.SHOULD));
        mainQuery.add(new BooleanClause(categoryQuery, BooleanClause.Occur.MUST));*/
        return queryL;
    }

    /**
     *
     * @param hits
     * @param record
     * @param backMappingMethod
     * @return
     * @throws IOException
     */
    private List<ScoreDoc> addTobackMapping(ScoreDoc[] hits, Record record, BackMappingInterface backMappingMethod) throws IOException {
        boolean backMapped_name = false;
        boolean backMapped = false;
        boolean backMapped_alias = false;
        BackMapping backMappingResult_alias = null;
        BackMapping backMappingResult_name = null;
        BackMapping backMappingResult = null;
        List<ScoreDoc> backMappedResults = new ArrayList<ScoreDoc>();
        ArrayList<String> toBackMapping = new ArrayList<String>();
        for (int i = 0; i < hits.length; i++) {
            Document doc = indexSearcher.doc(hits[i].doc);
            LOGGER.info("title: " + doc.get("title"));
            if (doc.get("fb_id_erd") == null){
                continue;
            }
            IndexableField[] alts = doc.getFields("alt");
            String entittyTitle = doc.get("title");
            List<String> altNames = new ArrayList<String>();
            for (IndexableField indexableField : alts) {
                altNames.add(indexableField.toString());
            }
            altNames.add(entittyTitle);
            String fb_name = doc.get("fb_name");
            String fb_alias = doc.get("fb_alias");
            backMapped_name = false;
            backMapped_alias = false;
            if (fb_name != null) {
                backMappingResult_name = backMappingMethod.backMapped(record.getQuestion(), fb_name);
                backMapped_name =  backMappingResult_name.isMapped();
            }
            if (fb_alias != null) {
                backMappingResult_alias = backMappingMethod.backMapped(record.getQuestion(), fb_name);
                backMapped_name =  backMappingResult_alias.isMapped();
            }
            if (backMapped_name) {
                toBackMapping.add(backMappingResult_name.getWords().toLowerCase());
                backMappedResults.add(hits[i]);
            } else if (backMapped_alias) {
                toBackMapping.add(backMappingResult_alias.getWords().toLowerCase());
                backMappedResults.add(hits[i]);
            } else {
                for (String altName : altNames) {
                    backMappingResult = backMappingMethod.backMapped(record.getQuestion(), altName);
                    backMapped = backMappingResult.isMapped();
                    if (backMapped) {
                        String haystack = backMappingResult.getWords().toLowerCase();
                        toBackMapping.add(haystack);
                        backMappedResults.add(hits[i]);
                        break;
                    }
                }
            }
            //LOGGER.info("title: " + doc.get("title") + " fb_name: " + fb_name + " fb_alias: " + fb_alias);
        }
        backMapping.setToBackMapping(toBackMapping);
        return backMappedResults;
    }

    /**
     *
     * @param record
     * @param query
     * @param conf
     * @return
     * @throws IOException
     * @throws ParseException
     */
    private List<Document> retrieve(Record record, String query, Configuration conf) throws IOException, ParseException {

        List<Document> finalId = new ArrayList<>();

        ArrayList<String> toBackMapping = new ArrayList<String>();
        indexSearcher.setSimilarity(new MultiSimilarity(conf.getSims()));
        Query queryL = this.buildLuceneQuery(query, conf);
        TopDocs results = indexSearcher.search(queryL, conf.getNumSearchRes());
        ScoreDoc[] hits = results.scoreDocs;
        List<ScoreDoc> backMappedResults = new ArrayList<ScoreDoc>();

        String answer =  record.getAnswer();
        float score = iEvaluation.getScore(hits, answer);
        statistics.statisticScore.count(score);

       // LOGGER.info(record.getQuestion() + ", " + answer + " => " + score);
        for (int i = 0; i < hits.length; i++) {
            Document doc = indexSearcher.doc(hits[i].doc);
            statistics.fbStats(doc);
            statistics.dbStats(doc);

        }
    // USE FOR BACKMAPPING
        backMappedResults = addTobackMapping(hits, record, backMapping2);
        if ( backMappedResults.size() > 0) {
            for (int i = 0; i < backMappedResults.size(); i++){
                ScoreDoc hit = backMappedResults.get(i);
                Document finalDoc = indexSearcher.doc(hit.doc);
                //String resultId =  finalDoc.get("title")
                finalId.add(finalDoc);
            }
            //LOGGER.info("FINAL RESULT IS: " + finalId);

        } else {
            backMappedResults = addTobackMapping(hits, record, backMapping1);
            if ( backMappedResults.size() > 0) {
                for (int i = 0; i < backMappedResults.size(); i++){
                    ScoreDoc hit = backMappedResults.get(i);
                    Document finalDoc = indexSearcher.doc(hit.doc);
                    //String wikiID = finalDoc.get("title").trim().replace(" ", "_");
                     finalId.add(finalDoc);
                }
            } else {
                for (int i = 0; i < hits.length; i++) {
                    Document doc = indexSearcher.doc(hits[i].doc);
                    //String wikiID = doc.get("title").trim().replace(" ", "_");
                    finalId.add(doc);
                    return finalId;
                }
            }
        }
        //USE BACKMAPPING TO MAPPED VALUES
        toBackMapping = backMapping.getToBackMapping();
        if (!toBackMapping.isEmpty()) {
            statistics.countBackMapped++;
            //LOGGER.info("QUESTION CAN BE BACKMAPPED: " + record.getQuestion());
            String question = record.getUtterance();
            Collections.sort(toBackMapping, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return t1.length() - s.length();
                }
            });
            for (String backMap : toBackMapping) {
                String[] backArray = backMap.split(" ");
                for (String b : backArray) {
                    question = question.replaceAll(b, "").replace("?", "").trim();
                }
            }
            //LOGGER.info("BACKMAPPED QUERY: " + question);
            if (question.length() > 0) {
                Query queryBackMapped = this.buildLuceneQuery(question, conf);
                TopDocs resultsBackMapped = indexSearcher.search(queryBackMapped, 5);
                ScoreDoc[] hitsBackMapped = resultsBackMapped.scoreDocs;

                answer = record.getAnswer();
                for (int j = 0; j < hitsBackMapped.length; j++) {
                    Document docBackMapped = indexSearcher.doc(hitsBackMapped[j].doc);
                    //String resultBMId =  docBackMapped.get("title").trim().replace(" ", "_");

                   // finalId.add(docBackMapped);
                  //  LOGGER.info("ANSWERS FOR BACKMAPPED QUESTIONS: " + docBackMapped.get("title") + " --- back mapped---");
                }
                //LOGGER.info(question + ", " + answer);

            }
        }
        return finalId;
    }


}
