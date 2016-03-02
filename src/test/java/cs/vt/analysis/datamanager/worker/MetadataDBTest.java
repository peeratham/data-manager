package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.*;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;

public class MetadataDBTest {

	private AnalysisDBManager manager;

	@Before
	public void setUp() throws Exception {
		manager = new AnalysisDBManager();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void connectToMongoDB() {
		assertNotNull(manager);
		
	}
	
	@Test
	public void insertProjectMetadata() throws Exception {
		int projectID = 43026762;
		ProjectMetadata metadata = new ProjectMetadata(projectID);
		Crawler crawler = new Crawler();
		crawler.retrieveProjectMetadata(metadata);
		
		Document doc = metadata.toDocument();
		manager.insertMetadata(doc);
		Document result = manager.findMetadata(projectID);
		assertTrue(result.containsValue(projectID));
		assertEquals(1,manager.removeMetadata(projectID));
	}
	
	@Test
	public void clearAllProjectMetadataRecords(){
		manager.clearMetadata();
		assertEquals(0,manager.getMetadataSize());
	}
	
	@Test
	public void returnNullIfNotFound(){
		assertNull(manager.findMetadata(0));
	}

	@Test
	public void updateIfMetadataExists() throws Exception{
		int projectID = 98908690;
		ProjectMetadata metadata = new ProjectMetadata(projectID);
		Crawler crawler = new Crawler();
		crawler.retrieveProjectMetadata(metadata);
		
		Document doc = metadata.toDocument();
	
		manager.insertMetadata(doc);
		manager.insertMetadata(doc);
		
	}
}
