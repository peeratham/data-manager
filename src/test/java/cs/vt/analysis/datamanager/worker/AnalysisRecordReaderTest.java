package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cs.vt.analysis.datamanager.main.Main;

public class AnalysisRecordReaderTest {

	private static final String ANALYSIS_OUTPUT_FILENAME = "69004564-m-1";
	private AnalysisRecordReader reader;

	@Before
	public void setUp() throws Exception {
		reader = new AnalysisRecordReader();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void readAnalysisFile() throws IOException, ParseException{
		InputStream in = Main.class.getClassLoader()
				.getResource(ANALYSIS_OUTPUT_FILENAME).openStream();
		String inputString = IOUtils.toString(in);
		in.close();
		reader.process(inputString);
		Document doc = reader.getFullReportAsDoc();
		System.out.println(doc);
	}
	
	@Test
	public void testExtractMasteryReport() throws IOException, ParseException{
		InputStream in = Main.class.getClassLoader()
				.getResource(ANALYSIS_OUTPUT_FILENAME).openStream();
		String inputString = IOUtils.toString(in);
		in.close();
		reader.process(inputString);
		Document masteryReport = reader.getDoc("Mastery Level");
		System.out.println(masteryReport.toJson());
	}
	
	
	
	
}
