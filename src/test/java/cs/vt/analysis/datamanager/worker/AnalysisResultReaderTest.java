package cs.vt.analysis.datamanager.worker;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cs.vt.analysis.datamanager.main.Main;

public class AnalysisResultReaderTest {

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
	public void readAnalysisFile() throws IOException{
		InputStream in = Main.class.getClassLoader()
				.getResource("88190066-m-1").openStream();
		String inputString = IOUtils.toString(in);
		in.close();
		Document doc = reader.extractDocument(inputString);
	}
	
	
	
	
}
