package vt.cs.smells.datamanager.main;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import vt.cs.smells.datamanager.crawler.Crawler;
import vt.cs.smells.datamanager.crawler.ProjectMetadata;
import vt.cs.smells.datamanager.worker.AnalysisDBManager;
import vt.cs.smells.datamanager.worker.TestAnalysisResultReader;

public class TestSourceDownloader {
	
	private String inputPath;
	@Before
	public void setUp() throws Exception {
		Properties props = System.getProperties();
		props.setProperty("logDir", "./");
		inputPath = TestAnalysisResultReader.class.getClassLoader()
				.getResource("remix_original_id.json").getPath();
	}
	@Test
	public void test() throws IOException, ParseException {
		SourceDownloader loader = new SourceDownloader();
		String jsonArrayProjectIDListFile = "/home/peeratham/tpeera4/smell-analysis/scripts/remix_id.json";
		File f = new File(inputPath);
		loader.setInputPath(f);
		loader.setDatabase("localhost","analysis");
		loader.retrieveSource();
	}

}
