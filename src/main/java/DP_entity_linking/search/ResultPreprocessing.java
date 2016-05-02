package DP_entity_linking.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import java.util.*;

/**
 * Created by miroslav.kudlac on 3/20/2016.
 */
public class ResultPreprocessing {

    private List<Document> newList;
    private List<Integer> indexes;

    public List<Integer> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Integer> indexes) {
        this.indexes = indexes;
    }

    public ResultPreprocessing() {

        newList = new ArrayList<Document>();
        indexes = new ArrayList<Integer>();
    }
    public List<Document> getNewList() {
        return newList;
    }

    public void setNewList(List<Document> newList) {
        this.newList = newList;
    }

    /**
     *
     * @param question
     * @param list
     * @return
     */
    private String findCanonic (String question, List<Document> list) {
        String canonic = list.get(0).get("title").replaceAll(" ", "_");
        for (int i = 0; i < list.size(); i++) {
            boolean stop = false;
            IndexableField[] alts = list.get(i).getFields("alt");
            String fb_name = list.get(i).get("fb_name");
            String fb_alias = list.get(i).get("fb_alias");
            String entittyTitle = list.get(i).get("title");
            List<String> altNames = new ArrayList<String>();
            for (IndexableField indexableField : alts) {
                altNames.add(indexableField.toString());
            }
            altNames.add(entittyTitle);
            altNames.add(fb_name);
            altNames.add(fb_alias);
            for (String altName : altNames) {
                if (altName == null) {
                    continue;
                }
                if (altName.indexOf(">") != -1) {
                    altName = altName.substring(altName.indexOf(":") + 1, altName.indexOf(">") );
                }
                if (question.toLowerCase().contains(altName.toLowerCase())) {
                    canonic = list.get(i).get("title").replaceAll(" ", "_");
                    stop = true;
                    break;
                }
            }
            if (stop) {
                break;
            }
        }
        return canonic;
    }

    /**
     *
     * @param question
     * @param list
     * @return
     */
    public List<List<String>> results(String question, List<Document> list){
        String canonic = findCanonic(question,list);

        List<List<String>> result = new ArrayList<>();
        List<String> canonicList = new ArrayList<String>();
        this.setNewList(list);
        canonicList = this.addCorrespondingEntities(canonic);

        result.add(canonicList);
        question = question.replaceAll("[^a-zA-Z0-9]+", " ").toLowerCase().trim();
        for (Document l : newList) {
            boolean checkAlreadyExist = false;
            for (List<String> res : result){
                if (res.contains(l.get("title").trim().replaceAll(" ", "_"))){
                    checkAlreadyExist = true;
                    continue;
                }
            }
            if (checkAlreadyExist){
                continue;
            }
            List<String> grouped = new ArrayList<String>();

            String prepararedQuery =  l.get("title");
            if (prepararedQuery.indexOf("(") != -1) {
                prepararedQuery = prepararedQuery.substring(0, prepararedQuery.indexOf("("));
            } else if (prepararedQuery.indexOf(",") != -1) {
                prepararedQuery = prepararedQuery.substring(0, prepararedQuery.indexOf(","));
            }
            List<Integer> indexesFirst = this.findIndexes(question, prepararedQuery.trim().replaceAll(" ", "_"));
            if (indexes != null){
                for (Document indexList : newList) {
                    checkAlreadyExist = false;
                    for (List<String> res : result){
                        if (res.contains(indexList.get("title").trim().replaceAll(" ", "_"))){
                            checkAlreadyExist = true;
                            continue;
                        }
                    }
                    if (checkAlreadyExist){
                        continue;
                    }
                    String comparedQuery =  indexList.get("title");
                    if (comparedQuery.indexOf("(") != -1) {
                        comparedQuery = comparedQuery.substring(0, comparedQuery.indexOf("("));
                    } else if (comparedQuery.indexOf(",") != -1) {
                        comparedQuery = comparedQuery.substring(0, comparedQuery.indexOf(","));
                    }
                    List<Integer> comparedIndexes = this.findIndexes(question, comparedQuery.trim().replaceAll(" ", "_"));
                    if (indexesFirst != null) {
                        int size = indexesFirst.size();
                        indexesFirst.retainAll(comparedIndexes);
                        if (size !=  (size - indexesFirst.size())){
                            grouped.add(indexList.get("title").replaceAll(" ", "_"));
                        }
                    }
                }
                if (grouped.size() > 0) {
                    result.add(grouped);
                }
            }
            this.setIndexes(indexesFirst);


        }
        return result;
    }

    /**
     * Find indexes of backmapped words in question
     * @param question
     * @param query
     * @return
     */
    public List<Integer> findIndexes(String question, String query){
        List<Integer> indexes = new ArrayList<Integer>();
        String[] canonicArray =  query.split("_");
        for (String s : canonicArray) {
            int index = question.indexOf(s.toLowerCase().replaceAll("[^a-zA-Z0-9]+", " ").trim());
            if (index > -1){
                indexes.add(index);
            }
        }
        return indexes;
    }

    /**
     * delete corresponding entities to canonic entity
     * @param canonic
     */
    private List<String> addCorrespondingEntities(String canonic){
        String[] canonicArray =  canonic.split("_");
        Collections.sort(Arrays.asList(canonicArray), new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return t1.length() - s.length();
            }
        });
        List<String> canonicList = new ArrayList<String>();
        for (int i = 0; i < newList.size(); i++) {
            boolean delete = false;
            String entittyTitle = newList.get(i).get("title");
                if (entittyTitle.indexOf("(") != -1) {
                    if (entittyTitle.substring(0, entittyTitle.indexOf("(") ).length() >= canonic.toLowerCase().length()) {
                        entittyTitle = entittyTitle.substring(0, entittyTitle.indexOf("("));
                    } else {
                        delete = true;
                    }
                } else if (entittyTitle.indexOf(",") != -1) {
                    if (entittyTitle.substring(0, entittyTitle.indexOf(",") ).length() >= canonic.toLowerCase().length()) {
                        entittyTitle = entittyTitle.substring(0, entittyTitle.indexOf(","));
                    } else {
                        delete = true;
                    }
                }

                if (canonic.toLowerCase().contains(entittyTitle.toLowerCase().trim().replaceAll(" ", "_").trim())
                        || entittyTitle.toLowerCase().trim().replaceAll(" ", "_").contains(canonic.toLowerCase())
                        ) {
                    if (newList.get(i).get("title").replaceAll( " ", "_").length() >= canonic.length()) {
                        canonicList.add(newList.get(i).get("title").replaceAll(" ", "_"));
                        delete = true;
                    } else {
                        delete = true;
                    }

                } else {
                    int countOfMapped = 0;
                    for(String c : canonicArray){
                        if (entittyTitle.toLowerCase().trim().contains(c.toLowerCase()))
                            countOfMapped++;
                    }
                    if(countOfMapped > 0){
                        delete = true;
                    }
                }
            if (delete) {
                newList.remove(i);
                i--;
                if (newList.size() == 1) {
                    i = -1;
                }
            }
        }
        return canonicList;
    }
}
