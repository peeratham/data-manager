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
	private static final String METADATA = "metadata";
	private static final String ANALYSIS_RESULT_DB = "reports";
	private static final String CREATOR = "creators";
	private MongoDatabase db = null;
	private MongoClient mongoClient = new MongoClient();
	
	
	public AnalysisDBManager(){
		if(Main.TEST){
			db = mongoClient.getDatabase("test");
		}else{
			db = mongoClient.getDatabase("analysis");
		}
		
		db.getCollection(CREATOR).createIndex(new Document("creator", "text"));
		
	}
	
	public void insertMetadata(Document doc) {
		int projectID = (Integer) doc.get("_id");
		if(findMetadata(projectID) == null){
			db.getCollection(METADATA).insertOne(doc);
		}else{
			db.getCollection(METADATA).findOneAndReplace(eq("_id", projectID), doc);
		}
		
	}
	
	public Document findMetadata(int projectID) {
		FindIterable<Document> iterable = db.getCollection(METADATA).find(eq("_id", projectID));
		return iterable.first();	
	}
	
	public long removeMetadata(int projectID) {
		DeleteResult result = db.getCollection(METADATA).deleteOne(eq("_id", projectID));
		return result.getDeletedCount();
	}
	
	public void clearMetadata() {
		db.getCollection(METADATA).drop();
	}
	
	public long getMetadataSize(){
		return db.getCollection(METADATA).count();
	}
	public void insertAnalysisReport(Document report) {
		db.getCollection(ANALYSIS_RESULT_DB).insertOne(report);
	}
	
	public void clearAnalysisReport() {
		db.getCollection(ANALYSIS_RESULT_DB).drop();
	}
	
	public Document findAnalysisReport(int projectID) {
		FindIterable<Document> iterable = db.getCollection(ANALYSIS_RESULT_DB).find(eq("_id", projectID));
		return iterable.first();	
	}
	
	public long removeAnalysisReport(int projectID) {
		DeleteResult result = db.getCollection(ANALYSIS_RESULT_DB).deleteOne(eq("_id", projectID));
		return result.getDeletedCount();
	}

	public Document findCreatorRecord(String creator){
		FindIterable<Document> iterable = db.getCollection(CREATOR).find(eq("creator", creator));
		return iterable.first();	
	}
	
	
	public void addCreatorRecord(Document creatorDoc) {
		db.getCollection(CREATOR).insertOne(creatorDoc);
	}
	
	public void updateCreatorRecord(Document creatorDoc){
		String creatorName = (String) creatorDoc.get("creator");
		Document matchedRecord = findCreatorRecord(creatorName);
		if(matchedRecord == null){
			db.getCollection(CREATOR).insertOne(creatorDoc);
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
			
			db.getCollection(CREATOR).findOneAndReplace(eq("_id", matchedRecord.get("_id")), matchedRecord);
		}
		
	}

	public void deleteCreator(String creatorName) {
		db.getCollection(CREATOR).deleteOne(eq("creator", creatorName));
		
	}

	public String lookUpCreator(int projectID) {
		Document metadata = findMetadata(projectID);
		return metadata.getString("creator");
	}
	
		
}
