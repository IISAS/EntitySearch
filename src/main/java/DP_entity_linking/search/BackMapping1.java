package DP_entity_linking.search;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by miroslav.kudlac on 3/20/2016.
 */
public class BackMapping1 implements BackMappingInterface{
    private BackMapping backMapping;
    public BackMapping1() {
        backMapping = new BackMapping();
    }

    @Override
    public  BackMapping backMapped(String query, String form) {
        boolean isMapped = false;
        boolean contains;
        int string_count = 0;
        String result = "";
        form = form.replaceAll("[^a-zA-Z0-9]+", " ").trim();
        String [] split = form.split(" ");
        split = new HashSet<String>(Arrays.asList(split)).toArray(new String[0]);
        if (split.length > 1 && form.matches(".*\\bdisambiguation\\b.*")) {
            form = form.replace("disambiguation", "");
        }
        if (split.length <= 2) {
            isMapped = query.toLowerCase().contains(form.toLowerCase().trim());
            backMapping.setIsMapped(isMapped);
            if (isMapped) {
                backMapping.setWords(form.trim());
            }
        } else {
            for (String s : split){
                if (s.length() < 3  && !Character.isUpperCase(s.charAt(0))){
                    continue;
                }
                s = s.replaceAll("[^a-zA-Z0-9]+"," ").trim();
                contains = query.toLowerCase().matches(".*\\b" + s.toLowerCase() + "\\b.*");
                    string_count++;
                    if (contains) {
                    result = result + " " + s;
                }
            }
            if (string_count >= 2){
                isMapped = true;
                backMapping.setIsMapped(isMapped);
                backMapping.setWords(form);
            }
        }
        return backMapping;
    }
}
