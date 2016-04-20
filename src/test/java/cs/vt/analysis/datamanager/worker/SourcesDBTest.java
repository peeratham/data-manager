package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cs.vt.analysis.datamanager.crawler.Crawler;

public class SourcesDBTest {

	private AnalysisDBManager manager;
	private Crawler crawler;
	private JSONParser parser;

	@Before
	public void setUp() throws Exception {
		manager = new AnalysisDBManager();
		manager.setAnalysisDBManagerForTest(true);
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
