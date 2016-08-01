package vt.cs.smells.datamanager.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import vt.cs.smells.datamanager.crawler.Crawler;
import vt.cs.smells.datamanager.crawler.ProjectMetadata;

public class SourceDownloader {
	private static String databaseName;
	private static String host;
	private static String sourceFilePath;
	JSONParser jsonParser = new JSONParser();
	JSONArray idList;
	List<ProjectMetadata> metadataListing = new ArrayList<ProjectMetadata>();
	Properties props = System.getProperties();
	Crawler crawler = new Crawler();
	private static String jsonArrayProjectIDListFile;

	public SourceDownloader() {
		props.setProperty("logDir", "./");
	}

	public void setInputPath(File f) throws IOException, ParseException {
		InputStream in = new FileInputStream(f);
		// InputStream in =
		// Main.class.getClassLoader().getResource("commands_src.json").openStream();
		if (in != null) {
			idList = (JSONArray) jsonParser.parse((new BufferedReader(
					new InputStreamReader(in))));
		} else {
			System.out.println("Null Input");
		}

		prepareProjectMetadataListing();

	}

	private void prepareProjectMetadataListing() {
		for (int i = 0; i < idList.size(); i++) {
			JSONObject idObject = (JSONObject) idList.get(i);
			Document document = Document.parse(idObject.toJSONString());
			Integer projectID = document.getInteger("original");
			metadataListing.add(new ProjectMetadata(projectID));
		}
		System.out.println(metadataListing.size()+" projects to download");
	}

	public void retrieveSource() {
		DatasetCrawl2.retrieveProjectMetadataListing(crawler, metadataListing,
				jsonParser);
	}

	public void setDatabase(String host, String dbName) {
		DatasetCrawl2.setDatabase(host, dbName);

	}
	
	public static void main(String[] args){
		final Options options = createOptions();
		try {
			final CommandLine line = getCommandLine(options, args);
			try{
				databaseName = line.getOptionValue("db");
				host = line.getOptionValue("h");
				jsonArrayProjectIDListFile = line.getOptionValue("s");
				
				SourceDownloader loader = new SourceDownloader();
				File f = new File(jsonArrayProjectIDListFile);
				loader.setInputPath(f);
				loader.setDatabase(host,databaseName);
				loader.retrieveSource();
			}catch(Exception e){
				e.printStackTrace();
			}
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
		options.addOption("db", true, "database name");
		options.addOption("h", true, "host for mongod instance");
		options.addOption("s", true, "input file as array json of project ids");
		options.addOption("l", true, "log file directory");
		return options;
	}

}
