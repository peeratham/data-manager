package cs.vt.analysis.datamanager.main;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;

import cs.vt.analysis.analyzer.BlockAnalyzer;
import cs.vt.analysis.analyzer.analysis.AnalysisException;
import cs.vt.analysis.analyzer.parser.ParsingException;
import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;
import cs.vt.analysis.datamanager.crawler.RetryOnException;
import cs.vt.analysis.datamanager.worker.AnalysisDBManager;
import cs.vt.analysis.datamanager.worker.AnalysisResultReader;
import cs.vt.analysis.datamanager.worker.FileResourceManager;

public class Main {
	static int numOfProjects = 10;
	private static final String DATASET_DIR = "C:/Users/Peeratham/workspace/dataset";
	private static final String ANALYSIS_OUTPUT_DIR = "C:/Users/Peeratham/workspace/analysis-output/output";
	private static final String TEST_DATASET_DIR = "C:/Users/Peeratham/workspace/test-dataset";
	private static final String TEST_ANALYSIS_OUTPUT_DIR = "C:/Users/Peeratham/workspace/analysis-output/test-output";
	static AnalysisDBManager manager = null;
	static FileResourceManager resourceManager = null;
	static Logger logger = Logger.getLogger(Main.class);
	public static final boolean TEST = true;
	public static final boolean ENABLE_LOCAL_ANALYSIS = true;

	public static void main(String[] args) {
		// config log4j
		PropertyConfigurator.configure(Main.class.getClassLoader().getResource(
				"log4j.properties"));

		Crawler crawler = new Crawler();
		crawler.setNumberOfProjectToCollect(numOfProjects);

		try {
			manager = new AnalysisDBManager();
			resourceManager = new FileResourceManager();
			if (TEST) {
				resourceManager.setDatasetDirectory(TEST_DATASET_DIR);
				resourceManager.setAnalysisResultDir(TEST_ANALYSIS_OUTPUT_DIR);
				cleanDataSet();
				cleanAnalysisResult();
			} else {
				resourceManager.setDatasetDirectory(DATASET_DIR);
				resourceManager.setAnalysisResultDir(ANALYSIS_OUTPUT_DIR);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		List<ProjectMetadata> projectMetadataListing = crawler.getProjectsFromQuery();
		
		for (int i = 0; i < projectMetadataListing.size(); i++) {
			ProjectMetadata current = projectMetadataListing.get(i);
			try {
				crawler.retrieveProjectMetadata(current);
				String src = crawler.retrieveProjectSourceFromProjectID(current
						.getProjectID());
				resourceManager.write(current.getProjectID() + ".json", src);
				manager.insertMetadata(current.toDocument());
				logger.info(i + "/" + numOfProjects
						+ " processed: project _id:" + current.getProjectID());

			} catch (ParseException | IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// analyze data in DATASET_DIR and write result to ANALYSIS_OUTPUT_DIR
		if (ENABLE_LOCAL_ANALYSIS) {
			BlockAnalyzer blockAnalyzer = new BlockAnalyzer();
			
			for (File f : resourceManager.getDatasetDirectory().listFiles()) {
				try {
					JSONObject result = blockAnalyzer.analyze(FileUtils
							.readFileToString(f));
					int projectID = blockAnalyzer.getProjectID();

					File path = new File(resourceManager.getAnalysisResultDir().getAbsolutePath(), projectID
							+ "-m-1");
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

			// read back and write to database
			AnalysisResultReader reader = new AnalysisResultReader();
			for (File f : resourceManager.getAnalysisResultDir().listFiles()) {
				try {
					Document report = reader.extractDocument(FileUtils
							.readFileToString(f));
					manager.insertAnalysisReport(report);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

	private static void cleanAnalysisResult() throws IOException {
		manager.clearAnalysisReport();
		resourceManager.cleanAnalysisResultDir();
	}

	private static void cleanDataSet() throws IOException {
		manager.clearMetadata();
		resourceManager.cleanDatasetDirectory();

	}

}
