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
			AnalysisDBManager manager = new AnalysisDBManager();
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

		lines.forEach((line) -> {
			String[] lineRecord = line.split("\t");
			int projectID = Integer.parseInt(lineRecord[0]);
			AnalysisRecordReader reader = new AnalysisRecordReader();
			try {
				reader.process(lineRecord[1]);
				dbManager.putAnalysisReport(projectID,
						reader.getReportAsDocument());
				Document masteryReport = reader.getDoc("Mastery Level");
				if (masteryReport != null) {
					String creatorName = dbManager.lookUpCreator(projectID);
					if (creatorName == null) {
						creatorName = "creatorOf" + projectID;
					}
					Creator creator = new Creator(creatorName);
					creator.addProjectID(projectID);

					creator.setMasteryReport(masteryReport);
					dbManager.putCreatorRecord(creator.toDocument());
				}

			} catch (Exception e) {
				System.err.println("Error reading project: " + projectID);
				e.printStackTrace();
			}
		});

	}

}
