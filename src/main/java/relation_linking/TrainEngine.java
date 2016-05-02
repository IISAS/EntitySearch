package relation_linking;

import java.io.File;
import java.io.IOException;

public class TrainEngine {

	File train;
	File test;

	public TrainEngine(String trainFilePath, String testFilePath, String resultFilePath) throws IOException, InterruptedException {
		System.out.println("Starting conversion...");
		Process p = Runtime.getRuntime().exec("python src/main/resources/data/ppp_vw_2_lsvm.py --input " +trainFilePath + "--out " +resultFilePath);
		p.waitFor();
		System.out.println("Conversion finished...");
	}
}
