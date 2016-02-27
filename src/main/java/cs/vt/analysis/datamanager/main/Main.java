package cs.vt.analysis.datamanager.main;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;
import cs.vt.analysis.datamanager.worker.AnalysisDBManager;
import cs.vt.analysis.datamanager.worker.FileResourceManager;

public class Main {
	static int numOfProjects = 10;
	private static final String datasetDirectory = "C:/Users/Peeratham/workspace/scratch-dataset";

	
	public static void main(String[] args){
		Crawler crawler = new Crawler();
		AnalysisDBManager manager = null;
		FileResourceManager resourceManager = null;
		try {
			manager = new AnalysisDBManager();
			resourceManager = new FileResourceManager();
			resourceManager.setDatasetDirectory(datasetDirectory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		crawler.setNumberOfProjectToCollect(numOfProjects);
		
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
}
