package cs.vt.analysis.datamanager.crawler;

import java.util.ArrayList;

import org.bson.Document;

public class Creator {

	private String _id;
	ArrayList<Integer> projectsCreated = new ArrayList<Integer>();
	private Document masteryReport;

	public Creator(String name) {
		this._id = name;
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
				+ (_id != null ? "creator=" + _id + ", " : "")
				+ (projectsCreated != null ? "projectsCreated="
						+ projectsCreated + ", " : "")
				+ (masteryReport != null ? "masteryReport=" + masteryReport
						: "") + "]";
	}

	public Document toDocument() {
		Document doc = new Document();
		doc.append("creator", _id);
		doc.append("projects_created", projectsCreated);
		doc.append("mastery", masteryReport);
		return doc;
	}
	
	

}
