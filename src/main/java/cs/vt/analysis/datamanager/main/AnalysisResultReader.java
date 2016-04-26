package cs.vt.analysis.datamanager.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import cs.vt.analysis.datamanager.crawler.Creator;
import cs.vt.analysis.datamanager.worker.AnalysisDBManager;
import cs.vt.analysis.datamanager.worker.AnalysisRecordReader;

public class AnalysisResultReader {
	private static File resultDirectory;

	public static void main(String[] args) {
		final Options options = createOptions();
		try {
			final CommandLine line = getCommandLine(options, args);
			boolean isDir = setAnalysisResultDir(line.getOptionValue("dir"));
			if (!isDir) {
				System.err
						.println(resultDirectory + "is not a valid directory");
			}
			AnalysisDBManager manager;
			String host = line.getOptionValue("h");
			if (host==null){
				manager = new AnalysisDBManager();
			}else{
				manager = new AnalysisDBManager(host);
			}
			 
			processAnalysisResultFiles(manager);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static CommandLine getCommandLine(Options options, String[] args)
			throws ParseException {
		final CommandLineParser parser = new BasicParser();
		final CommandLine line;
		line = parser.parse(options, args);
		return line;
	}

	private static Options createOptions() {
		final Options options = new Options();
		options.addOption("dir", true, "analysis result direcotry");
		options.addOption("h", true, "host for mongod instance");
		return options;
	}

	public static boolean setAnalysisResultDir(String dir) {
		resultDirectory = new File(dir);
		return resultDirectory.isDirectory();
	}

	public static ArrayList<File> processAnalysisResultFiles(
			AnalysisDBManager dbManager) {
		ArrayList<File> list = new ArrayList<File>();
		for (File part : resultDirectory.listFiles()) {
			if (part.getName().contains("part")) {
				try {
					parseAndSaveResultToDatabase(dbManager, part);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	private static void parseAndSaveResultToDatabase(
			AnalysisDBManager dbManager, File part) throws IOException {
		ArrayList<JSONObject> reports = new ArrayList<JSONObject>();
		InputStream in = new FileInputStream(part);
		List<String> lines = IOUtils.readLines(in);
		in.close();
		
		for(int i = 0; i < lines.size(); i++){
			try {
				processLine(dbManager, lines.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
			double percent = (double)i/(double)lines.size();
			System.out.print("\r"+percent*100+"%");
		}
//		lines.forEach((line) -> {
//			processLine(dbManager, line);
//		});

	}

	public static void processLine(AnalysisDBManager dbManager, String line) throws Exception {
		String[] lineRecord = line.split("\t");
		int projectID = Integer.parseInt(lineRecord[0]);
		try {
			Document fullReportDoc = Document.parse(lineRecord[1]);
			dbManager.putAnalysisReport(projectID, fullReportDoc);
			String creatorName = dbManager.lookUpCreator(projectID);
			if(fullReportDoc.containsKey("Mastery Level") && creatorName!=null){
				Document masteryReport = (Document) fullReportDoc.get("Mastery Level");
				Creator creator = new Creator(creatorName);
				creator.addProjectID(projectID);
				creator.setMasteryReport(masteryReport);
				dbManager.putCreatorRecord(creator.toDocument());
			}
		} catch (Exception e) {
			throw new Exception("Error reading project: " + projectID);
		}
	}

}
