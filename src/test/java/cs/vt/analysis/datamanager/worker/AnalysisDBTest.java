package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cs.vt.analysis.analyzer.AnalysisManager;
import cs.vt.analysis.analyzer.parser.Util;
import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;
import cs.vt.analysis.datamanager.main.Main;

public class AnalysisDBTest {

	private AnalysisDBManager manager;
	private AnalysisResultReader reader;
	

	@Before
	public void setUp() throws Exception {
		manager = new AnalysisDBManager();
		reader = new AnalysisResultReader();
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void insertAnalysisReport() throws Exception {
		InputStream in = Main.class.getClassLoader()
				.getResource("88190066-m-1").openStream();
		String inputString = IOUtils.toString(in);
		in.close();
		
		manager.clearAnalysisReport();
		Document report = reader.getFullReportAsDoc();
		manager.insertAnalysisReport(report);
		Document result = manager.findAnalysisReport(88190066);
		assertTrue(result.containsValue(88190066));
		assertEquals(1,manager.removeAnalysisReport(88190066));

	}

}
