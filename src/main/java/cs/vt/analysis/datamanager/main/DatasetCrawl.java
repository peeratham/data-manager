package cs.vt.analysis.datamanager.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import cs.vt.analysis.datamanager.crawler.Crawler;
import cs.vt.analysis.datamanager.crawler.ProjectMetadata;
import cs.vt.analysis.datamanager.worker.AnalysisDBManager;
import cs.vt.analysis.datamanager.worker.FileResourceManager;

public class DatasetCrawl {
	
	private static int numOfProjects=0;
	public static String configurationFilePath = "";
	private static String outputDirectory = "";
	private static String databaseName = "";
	private static AnalysisDBManager DBManager;
	private static FileResourceManager resourceManager;
	static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args){
		// config log4j
		PropertyConfigurator.configure(Main.class.getClassLoader().getResource("log4j.properties"));
		final Options options = createOptions();
		try {
			final CommandLine line = getCommandLine(options, args);
			numOfProjects = Integer.parseInt(line.getOptionValue("n"));
			configurationFilePath = line.getOptionValue("cf");
			Properties prop = new Properties();
			InputStream inputStream = new FileInputStream(configurationFilePath);
			try {
				prop.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			outputDirectory = prop.getProperty("OUTPUT_DIR")
			databaseName = prop.getProperty("DB_NAME");
			Crawler crawler = new Crawler();
			crawler.setNumberOfProjectToCollect(numOfProjects);
			DBManager = new AnalysisDBManager(databaseName);
			resourceManager = new FileResourceManager();
			resourceManager.setDatasetDirectory(outputDirectory);
			
			List<ProjectMetadata> projectMetadataListing = crawler.getProjectsFromQuery();
			JSONParser parser = new JSONParser();
			int downloadedProjects = 0;
			long startTime = System.nanoTime();
			for (int i = 0; i < projectMetadataListing.size(); i++) {
				ProjectMetadata current = projectMetadataListing.get(i);
				logger.info("Processing:"+current.getProjectID());
				if(DBManager.findMetadata(current.getProjectID()) != null){
					continue;
				}
				try {
					crawler.retrieveProjectMetadata(current);
					String src = crawler.retrieveProjectSourceFromProjectID(current.getProjectID());
					String singleLineJSONSrc = ((JSONObject) parser.parse(src)).toJSONString();
					resourceManager.write(current.getProjectID() + ".json", singleLineJSONSrc);
					DBManager.insertMetadata(current.toDocument());
					downloadedProjects++;
					double percentCompleteion = ((double)downloadedProjects/(double)numOfProjects)*100;
					logger.info(percentCompleteion+"%  "+i + "/" + numOfProjects + " saved metadata for project" + current.getProjectID());

				} catch (ParseException | IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					logger.error("Fail to retrieve project: "+current.getProjectID());
					e.printStackTrace();
				}
			}
			long completionTime = System.nanoTime();
			long elapsedTime = completionTime-startTime;
			
			logger.info("COMPLETE");
			logger.info("Time elapsed: "+getElapsedTimeHoursMinutesFromMilliseconds(elapsedTime));
			

		} catch (Exception e1) {
			e1.printStackTrace();
			
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
		options.addOption("n", true, "number of projects to download");
		options.addOption("cf", true, "configuration file for crawler");
		

		return options;
	}
	
	public static String getElapsedTimeHoursMinutesFromMilliseconds(long l) {
		final long hr = TimeUnit.NANOSECONDS.toHours(l);
        final long min = TimeUnit.NANOSECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.NANOSECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        
        return String.format("%02d hr:%02d min:%02d sec", hr, min, sec);
    }

	
	
}
