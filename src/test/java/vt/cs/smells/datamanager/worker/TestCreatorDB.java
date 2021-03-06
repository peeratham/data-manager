package vt.cs.smells.datamanager.worker;

import static org.junit.Assert.assertNotNull;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vt.cs.smells.datamanager.crawler.Creator;
import vt.cs.smells.datamanager.worker.AnalysisDBManager;

public class TestCreatorDB {

	private AnalysisDBManager manager;

	@Before
	public void setUp() throws Exception {
		manager = AnalysisDBManager.getTestAnalysisDBManager();
		manager.clearCreatorRecords();
	}

	@After
	public void tearDown() throws Exception {
		manager.clearCreatorRecords();
	}
	
	@Test
	public void testConnectToMongo(){
		assertNotNull(manager);
	}
	
	@Test
	public void testUserObject(){
		int projectID = 123456;
		Creator creator = new Creator("userName");
		creator.addProjectID(projectID);
		String masteryInput = "{ \"FlowControl\" : 2, \"abstraction\" : 1, \"DataRepresentation\" : 2, \"Synchronization\" : 2, \"Logic\" : 3, \"User Interactivity\" : 1, \"Parallelization\" : 1 }";
		Document masteryReport = Document.parse(masteryInput);
		creator.setMasteryReport(masteryReport);
	}

	@Test
	public void testInsertNonExistingUser() {
		
		Creator creator1 = new Creator("a");
		int project1 = 1234;
		creator1.addProjectID(project1);
		String masteryInput1 = "{ \"FlowControl\" : 2, \"abstraction\" : 1, \"DataRepresentation\" : 2, \"Synchronization\" : 2, \"Logic\" : 3, \"User Interactivity\" : 1, \"Parallelization\" : 1 }";
		Document masteryReport1 = Document.parse(masteryInput1);
		creator1.setMasteryReport(masteryReport1);
		manager.putCreatorRecord(creator1.toDocument());
		
		
		Creator creator2 = new Creator("b");
		int project2 = 5678;
		creator2.addProjectID(project2);
		String masteryInput2 = "{ \"FlowControl\" : 3, \"abstraction\" : 0, \"DataRepresentation\" : 2, \"Synchronization\" : 3, \"Logic\" : 3, \"User Interactivity\" : 1, \"Parallelization\" : 1 }";
		Document masteryReport2 = Document.parse(masteryInput2);
		creator2.setMasteryReport(masteryReport2);
		manager.putCreatorRecord(creator2.toDocument());
		manager.deleteCreator("userName");
		
	}
	
	@Test
	public void testInsertSameCreatorShouldUpdate(){
		Creator creator1 = new Creator("a");
		int project1 = 1234;
		creator1.addProjectID(project1);
		String masteryInput1 = "{ \"FlowControl\" : 2, \"abstraction\" : 1, \"DataRepresentation\" : 2, \"Synchronization\" : 2, \"Logic\" : 3, \"User Interactivity\" : 1, \"Parallelization\" : 1 }";
		Document masteryReport1 = Document.parse(masteryInput1);
		creator1.setMasteryReport(masteryReport1);
		manager.putCreatorRecord(creator1.toDocument());
		manager.putCreatorRecord(creator1.toDocument());
	}
	
	@Test
	public void testInsertConciseReport(){
		Creator creator1 = new Creator("a");
		int project1 = 1234;
		creator1.addProjectID(project1);
		String masteryInput1 = "{\"total\":14}";
		Document masteryReport1 = Document.parse(masteryInput1);
		creator1.setMasteryReport(masteryReport1);
		manager.putCreatorRecord(creator1.toDocument());
		manager.putCreatorRecord(creator1.toDocument());
	}

}
