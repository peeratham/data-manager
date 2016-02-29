package cs.vt.analysis.datamanager.main;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;
import cs.vt.analysis.datamanager.worker.AnalysisDBManager;
import cs.vt.analysis.datamanager.worker.FileResourceManager;

public class Main {
	static int numOfProjects = 10;
	private static final String DATASET_DIR = "C:/Users/Peeratham/workspace/scratch-dataset";
	static AnalysisDBManager manager = null;
	static FileResourceManager resourceManager = null;
	
	public static void main(String[] args){
		Crawler crawler = new Crawler();
		
		try {
			manager = new AnalysisDBManager();
			resourceManager = new FileResourceManager();
			resourceManager.setDatasetDirectory(DATASET_DIR);
		} catch (Exception e) {
			e.printStackTrace();
		}
		crawler.setNumberOfProjectToCollect(numOfProjects);
		try {
			cleanDataSet();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		
	}


	private static void cleanDataSet() throws IOException {
		manager.clearMetadata();
		resourceManager.cleanDirectory();
		
	}


	
}
