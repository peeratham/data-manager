package cs.vt.analysis.datamanager.worker;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import cs.vt.analysis.datamanager.main.Main;

public class AnalysisDBManager {
	private static final String METADATA = "metadata";
	private MongoDatabase db = null;
	private MongoClient mongoClient = new MongoClient();
	private static final String ANALYSIS_RESULT_DB = "reports";
	
	public AnalysisDBManager(){
		if(Main.TEST){
			db = mongoClient.getDatabase("test");
		}else{
			db = mongoClient.getDatabase("analysis");
		}
		
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
	
		
}
