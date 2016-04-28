package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

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
		Properties props = System.getProperties();
		props.setProperty("logDir", "./");
		dir = AnalysisResultReaderTest.class.getClassLoader().getResource("test-output").getPath();
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
	public void testMetadataShouldMatchWithCreator() throws Exception{
		Crawler crawler = new Crawler();
		int projectID = 100204638;
		ProjectMetadata metadata = new ProjectMetadata(projectID);
		crawler.retrieveProjectMetadata(metadata);
		String src = crawler.retrieveProjectSourceFromProjectID(projectID);
		
		Document doc = metadata.toDocument();
		manager.putMetadata(doc);
		assertNotNull(manager.findMetadata(projectID));
		
		AnalysisManager blockAnalyzer = new AnalysisManager();
		JSONObject result = blockAnalyzer.analyze(src);
		
		String line = projectID+"\t"+result.toJSONString();
		AnalysisResultReader.processLine(manager, line);
		assertNotNull(manager.findAnalysisReport(projectID));
	}
	
	@Test
	public void testCreatorUpdateOnlyIfProjectOriginal() throws Exception{
		Document doc = new Document();
		doc.append("_id", 1234);
		doc.append("creator", "creator1");
		doc.append("original", 2345);
		manager.putMetadata(doc);
		String reportRecord1 = "1234\t{\"smells\":{}, \"metrics\":{\"Mastery Level\":{}}, \"_id\":1234}";
		AnalysisResultReader.processLine(manager, reportRecord1);
		assertNull(manager.findCreatorRecord("creator1"));
		
		Document doc2 = new Document();
		doc2.append("_id", 5678);
		doc2.append("creator", "creator1");
		doc2.append("original", 5678);
		manager.putMetadata(doc2);
		String reportRecord2 = "5678\t{\"smells\":{}, \"metrics\":{\"Mastery Level\":{}}, \"_id\":5678}";
		AnalysisResultReader.processLine(manager, reportRecord2);
		assertNotNull(manager.findCreatorRecord("creator1"));
	}
	
	@Test
	public void testReader() throws Exception{
		Crawler crawler = new Crawler();
		int[] projectIDs = {105292497,104088578,100289065};
		for(int id: projectIDs){
			ProjectMetadata metadata = crawler.retrieveProjectMetadata(new ProjectMetadata(id));
			manager.putMetadata(metadata.toDocument());
		}
		String lines = analysisReportGenerator(projectIDs);
		for(String line : lines.split("\n")){
			AnalysisResultReader.processLine(manager, line);
		}
		
	}
	
	public String analysisReportGenerator(int[] projectIDs) throws Exception{
		Crawler crawler = new Crawler();
		AnalysisManager blockAnalyzer = new AnalysisManager();
		StringBuilder sb = new StringBuilder();
		for(int id: projectIDs){
			String src = crawler.retrieveProjectSourceFromProjectID(id);
			JSONObject report = blockAnalyzer.analyze(src);
			String result = report.toJSONString();
			sb.append(id);
			sb.append("\t");
			sb.append(result);
			sb.append("\n");
		}
		System.out.println(sb.toString());
		return sb.toString();
	}

	

}
