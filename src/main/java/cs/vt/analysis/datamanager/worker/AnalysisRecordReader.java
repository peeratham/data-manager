package cs.vt.analysis.datamanager.worker;

import java.io.File;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AnalysisRecordReader {

	private File analysisResultDirectory;
	private JSONObject reports;
	private Document fullReportDoc;
		
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

		public Document getFullReportAsDoc() {
			return fullReportDoc;
			
		}

		public void process(String inputString) throws ParseException {
			fullReportDoc = Document.parse(inputString);
			reports = (JSONObject) new JSONParser().parse(inputString);
		}
		

		public Document getDoc(String key) throws Exception {
			JSONObject reportJSON = (JSONObject) reports.get(key);
			Document reportDoc;
			try{
				reportDoc = Document.parse(reportJSON.toJSONString());
			}catch(Exception e){
				throw new Exception(e);
			}
			return reportDoc;
		}

		public JSONObject getReportAsJson() {
			return reports;
		}
		
		public Document getReportAsDocument(){
			return fullReportDoc;
		}
		
		
		
		
		
	

}
