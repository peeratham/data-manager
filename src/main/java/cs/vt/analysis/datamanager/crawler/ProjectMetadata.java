package cs.vt.analysis.datamanager.crawler;

import java.util.Date;

public class ProjectMetadata {

	private int projectID;
	private String title = "";
	private String creator = "";
	private int favoriteCount = 0;
	private int loveCount = 0;
	private int views = 0;
	private int remixes = 0;
	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public int getLoveCount() {
		return loveCount;
	}

	public void setLoveCount(int loveCount) {
		this.loveCount = loveCount;
	}

	public int getViews() {
		return views;
	}

	public void setViews(int views) {
		this.views = views;
	}

	public int getRemixes() {
		return remixes;
	}

	public void setRemixes(int remixes) {
		this.remixes = remixes;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Date getDateShared() {
		return dateShared;
	}

	public void setDateShared(Date dateShared) {
		this.dateShared = dateShared;
	}

	private Date modifiedDate = null;
	private Date dateShared = null;


	public void setCreator(String creator) {
		this.creator = creator;
	}

	public ProjectMetadata(int projectID) {
		this.projectID = projectID; 
	}

	public int getProjectID() {
		
		return projectID;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String getCreator() {
		return creator;
	}



}
