package vt.cs.smells.datamanager.crawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.bson.Document;

import vt.cs.smells.datamanager.worker.Progress;

public class Crawler {
	public static final String baseURL = "https://api.scratch.mit.edu/search/projects?limit=16&offset=%1$d&language=en&q=*";
	public static final String baseDownLoadURL = "http://projects.scratch.mit.edu/internalapi/project/%1$d/get/";
	private static final String pageURL = "https://scratch.mit.edu/projects/%1$d";
	static Logger logger = Logger.getLogger(Crawler.class);

	private int numProjectToCollect;
	private JSONParser parser = new JSONParser();
	private ArrayList<ProjectMetadata> result = new ArrayList<ProjectMetadata>();
	private double percentCompletion = 0;
	private Thread statusUpdateThread;

	Runnable updateProjectListingStatus = () -> {
		while (result.size() < numProjectToCollect) {
			percentCompletion = ((double) result.size() / (double) (numProjectToCollect)) * 100;
			try {
				Progress.updateProgress(percentCompletion);
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				break;
			}
		}

		percentCompletion = ((double) result.size() / (double) (numProjectToCollect)) * 100;
		Progress.updateProgress(percentCompletion);
		logger.info("\nTotal Project IDs Retrieved: " + result.size());
		Thread.currentThread().interrupt();
	};

	public Crawler() {
		 statusUpdateThread = new Thread(updateProjectListingStatus);
		 statusUpdateThread.start();
	}

	public void setNumberOfProjectToCollect(int num) {
		numProjectToCollect = num;
	}

	public List<ProjectMetadata> getProjectsFromQuery() {
		int offset = 0;
		Set<Integer> projectIDs = new HashSet<>();
		while (result.size() < numProjectToCollect) {
			String URL = String.format(baseURL, offset);
			RetryOnException retry = new RetryOnException(3, 2000);
			while (retry.shouldRetry()) {
				try {
					String doc = Jsoup.connect(URL).ignoreContentType(true)
							.execute().body();
					JSONArray projectListing = (JSONArray) parser.parse(doc);

					for (int i = 0; i < projectListing.size(); i++) {
						JSONObject obj = (JSONObject) projectListing.get(i);
						Document document = Document.parse(obj.toJSONString());
						Integer projectID = document.getInteger("id");
						
						if(projectIDs.contains(projectID)){
							continue;
						}else{
							projectIDs.add(projectID);
						}
						String title = document.getString("title");
						Document remixDoc = (Document) document.get("remix");
						if (remixDoc.getInteger("root") == null) {
//							System.out.println("original project");
						}

						Document historyDoc = (Document) document
								.get("history");
						String created = historyDoc.getString("created");
						String modified = historyDoc.getString("modified");
						String shared = historyDoc.getString("shared");

						Document statsDoc = (Document) document.get("stats");
						Integer views = statsDoc.getInteger("views");
						Integer loves = statsDoc.getInteger("loves");
						Integer favorites = statsDoc.getInteger("favorites");

						ProjectMetadata proj = new ProjectMetadata(projectID);
						proj.setTitle(title);
						result.add(proj);
						if (result.size() >= numProjectToCollect) {
							break;
						}
					}
					break;
				} catch (Exception e) {
					try {
						retry.errorOccured();
					} catch (Exception failAttemptException) {
						logger.error("Skipped crawling for listing @ offset "
								+ offset);
						logger.error("Exception while calling URL:" + URL);
						break;

					}
				}
			}
			offset += 16;
		}
		
		statusUpdateThread.interrupt();
		return result;
	}

	

	public int getNumberOfProjectToCollect() {
		return numProjectToCollect;
	}

