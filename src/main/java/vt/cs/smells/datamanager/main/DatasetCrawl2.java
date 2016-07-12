package vt.cs.smells.datamanager.main;

import java.io.FileInputStream;
import java.io.IOException;
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

import vt.cs.smells.datamanager.crawler.Crawler;
import vt.cs.smells.datamanager.crawler.ProjectMetadata;
import vt.cs.smells.datamanager.worker.AnalysisDBManager;
import vt.cs.smells.datamanager.worker.Progress;

public class DatasetCrawl2 implements Runnable {
	
	private static int numOfProjects=500;
	public static String configurationFilePath = "";
	private static String databaseName = "test";
	private static AnalysisDBManager DBManager;
	static Logger logger = Logger.getLogger(DatasetCrawl2.class);
	private static double percentCompletion = 0;
	private static String host="localhost";
	private static int downloadedProjects = 0;
	private static long elapsedTime;
	private static int failureCounter = 0;
	private static int newProjectCounter = 0;
	private static long startTime;
	private static long endTime;

	public static void main(String[] args){
		// config log4j
		//set log directory
		Properties props = System.getProperties();
		if(props.getProperty("logDir")!=null){
			props.setProperty("logDir", "./");
		}
		PropertyConfigurator.configure(DatasetCrawl2.class.getClassLoader().getResource("log4j.xml"));
		
		final Options options = createOptions();
		
		Thread srcRetrievalStatusThread = new Thread(new DatasetCrawl2());
		
		try {
			final CommandLine line = getCommandLine(options, args);
			try{
				numOfProjects = Integer.parseInt(line.getOptionValue("n"));
				databaseName = line.getOptionValue("db");
				host = line.getOptionValue("h");
			}catch(Exception e){
				
			}
			
			Crawler crawler = new Crawler();
			crawler.setNumberOfProjectToCollect(numOfProjects);
			
			DBManager = new AnalysisDBManager(host,databaseName);
			
			
//			DBManager.setDBName(databaseName);
			logger.info("Running Project Listing Retrieval");
			logger.info("Number of Projects to Collect: "+numOfProjects);
			
			List<ProjectMetadata> projectMetadataListing = crawler.getProjectsFromQuery();
			System.out.println(projectMetadataListing.size());
			JSONParser parser = new JSONParser();
			
			srcRetrievalStatusThread.start();
			startTime = System.nanoTime();
			for (int i = 0; i < projectMetadataListing.size(); i++) {
				ProjectMetadata current = projectMetadataListing.get(i);
				if(DBManager.findMetadata(current.getProjectID()) != null){
					downloadedProjects++;
					continue;
				}
				try {
					current = crawler.retrieveProjectMetadata(current);
					if(current!=null){
					String src = crawler.retrieveProjectSourceFromProjectID(current.getProjectID());
						String singleLineJSONSrc = ((JSONObject) parser.parse(src)).toJSONString();
						DBManager.putSource(current.getProjectID(), singleLineJSONSrc);
						DBManager.putMetadata(current.toDocument());
						downloadedProjects++;
						newProjectCounter ++;
					}
				} catch (Exception e) {
					failureCounter ++;
					logger.error("ID:"+ current.getProjectID()+" - "+e);
				}
			}
			endTime = System.nanoTime();
			elapsedTime = endTime-startTime;
			
			
			srcRetrievalStatusThread.interrupt();
			
			StringBuilder sb = new StringBuilder();
			sb.append("Complete\n");
			sb.append("Projects Downloaded: "+downloadedProjects+ "/" + numOfProjects +"\n");
			sb.append("New projects : "+newProjectCounter +"\n");
			sb.append("Projects already in database : "+ (downloadedProjects-newProjectCounter)+ "\n");
			sb.append("Projects failed to download : "+ failureCounter +"\n");
			sb.append("Time elapsed: "+getElapsedTimeHoursMinutesFromMilliseconds(elapsedTime) +"\n");
			logger.info(sb.toString());

		} catch (Exception e1) {
			e1.printStackTrace();
			srcRetrievalStatusThread.interrupt();
			
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
		options.addOption("db", true, "database name");
		options.addOption("h", true, "host for mongod instance");
		options.addOption("l", true, "log file directory");
		

		return options;
	}
	
	public static String getElapsedTimeHoursMinutesFromMilliseconds(long l) {
		final long hr = TimeUnit.NANOSECONDS.toHours(l);
        final long min = TimeUnit.NANOSECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.NANOSECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        
        return String.format("%02d hr:%02d min:%02d sec", hr, min, sec);
    }


	@Override
	public void run() {
		logger.info("#################################");
		logger.info("Running Project Source Retrieval");
		while(percentCompletion < 100){
			percentCompletion = ((double)downloadedProjects/(double)(numOfProjects-failureCounter))*100;
			try {
				Progress.updateProgress(percentCompletion);
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				break;
			}
		}
		
		percentCompletion = ((double)downloadedProjects/(double)(numOfProjects-failureCounter))*100;
		Progress.updateProgress(percentCompletion);
		Thread.currentThread().interrupt();
		
	}

	
	
}
