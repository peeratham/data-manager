package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AnalysisDBTest {

	private AnalysisDBManager manager;
	private AnalysisRecordReader reader;
	

	@Before
	public void setUp() throws Exception {
		manager = AnalysisDBManager.getTestAnalysisDBManager();
		reader = new AnalysisRecordReader();
	}

	@After
	public void tearDown() throws Exception {
		
	}

	//TODO
	@Ignore
	@Test
	public void insertAnalysisReport() throws Exception {
		InputStream in = AnalysisDBTest.class.getClassLoader()
				.getResource("88190066-m-1").openStream();
		String inputString = IOUtils.toString(in);
		in.close();
		
		manager.clearAnalysisReport();
		Document report = reader.getFullReportAsDoc();
		manager.putAnalysisReport(88190066, report);
		Document result = manager.findAnalysisReport(88190066);
		assertTrue(result.containsValue(88190066));
		assertEquals(1,manager.removeAnalysisReport(88190066));

	}

}
