package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.assertEquals;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cs.vt.analysis.analyzer.AnalysisManager;
import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;
import cs.vt.analysis.datamanager.main.AnalysisResultReader;

public class AnalysisResultReaderTest {

	private String dir;
	private AnalysisDBManager manager;

	@Before
	public void setUp() throws Exception {
		dir = AnalysisResultReaderTest.class.getClassLoader().getResource("test-output").getPath();
		manager = AnalysisDBManager.getTestAnalysisDBManager();
	}

	@After
	public void tearDown() throws Exception {
		manager.clearMetadata();
		manager.clearAnalysisReport();
		manager.clearCreatorRecords();
	}

	@Test
	public void testSaveAnalysisRecords() throws Exception{
		AnalysisResultReader reader = new AnalysisResultReader();
		reader.setAnalysisResultDir(dir);
		reader.processAnalysisResultFiles(manager);
		assertEquals(10L,manager.getReportSize());
	}
	
	@Test
	public void testMetadataShouldMatchWithCreator() throws Exception{
		Crawler crawler = new Crawler();
		int projectID = 100204638;
		ProjectMetadata metadata = new ProjectMetadata(projectID);
		crawler.retrieveProjectMetadata(metadata);
		String src = crawler.retrieveProjectSourceFromProjectID(projectID);
		System.out.println(metadata);
		
		Document doc = metadata.toDocument();
		manager.putMetadata(doc);
		
		AnalysisManager blockAnalyzer = new AnalysisManager();
		JSONObject result = blockAnalyzer.analyze(src);
		AnalysisResultReader reader = new AnalysisResultReader();
		
		String line = "100204638\t{\"Uncommunicative Naming\":{\"instances\":[],\"count\":0},\"Unnecessary Broadcast\":{\"instances\":[],\"count\":0},\"Duplicate Code\":{\"instances\":[],\"count\":0},\"Mastery Level\":{\"FlowControl\":2,\"abstraction\":1,\"DataRepresentation\":1,\"Synchronization\":3,\"Logic\":0,\"User Interactivity\":1,\"Parallelization\":1},\"BroadCastWorkaround\":{\"instances\":[],\"count\":0},\"Unreachable Code\":{\"instances\":[],\"count\":0},\"spriteCount\":2,\"_id\":100204638,\"scriptCount\":3,\"Too Long Script\":{\"instances\":[],\"count\":0},\"Too Broad Variable Scope\":{\"instances\":[],\"count\":0}}";
		AnalysisResultReader.processLine(manager, line);
		
		assertEquals(1L, manager.getMetadataSize());
		assertEquals(1L, manager.getReportSize());
		assertEquals(1L, manager.getCreatorsSize());
	}
	

}
