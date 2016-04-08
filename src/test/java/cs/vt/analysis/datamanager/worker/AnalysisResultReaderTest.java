package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cs.vt.analysis.datamanager.main.AnalysisResultReader;
import cs.vt.analysis.datamanager.main.Main;

public class AnalysisResultReaderTest {

	private String dir;
	private AnalysisDBManager manager;

	@Before
	public void setUp() throws Exception {
		dir = Main.class.getClassLoader().getResource("test-output").getPath();
		manager = new AnalysisDBManager();
		manager.setAnalysisDBManagerForTest(true);
	}

	@After
	public void tearDown() throws Exception {
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
	

}
