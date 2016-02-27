package cs.vt.analysis.datamanager.worker;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

public class AnalysisDBManager {
	private MongoDatabase db = null;
	private MongoClient mongoClient = new MongoClient();
	public AnalysisDBManager(){
		db = mongoClient.getDatabase("test");
	}
	public void insertMetadata(Document doc) {
		db.getCollection("metadata").insertOne(doc);
	}
	public Document findProject(int projectID) {
		FindIterable<Document> iterable = db.getCollection("metadata").find(eq("_id", projectID));
		return iterable.first();
		
		
		
	}
	public long removeMetadata(int projectID) {
		DeleteResult result = db.getCollection("metadata").deleteOne(eq("_id", projectID));
		return result.getDeletedCount();
	}
	public void clearMetadata() {
		db.getCollection("metadata").drop();
	}
	
	public long getMetadataSize(){
		return db.getCollection("metadata").count();
	}
		
}