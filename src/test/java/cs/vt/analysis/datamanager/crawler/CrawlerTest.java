package cs.vt.analysis.datamanager.crawler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

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
		int numOfProjects = 100;
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
		crawler.retrieveProjectMetadata(metadata);
		assertEquals("Amplex", metadata.getTitle());
		assertEquals("Unrealisation", metadata.getCreator());
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
	public void testProjectExists(){
		
	}
	
	


}
