package vt.cs.smells.datamanager.main;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import vt.cs.smells.datamanager.crawler.Crawler;
import vt.cs.smells.datamanager.crawler.ProjectMetadata;
import vt.cs.smells.datamanager.worker.AnalysisDBManager;

public class RangeCrawler {
	static JSONParser parser = new JSONParser();
	private static AnalysisDBManager DBManager;
	private static int limit;
	private static String databaseName;
	private static String host;
	private static int start;
	private static int end;
	private static int lastID;

	private static void crawlBetweenIDRange(int start, int end, int limit) {
		int counter = 0;
		for (int id = start; id < end; id++) {
			if (counter >= limit) {
				break;
			}
			if (Crawler.checkIfExists(id)) {
				ProjectMetadata currentProjMetadata = new ProjectMetadata(id);
				try {
					currentProjMetadata = Crawler
							.retrieveProjectMetadata(currentProjMetadata);
					String src = Crawler
							.retrieveProjectSourceFromProjectID(currentProjMetadata
									.getProjectID());
					String singleLineJSONSrc = ((JSONObject) parser.parse(src))
							.toJSONString();
					DBManager.putSource(currentProjMetadata.getProjectID(),
							singleLineJSONSrc);
					DBManager.putMetadata(currentProjMetadata.toDocument());
					counter++;
					lastID = id;
					System.out.print("\r" + counter);
				} catch (Exception e) {
				}

			}
			
		}
		System.out.println("Last ID: "+lastID);
		System.out.println("Total Collected:" + counter);
	}

	public static void main(String[] args) {
		Properties props = System.getProperties();
		if(props.getProperty("logDir")!=null){
			props.setProperty("logDir", "./");
		}
		final Options options = createOptions();

		try {
			final CommandLine line = getCommandLine(options, args);
			limit = Integer.parseInt(line.getOptionValue("n"));
			databaseName = line.getOptionValue("db");
			host = line.getOptionValue("h");
			start = Integer.parseInt(line.getOptionValue("s"));
			end = Integer.parseInt(line.getOptionValue("e"));
		} catch (Exception e) {

		}

		DBManager = new AnalysisDBManager(host, databaseName);
		
		crawlBetweenIDRange(start, end, limit);

	}

	private static Options createOptions() {
		final Options options = new Options();
		options.addOption("n", true, "limit of projects to download");
		options.addOption("db", true, "database name");
		options.addOption("h", true, "host for mongod instance");
		options.addOption("s", true, "start id");
		options.addOption("e", true, "end id");

		return options;
	}

	private static CommandLine getCommandLine(Options options, String[] args)
			throws Exception {
		final CommandLineParser parser = new BasicParser();
		final CommandLine line;
		line = parser.parse(options, args);
		return line;
	}
}
