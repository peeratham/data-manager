package cs.vt.analysis.datamanager.worker;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashSet;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import cs.vt.analysis.datamanager.main.Main;

public class AnalysisDBManager {
	private static final String METADATA_COLLECTION_NAME = "metadata";
	private static final String REPORT_COLLECTION_NAME = "reports";
	private static final String CREATOR_COLLECTION_NAME = "creators";
	private MongoDatabase db = null;
	private MongoClient mongoClient = new MongoClient();
	
	
	public AnalysisDBManager(){
		setAnalysisDBManagerForTest(false);		
		db.getCollection(CREATOR_COLLECTION_NAME).createIndex(new Document("creator", "text"));
		
	}
	
	public void setAnalysisDBManagerForTest(boolean isTest){
		if(isTest){
			db = mongoClient.getDatabase("test");
		}else{
			db = mongoClient.getDatabase("analysis");
		}
	}
	
	public AnalysisDBManager(String DBName){
		db = mongoClient.getDatabase(DBName);
		db.getCollection(CREATOR_COLLECTION_NAME).createIndex(new Document("creator", "text"));
	}
	
	public void putMetadata(Document doc) {
		int projectID = (Integer) doc.get("_id");
		if(findMetadata(projectID) == null){
			db.getCollection(METADATA_COLLECTION_NAME).insertOne(doc);
		}else{
			db.getCollection(METADATA_COLLECTION_NAME).findOneAndReplace(eq("_id", projectID), doc);
		}
		
	}
	
	public Document findMetadata(int projectID) {
		FindIterable<Document> iterable = db.getCollection(METADATA_COLLECTION_NAME).find(eq("_id", projectID));
		if(iterable==null){
			return null;
		}else{
			return iterable.first();
		}
			
	}
	
	public Document findReport(int projectID){
		FindIterable<Document> iterable = db.getCollection(REPORT_COLLECTION_NAME).find(eq("_id", projectID));
		if(iterable==null){
			return null;
		}else{
			return iterable.first();
		}
	}
	
	public long removeMetadata(int projectID) {
		DeleteResult result = db.getCollection(METADATA_COLLECTION_NAME).deleteOne(eq("_id", projectID));
		return result.getDeletedCount();
	}
	
	public void clearMetadata() {
		db.getCollection(METADATA_COLLECTION_NAME).drop();
	}
	
	public long getMetadataSize(){
		return db.getCollection(METADATA_COLLECTION_NAME).count();
	}
	public void putAnalysisReport(int projectID, Document report) {
		if(findReport(projectID) == null){
			db.getCollection(REPORT_COLLECTION_NAME).insertOne(report);
		}else{
			db.getCollection(REPORT_COLLECTION_NAME).findOneAndReplace(eq("_id", projectID), report);
		}
		
	}
	
	public void clearAnalysisReport() {
		db.getCollection(REPORT_COLLECTION_NAME).drop();
	}
	
	public Document findAnalysisReport(int projectID) {
		FindIterable<Document> iterable = db.getCollection(REPORT_COLLECTION_NAME).find(eq("_id", projectID));
		return iterable.first();	
	}
	
	public long removeAnalysisReport(int projectID) {
		DeleteResult result = db.getCollection(REPORT_COLLECTION_NAME).deleteOne(eq("_id", projectID));
		return result.getDeletedCount();
	}

	public Document findCreatorRecord(String creator){
		FindIterable<Document> iterable = db.getCollection(CREATOR_COLLECTION_NAME).find(eq("creator", creator));
		return iterable.first();	
	}
	
	
	public void addCreatorRecord(Document creatorDoc) {
		db.getCollection(CREATOR_COLLECTION_NAME).insertOne(creatorDoc);
	}
	
	public void putCreatorRecord(Document creatorDoc){
		String creatorName = (String) creatorDoc.get("creator");
		Document matchedRecord = findCreatorRecord(creatorName);
		if(matchedRecord == null){
			db.getCollection(CREATOR_COLLECTION_NAME).insertOne(creatorDoc);
		}else{
			//update to max
			ArrayList<Integer> projects = (ArrayList<Integer>) matchedRecord.get("projects_created");
			ArrayList<Integer> projectsToAdd =(ArrayList<Integer>) creatorDoc.get("projects_created");
			HashSet<Integer> uniqueProjects = new HashSet<Integer>(projects);
			uniqueProjects.addAll(projectsToAdd);
			matchedRecord.put("projects_created", uniqueProjects);
			
			Document scores = (Document) matchedRecord.get("mastery");
			Document scoresToUpdate= (Document) creatorDoc.get("mastery");
			for(String concept : scores.keySet()){
				scores.put(concept, Math.max((int)scores.get(concept), (int)scoresToUpdate.get(concept)));
			}
			matchedRecord.put("mastery", scores);
			
			db.getCollection(CREATOR_COLLECTION_NAME).findOneAndReplace(eq("_id", matchedRecord.get("_id")), matchedRecord);
		}
		
	}

	public void deleteCreator(String creatorName) {
		db.getCollection(CREATOR_COLLECTION_NAME).deleteOne(eq("creator", creatorName));
		
	}

	public String lookUpCreator(int projectID) {
		Document metadata = findMetadata(projectID);
		if(metadata==null){
			return null;
		}else{
			return metadata.getString("creator");
		}
		
	}

	public long getReportSize() {
		return db.getCollection(REPORT_COLLECTION_NAME).count();
	}

	public void clearCreatorRecords() {
		db.getCollection(CREATOR_COLLECTION_NAME).drop();
		
	}

	public long getCreatorsSize() {
		return db.getCollection(CREATOR_COLLECTION_NAME).count();
	}
	
		
}
