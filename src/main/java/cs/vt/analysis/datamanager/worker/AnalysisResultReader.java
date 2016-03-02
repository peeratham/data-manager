package cs.vt.analysis.datamanager.worker;

import java.io.File;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AnalysisResultReader {

	private File analysisResultDirectory;
		
		public void setDatasetDirectory(String path) throws Exception {
			File f = new File(path);
			if(f.isDirectory()){
				analysisResultDirectory = new File(path);
			}else{
				throw new Exception("The givien path is not a directory");
			}
		}
		
		public File[] getFiles(){
			return analysisResultDirectory.listFiles();
		}

		public Document extractDocument(String inputLine) {
			Document doc = Document.parse(inputLine);
			return doc;
			
		}
		
		
		
	

}