	public static ProjectMetadata retrieveProjectMetadata(ProjectMetadata metadata)
			throws Exception {
		RetryOnException retry = new RetryOnException(3, 2000);
		org.jsoup.nodes.Document doc = null;
		String projectPageURL = String.format(pageURL, metadata.getProjectID());
		while (retry.shouldRetry()) {
			try {
				doc = Jsoup.connect(projectPageURL).get();
				break;
			} catch (Exception e) {
				try {
					retry.errorOccured();
				} catch (Exception failAttemptException) {
					// throw new RuntimeException("Exception while calling URL:"
					// + projectPageURL, failAttemptException);
					// logger.error("Error retrieving metadata for project: "+
					// metadata.getProjectID() + "...skipping..." );
					return null;
				}
			}
		}

		if (doc == null) {
			// throw new Exception("Fail to retrieve project at:" +
			// projectPageURL);
		}
		
		//shared?
//		#share-bar > span
		Elements shareBar = doc.select("div#share-bar");
		if(shareBar.hasText()){
			throw new Exception("Not shared");
		}

		Elements favCntSpan = doc.select("span[data-content=\"fav-count\"]");
		int favoriteCount = Integer.parseInt(favCntSpan.text());
		metadata.setFavoriteCount(favoriteCount);

		Elements loveCntSpan = doc.select("span[data-content=\"love-count\"]");
		int loveCount = Integer.parseInt(loveCntSpan.text());
		metadata.setLoveCount(loveCount);

		Elements viewsSpan = doc.select("span.icon.views");
		int views = Integer.parseInt(viewsSpan.text());
		metadata.setViews(views);

		Elements remixesSpan = doc.select("span.icon.remix-tree");
		int remixes = Integer
				.parseInt(remixesSpan.text().replace("\u00a0", "")) - 1;
		metadata.setRemixes(remixes);

		Elements scriptElements = doc.getElementsByTag("script");
		String creator = extractValueFromHTMLScript(scriptElements, "creator");
		metadata.setCreator(creator);

		String title = extractValueFromHTMLScript(scriptElements, "title");
		metadata.setTitle(title);

		SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy");

		Elements modifiedDateElm = doc.select("span.date-updated");
		String modifiedDateStr = modifiedDateElm.text();
		modifiedDateStr = modifiedDateStr.substring(
				modifiedDateStr.indexOf(":") + 1, modifiedDateStr.length())
				.trim();
		Date modifiedDate = formatter.parse(modifiedDateStr);
		metadata.setModifiedDate(modifiedDate);

		Elements dateSharedElm = doc.select("span.date-shared");
		String dateSharedStr = dateSharedElm.text();
		dateSharedStr = dateSharedStr.substring(dateSharedStr.indexOf(":") + 1,
				dateSharedStr.length()).trim();
		Date dateShared = formatter.parse(dateSharedStr);
		metadata.setDateShared(dateShared);

		// get original
		// if project itself is original ;set itself original
		Element originalProjectLink = doc.select(
				"#remix-history ul li div span a").first();
		if (originalProjectLink != null) {
			String originalProjectID = originalProjectLink.attr("href");
			int origin = extractOriginalProjectIDFromURLString(originalProjectID);
			metadata.setOriginalProject(origin);
		} else {
			metadata.setOriginalProject(metadata.getProjectID());
		}

		return metadata;
	}

	private static int extractOriginalProjectIDFromURLString(String originalProjectID) {
		int secondSlash = originalProjectID.indexOf('/', 1);
		int lastSlash = originalProjectID.indexOf('/', secondSlash + 1);
		String projectID = originalProjectID.substring(secondSlash + 1,
				lastSlash);
		return Integer.parseInt(projectID);
	}

	private static String extractValueFromHTMLScript(Elements scriptElements,
			String keyword) {
		String result = "";
		String patternStr = "(?:" + keyword + ": )'([^,']*)'";
		Pattern pattern = Pattern.compile(patternStr);
		String scriptString = scriptElements.toString();
		Matcher matcher = pattern.matcher(scriptString);
		if (matcher.find()) {
			result = matcher.group(1);
		}
		return result;
	}

	public static String retrieveProjectSourceFromProjectID(int projectID)
			throws Exception {
		String projectSrcURL = String.format(baseDownLoadURL, projectID);
		RetryOnException retry = new RetryOnException(3, 2000);
		String src = null;
		while (retry.shouldRetry()) {
			try {
				src = Jsoup.connect(projectSrcURL).ignoreContentType(true)
						.execute().body();
				break;
			} catch (Exception e) {
				try {
					retry.errorOccured();
				} catch (Exception failAttemptException) {
					// throw new RuntimeException("Exception while calling URL:"
					// + projectSrcURL, failAttemptException);
					logger.error("fail to retrieve source for: " + projectID
							+ "...skipping...");
				}
			}
		}

		// if (src == null) {
		// throw new Exception("Fail to retrieve project at:" + projectSrcURL);
		// }

		return src;
	}

	public static boolean checkIfExists(int projectID) {
		String projectSrcURL = String.format(baseDownLoadURL, projectID);
		Response con = null;
		RetryOnException timeOutHandler = new RetryOnException();

		while (timeOutHandler.shouldRetry()) {
			try {
				con = Jsoup.connect(projectSrcURL).ignoreContentType(true)
						.execute();
				return true;
			} catch (HttpStatusException e) {
				// System.err.println(projectID+": "+e.getStatusCode());
				return false;
			} catch (SocketTimeoutException e) {
				// System.err.println("checking: "+projectID);
				try {
					timeOutHandler.errorOccured();
				} catch (Exception failAttemptException) {
					return false; // exceed allowed number of tries
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

		}

		return true;

	}
	
}
