package vt.cs.smells.datamanager.worker;

import static org.junit.Assert.*;

import java.util.Properties;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vt.cs.smells.datamanager.crawler.Crawler;
import vt.cs.smells.datamanager.crawler.ProjectMetadata;
import vt.cs.smells.datamanager.worker.AnalysisDBManager;

public class TestMetadataDB {

	private AnalysisDBManager manager;

	@Before
	public void setUp() throws Exception {
		Properties props = System.getProperties();
		props.setProperty("logDir", "./");
		manager = AnalysisDBManager.getTestAnalysisDBManager();
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
		manager.putMetadata(doc);
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
		manager.putMetadata(doc);
		manager.putMetadata(doc);
	}
}
