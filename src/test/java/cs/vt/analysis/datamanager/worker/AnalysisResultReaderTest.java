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

public class AnalysisResultReaderTest {

	private static final String ANALYSIS_OUTPUT_FILENAME = "69004564-m-1";
	private AnalysisResultReader reader;

	@Before
	public void setUp() throws Exception {
		reader = new AnalysisResultReader();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void ReaderListFilesInDirectory() throws Exception {
		
		reader.setDatasetDirectory("C:\\Users\\Peeratham\\workspace\\analysis-output");
		assertNotNull(reader.getFiles());
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
