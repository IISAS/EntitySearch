package relation_linking;

import java.util.*;

import scala.collection.*;
import edu.knowitall.openie.*;
import edu.knowitall.tool.parse.ClearParser;
import edu.knowitall.tool.postag.ClearPostagger;
import edu.knowitall.tool.srl.ClearSrl;
import edu.knowitall.tool.tokenize.ClearTokenizer;

public class OpenIEEngine {

	private OpenIE openIE;
	
	public OpenIEEngine(){
		System.out.println("Starting openIE Engine...");
		openIE = new OpenIE(new ClearParser(new ClearPostagger(new ClearTokenizer())), new ClearSrl(), true, true);
	}
	
	public ArrayList<String> getRelations(String sentence){
		
		System.out.println("Getting openIE relations...");
		
		ArrayList<String> results = new ArrayList<String>();
		
		Seq<Instance> extractions = openIE.extract(sentence);
		List<Instance> list_extractions = JavaConversions.seqAsJavaList(extractions);
		
		for(Instance instance : list_extractions) {
        	results.add(instance.extr().rel().text());
        }
		
		return results;
	}
	
}
