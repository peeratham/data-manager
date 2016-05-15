package vt.cs.smells.datamanager.main;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import vt.cs.smells.datamanager.worker.AnalysisDBManager;

public class ExportData {
	private static AnalysisDBManager manager;
	public static void main(String[] args) {
		
		final Options options = createOptions();
		try {
			final CommandLine line = getCommandLine(options, args);
			String mongoexport = line.getOptionValue("e");
			String host = line.getOptionValue("h");
			String databaseName = line.getOptionValue("d");
			manager = new AnalysisDBManager(host, databaseName);
			manager.setMongoExportPath(mongoexport);
			String collectionName = line.getOptionValue("c");
			String outputDir = line.getOptionValue("o");
			int limit = Integer.parseInt(line.getOptionValue("n"));
			manager.exportWithLimitPerFile(host, databaseName, collectionName, outputDir, limit);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private static CommandLine getCommandLine(Options options, String[] args)
			throws Exception {
		final CommandLineParser parser = new BasicParser();
		final CommandLine line;
		line = parser.parse(options, args);
		return line;
	}

	private static Options createOptions() {
		final Options options = new Options();
		options.addOption("e", true, "path to mongoexport executable");
		options.addOption("h", true, "host for mongod instance");
		options.addOption("d", true, "database name");
		options.addOption("c", true, "collection");
		options.addOption("o", true, "output directory");
		options.addOption("n", true, "records per file");
		return options;
	}

}
