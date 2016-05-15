package vt.cs.smells.datamanager.worker;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import vt.cs.smells.datamanager.worker.AnalysisDBManager;

public class TestSourceExport {

	private AnalysisDBManager manager;

	@Before
	public void setUp() throws Exception {
		manager = AnalysisDBManager.getTestAnalysisDBManager();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testExport() throws InterruptedException {
		String host = "localhost";
		manager.setMongoExportPath("/opt/mongodb/bin/mongoexport");
		manager.export(host, "analysis", "sources", 5, 5, "/Users/karn/Workspace/data-manager/target");
	}
	
	@Ignore
	@Test
	public void testBlockExport() throws InterruptedException{
		String host = "localhost";
		manager.setMongoExportPath("/opt/mongodb/bin/mongoexport");
		manager.exportWithLimitPerFile(host, "analysis", "sources", "/Users/karn/Workspace/data-manager/target", 3);
	}

}
