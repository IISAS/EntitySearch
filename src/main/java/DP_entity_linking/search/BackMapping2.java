package DP_entity_linking.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by miroslav.kudlac on 3/23/2016.
 */
public class BackMapping2 implements BackMappingInterface {
    private BackMapping backMapping;
    private static String FILE_NAME = "data/data/stop-words_long.txt";
    public BackMapping2() {
        backMapping = new BackMapping();
    }
    @Override
    public BackMapping backMapped(String query, String form) {
        boolean isMapped = false;
        boolean contains;
        int string_count = 0;
        String result = "";
        form = form.replaceAll("[^a-zA-Z0-9]+", " ").trim();
        String [] split = form.split(" ");
        //split = new HashSet<String>(Arrays.asList(split)).toArray(new String[0]);
        if (split.length <= 2) {
            isMapped = query.toLowerCase().contains(form.toLowerCase().trim());
            backMapping.setIsMapped(isMapped);
            if (isMapped) {
                backMapping.setWords(form.trim());
            }
        } else {
            ArrayList<String> permutations = new ArrayList<>();
            try {
                permutations = createPermutations(split);
                if (permutations.size() > 0) {
                    Collections.sort(permutations, new Comparator<String>() {
                        @Override
                        public int compare(String s, String t1) {
                            return t1.length() - s.length();
                        }
                    });
                    for (String s : permutations) {
                        isMapped = query.toLowerCase().contains(s.toLowerCase().trim());
                        backMapping.setIsMapped(isMapped);
                        if (isMapped) {
                            backMapping.setWords(form);
                            continue;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return backMapping;
    }

    /**
     * Create permutations of surface forms for backMapping
     * @param split
     * @return
     * @throws IOException
     */
    private ArrayList<String> createPermutations(String[] split) throws IOException {
        ArrayList<String> newSplit = new ArrayList<>();
        ArrayList<String> permutations = new ArrayList<>();
        newSplit = withOutStopWords(split);
        for (int i = 0; i < newSplit.size(); i++){
            if (Character.isUpperCase(newSplit.get(i).charAt(0))) {
                if (i+1 < newSplit.size()){
                    permutations.add(newSplit.get(i) + " " + newSplit.get(i+1));
                }
                if (i-1 >= 0){
                    String permutation = newSplit.get(i-1) + " " + newSplit.get(i);
                    permutations.add(permutation);
            }
                if (i-1 >= 0 && i+1 < newSplit.size()){
                    permutations.add(newSplit.get(i-1) + " " + newSplit.get(i) + " " +  newSplit.get(i+1));
                }
            }
        }
        if (permutations.size() < 1 && newSplit.size() > 0) {
            Collections.sort(newSplit, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return t1.length() - s.length();
                }
            });
           if (newSplit.get(0).length() >= 5){
               permutations.add(newSplit.get(0));
           }
        }
        Set<String> hs = new HashSet<>();
        hs.addAll(permutations);
        permutations.clear();
        permutations.addAll(hs);
        return permutations;
    }

    /**
     * Remove stop words from split array
     * @param split
     * @return
     * @throws IOException
     */
    private ArrayList<String> withOutStopWords(String[] split) throws IOException {
        HashSet<String> map = new HashSet<>();
        ArrayList<String> newSplit = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(FILE_NAME));
        String line = "";
        while ((line = in.readLine()) != null) {
            String part = line;
            map.add(part);
        }
        in.close();
        for (String s : split){
            if (!map.contains(s.toLowerCase())){
               newSplit.add(s);
            }
        }
        return newSplit;
    }
}
