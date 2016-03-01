package cs.vt.analysis.datamanager.main;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.json.simple.JSONObject;

import cs.vt.analysis.analyzer.BlockAnalyzer;
import cs.vt.analysis.analyzer.analysis.AnalysisException;
import cs.vt.analysis.analyzer.parser.ParsingException;
import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;
import cs.vt.analysis.datamanager.worker.AnalysisDBManager;
import cs.vt.analysis.datamanager.worker.AnalysisResultReader;
import cs.vt.analysis.datamanager.worker.FileResourceManager;

public class Main {
	static int numOfProjects = 10;
	private static final String DATASET_DIR = "C:/Users/Peeratham/workspace/scratch-dataset";
	private static final String ANALYSIS_OUTPUT_DIR = "C:\\Users\\Peeratham\\workspace\\analysis-output\\test";
	static AnalysisDBManager manager = null;
	static FileResourceManager resourceManager = null;
	
	public static void main(String[] args){
		Crawler crawler = new Crawler();
		crawler.setNumberOfProjectToCollect(numOfProjects);
		try {
			manager = new AnalysisDBManager();
			resourceManager = new FileResourceManager();
			resourceManager.setDatasetDirectory(DATASET_DIR);
			resourceManager.setAnalysisResultDir(ANALYSIS_OUTPUT_DIR);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			cleanDataSet();
			cleanAnalysisResult();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<ProjectMetadata> projectMetadataListing = crawler.getProjects();
		for (int i = 0; i < numOfProjects; i++) {
			ProjectMetadata current = projectMetadataListing.get(i);
			try {
				crawler.retrieveProjectMetadata(current);
				String src = crawler.retrieveProjectSourceFromProjectID(current.getProjectID());
				resourceManager.write(current.getProjectID()+".json", src);
				
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			manager.insertMetadata(current.toDocument());
		}
		
		//analyze data in DATASET_DIR and write result to ANALYSIS_OUTPUT_DIR 
		BlockAnalyzer blockAnalyzer = new BlockAnalyzer();
	
		
		for (File f: new File(DATASET_DIR).listFiles()){
        	try {
				JSONObject result = blockAnalyzer.analyze(FileUtils.readFileToString(f));
				int projectID = blockAnalyzer.getProjectID();
				
				File path = new File(ANALYSIS_OUTPUT_DIR, projectID+"-m-1");
				FileUtils.writeStringToFile(path, result.toJSONString());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (AnalysisException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
		//read back and write to database
		AnalysisResultReader reader = new AnalysisResultReader();
		for (File f: new File(ANALYSIS_OUTPUT_DIR).listFiles()){
			try {
				Document report = reader.extractDocument(FileUtils.readFileToString(f));
				manager.insertAnalysisReport(report);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
		
	}

	private static void cleanAnalysisResult() throws IOException {
		resourceManager.cleanAnalysisResultDir();
		
	}

	private static void cleanDataSet() throws IOException {
		manager.clearMetadata();
		resourceManager.cleanDatasetDirectory();
		
	}


	
}
