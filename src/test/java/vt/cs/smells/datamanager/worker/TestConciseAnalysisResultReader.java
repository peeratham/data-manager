package vt.cs.smells.datamanager.worker;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vt.cs.smells.analyzer.AnalysisManager;
import vt.cs.smells.datamanager.crawler.Crawler;
import vt.cs.smells.datamanager.crawler.ProjectMetadata;
import vt.cs.smells.datamanager.main.AnalysisResultReader;
import vt.cs.smells.datamanager.worker.AnalysisDBManager;

public class TestConciseAnalysisResultReader {

	private String dir;
	private AnalysisDBManager manager;

	@Before
	public void setUp() throws Exception {
		Properties props = System.getProperties();
		props.setProperty("logDir", "./");
		dir = TestConciseAnalysisResultReader.class.getClassLoader()
				.getResource("test-read-concise-output").getPath();
		manager = AnalysisDBManager.getTestAnalysisDBManager();
	}

	@After
	public void tearDown() throws Exception {
		manager.clearMetadata();
		manager.clearAnalysisReport();
		manager.clearCreatorRecords();
		manager.clearMetrics();
	}

	@Test
	public void testReadAnalysisResult(){
		AnalysisResultReader.setAnalysisResultDir(dir);
		AnalysisResultReader.processAnalysisResultFiles(manager);

		
	}

}
