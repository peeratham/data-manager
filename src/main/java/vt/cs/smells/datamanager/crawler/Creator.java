package vt.cs.smells.datamanager.crawler;

import java.util.ArrayList;

import org.bson.Document;

public class Creator {

	private String creator;
	ArrayList<Integer> projectsCreated = new ArrayList<Integer>();
	private Document masteryReport;

	public Creator(String name) {
		this.creator = name;
	}

	public void addProjectID(int projectID) {
		projectsCreated.add(projectID);
	}

	public void setMasteryReport(Document masteryReport) {
		this.masteryReport = masteryReport;
	}

	@Override
	public String toString() {
		return "Creator ["
				+ (creator != null ? "creator=" + creator + ", " : "")
				+ (projectsCreated != null ? "projectsCreated="
						+ projectsCreated + ", " : "")
				+ (masteryReport != null ? "masteryReport=" + masteryReport
						: "") + "]";
	}

	public Document toDocument() {
		Document doc = new Document();
		doc.append("_id", creator);
		doc.append("projects_created", projectsCreated);
		doc.append("mastery", masteryReport);
		return doc;
	}
	
	

}
