package DP_entity_linking.dataset;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by miroslav.kudlac on 10/3/2015.
 */
public class Record {
    private String utterance;
    private URI url;
    private String targetValue;
    private ArrayList<String> relations;

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public String getUtterance() {
        return utterance;
    }
    
	public ArrayList<String> getRelations() {
		return relations;
	}

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    public String getAnswer() {
        String path = this.getUrl().getPath();
        path = path.substring(path.lastIndexOf('/') + 1);
        String answer = path.replace("_", " ");
        return answer;
    }

    public String getQuestion() {
        return getUtterance();
    }
    
    public void setRelations(ArrayList<String> relations) {
		this.relations = relations;
	}

    @Override
    public String toString() {
        return "Record{" +
                "utterance='" + utterance + '\'' +
                ", url=" + url +
                ", targetValue='" + targetValue + '\'' +
                '}';
    }
}
