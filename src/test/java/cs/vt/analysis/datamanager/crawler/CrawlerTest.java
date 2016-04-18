package cs.vt.analysis.datamanager.crawler;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CrawlerTest {
	Crawler crawler;
	@Before
	public void setUp() throws Exception {
		crawler = new Crawler();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCralwerNotNull() throws Exception {
		assertNotNull(crawler);
	}
	
	
	@Test
	public void testCrawlingToCollectInitialMetaInfoProjectIDAndTitle() throws Exception {
		int numOfProjects = 5;
		crawler.setNumberOfProjectToCollect(numOfProjects);
		assertEquals(crawler.getNumberOfProjectToCollect(), numOfProjects);
		List<ProjectMetadata> projectMetadataListing = crawler.getProjectsFromQuery();
		assertEquals(projectMetadataListing.size(), numOfProjects);
		for (int i = 0; i < numOfProjects; i++) {	
			assertNotEquals(projectMetadataListing.get(i).getTitle(),"");
		}
		
	}
	
	
	@Test
	public void testRetrieveProjectMetadataFromHTMLPage() throws Exception {
		int projectID = 43026762;
		ProjectMetadata metadata = new ProjectMetadata(projectID);
		metadata = crawler.retrieveProjectMetadata(metadata);
		assertEquals("Amplex", metadata.getTitle());
		assertEquals("Unrealisation", metadata.getCreator());
		assertNotNull(metadata);
		assertNotNull(metadata.getFavoriteCount());
		assertNotNull(metadata.getLoveCount());
		assertNotNull(metadata.getViews());
		assertNotNull(metadata.getRemixes());
		assertNotNull(metadata.getModifiedDate());
		assertNotNull(metadata.getDateShared());
	}
	
	@Test
	public void testRetrieveProjectSourceFile() throws Exception {
		int projectID = 43026762;
		String src = crawler.retrieveProjectSourceFromProjectID(projectID);
		assertNotNull(src);
	}
	
	@Test
	public void testGetOriginalProject() throws Exception{
		int remixProjectID = 100189358;
		int originalProjectID = 99670195;
		ProjectMetadata metadata = new ProjectMetadata(remixProjectID);
		crawler.retrieveProjectMetadata(metadata);
		assertEquals(originalProjectID, metadata.getOriginal());
		
		ProjectMetadata metadata1 = new ProjectMetadata(originalProjectID);
		crawler.retrieveProjectMetadata(metadata1);
		assertEquals(originalProjectID, metadata1.getOriginal());
		
	}
	
	@Test
	public void testProjectListingCrawl(){
		Crawler crawler = new Crawler();
		crawler.setNumberOfProjectToCollect(5);
		List<ProjectMetadata> projectMetadataListing = crawler.getProjectsFromQuery();
	}

}
