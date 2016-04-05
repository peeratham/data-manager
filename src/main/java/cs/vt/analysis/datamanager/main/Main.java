package cs.vt.analysis.datamanager.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.json.simple.JSONObject;


import org.json.simple.parser.JSONParser;

import cs.vt.analysis.analyzer.AnalysisManager;
import cs.vt.analysis.analyzer.analysis.AnalysisException;
import cs.vt.analysis.analyzer.parser.ParsingException;
import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.Creator;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;
import cs.vt.analysis.datamanager.worker.AnalysisDBManager;
import cs.vt.analysis.datamanager.worker.AnalysisResultReader;
import cs.vt.analysis.datamanager.worker.FileResourceManager;

public class Main {
	static int numOfProjects = 500;
	public static final boolean TEST = true;
	public static final boolean ENABLE_LOCAL_ANALYSIS = true;
	public static final boolean RERUN_ANALYSIS_ONLY = false;

	private static String BASE_DATA_DIR = "";
	private static String DATASET_DIR = "";
	private static String ANALYSIS_OUTPUT_DIR = "";
	private static String TEST_DATASET_DIR = "";
	private static String TEST_ANALYSIS_OUTPUT_DIR = "";

	static AnalysisDBManager manager = null;
	static FileResourceManager resourceManager = null;
	static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) throws FileNotFoundException {
		initialize();

		Crawler crawler = new Crawler();
		crawler.setNumberOfProjectToCollect(numOfProjects);

		try {
			manager = new AnalysisDBManager();
			resourceManager = new FileResourceManager();
			if (TEST) {
				resourceManager.setDatasetDirectory(TEST_DATASET_DIR);
				resourceManager.setAnalysisResultDir(TEST_ANALYSIS_OUTPUT_DIR);
				cleanAnalysisResult();
				if(!RERUN_ANALYSIS_ONLY){
					cleanDataSet();
				}
			} else {
				resourceManager.setDatasetDirectory(DATASET_DIR);
				resourceManager.setAnalysisResultDir(ANALYSIS_OUTPUT_DIR);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		if(!RERUN_ANALYSIS_ONLY){
			List<ProjectMetadata> projectMetadataListing = crawler.getProjectsFromQuery();
			JSONParser parser = new JSONParser();
			for (int i = 0; i < projectMetadataListing.size(); i++) {
				ProjectMetadata current = projectMetadataListing.get(i);
				try {
					crawler.retrieveProjectMetadata(current);
					String src = crawler.retrieveProjectSourceFromProjectID(current.getProjectID());
					String singleLineJSONSrc = ((JSONObject) parser.parse(src)).toJSONString();
					resourceManager.write(current.getProjectID() + ".json", singleLineJSONSrc);
					manager.insertMetadata(current.toDocument());
					logger.info(i + "/" + numOfProjects + " saved metadata for project _id:" + current.getProjectID());

				} catch (ParseException | IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		

		// analyze data in DATASET_DIR and write result to ANALYSIS_OUTPUT_DIR
		if (ENABLE_LOCAL_ANALYSIS) {
			AnalysisManager blockAnalyzer = new AnalysisManager();

			for (File f : resourceManager.getBaseDatasetDirectory().listFiles()) {
				int projectID = 0;
				try {
					JSONObject result = blockAnalyzer.analyze(FileUtils.readFileToString(f));
					projectID = blockAnalyzer.getProjectID();

					File path = new File(resourceManager.getAnalysisResultDir().getAbsolutePath(), projectID + "-m-1");
					FileUtils.writeStringToFile(path, result.toJSONString());
					logger.info("analyzed project _id:" + projectID);
				} catch (IOException e) {
					System.err.println("error reading/writing: "+projectID);
					e.printStackTrace();
				} catch (ParsingException e) {
					System.err.println("error JSON parsing: "+projectID);
					e.printStackTrace();
				} catch (AnalysisException e) {
					System.err.println("error analyzing: "+projectID);
					e.printStackTrace();
				}
			}

			// read back and write to database
			AnalysisResultReader reader = new AnalysisResultReader();
			for (File f : resourceManager.getAnalysisResultDir().listFiles()) {
				try {
					reader.process(FileUtils.readFileToString(f));
					Document report = reader.getFullReportAsDoc();
					manager.insertAnalysisReport(report);
					int projectID = (int) report.get("_id");
				
					try{
						Document masteryReport = reader.getDoc("Mastery Level");
						Creator creator = new Creator(manager.lookUpCreator(projectID));
							creator.addProjectID(projectID);
							creator.setMasteryReport(masteryReport);
						manager.updateCreatorRecord(creator.toDocument());
					}catch(Exception e){
						logger.error("Error@MasteryReport: "+projectID);
						e.printStackTrace();
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (org.json.simple.parser.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	private static void initialize() throws FileNotFoundException {
		// data-manager config
		Properties prop = new Properties();
		String propFileName = "config.properties";
		InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			try {
				prop.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}

		BASE_DATA_DIR = prop.getProperty("BASE_DATA_DIR");
		DATASET_DIR = BASE_DATA_DIR + "dataset";
		ANALYSIS_OUTPUT_DIR = BASE_DATA_DIR + "output";
		TEST_DATASET_DIR = BASE_DATA_DIR + "test-dataset";
		TEST_ANALYSIS_OUTPUT_DIR = BASE_DATA_DIR + "test-output";

		// config log4j
		PropertyConfigurator.configure(Main.class.getClassLoader().getResource("log4j.properties"));
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
