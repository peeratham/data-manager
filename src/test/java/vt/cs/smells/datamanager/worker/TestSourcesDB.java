package vt.cs.smells.datamanager.worker;

import static org.junit.Assert.*;

import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vt.cs.smells.datamanager.crawler.Crawler;
import vt.cs.smells.datamanager.worker.AnalysisDBManager;

public class TestSourcesDB {

	private AnalysisDBManager manager;
	private Crawler crawler;
	private JSONParser parser;

	@Before
	public void setUp() throws Exception {
		Properties props = System.getProperties();
		props.setProperty("logDir", "./");
		manager = AnalysisDBManager.getTestAnalysisDBManager();
		crawler = new Crawler();
		parser = new JSONParser();
	}

	@After
	public void tearDown() throws Exception {
		manager.clearSources();
	}

	@Test
	public void test() throws Exception {
		int projectID = 43026762;
		String src = crawler.retrieveProjectSourceFromProjectID(projectID);
		String singleLineJSONSrc = ((JSONObject) parser.parse(src)).toJSONString();
		manager.putSource(projectID, singleLineJSONSrc);
		assertEquals(1, manager.getSourcesSize());
		manager.putSource(projectID, singleLineJSONSrc);

		int projectID2 = 98908690;
		String src2 = crawler.retrieveProjectSourceFromProjectID(projectID2);
		String singleLineJSONSrc2 = ((JSONObject) parser.parse(src2)).toJSONString();
		manager.putSource(projectID2, singleLineJSONSrc2);
		assertEquals(2, manager.getSourcesSize());
	}

}
