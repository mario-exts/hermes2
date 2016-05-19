package com.gaia.hermes2.model.impl;

import org.bson.Document;

import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

public class PushTaskModelImpl extends HermesAbstractModel implements com.gaia.hermes2.model.PushTaskModel {

	private MongoCollection<Document> collection;

	protected MongoCollection<Document> getCollection() {
		if (this.collection == null) {
			synchronized (this) {
				if (this.collection == null) {
					this.collection = this.getDatabase().getCollection(DBF.DATABASE_PUSH_TASK);
				}
			}
		}
		return this.collection;
	}

	@Override
	public PushTaskBean insert(PushTaskBean bean) {
		Document doc = bean.toDocument();
		getCollection().insertOne(doc);
		bean.setObjectId(doc.getObjectId(F._ID));
		return bean;
	}

	@Override
	public boolean update(PushTaskBean bean) {
		Document update = bean.toDocument();
		UpdateResult result = getCollection().updateOne(new Document(F._ID, bean.getObjectId()),
				new Document("$set", update));
		return result.getModifiedCount() > 0 ? true : false;
	}

	@Override
	public PushTaskBean findByTaskId(String id) {
		Document find = new Document(F.ID, id);
		FindIterable<Document> found = getCollection().find(find);
		if (found != null && found.first() != null) {
			return PushTaskBean.fromDocument(found.first());
		}
		return null;
	}

	@Override
	public boolean updateGcm(String taskId, int gcmSuccess, int gcmFailure) {
		Document match = new Document(DBF.ID, taskId);
		Document update = new Document("$inc",
				new Document(DBF.GCM_SUCCESS_COUNT, gcmSuccess).append(DBF.GCM_FAILURE_COUNT, gcmFailure));
		update.append("$set", new Document(DBF.LAST_MODIFIED, currentTime()));
//		update.append("$currentDate", new Document(DBF.LAST_MODIFIED,
//				new Document("$type", "timestamp")));
		UpdateResult result = getCollection().updateOne(match, update);
		return result.getModifiedCount() > 0 ? true : false;
	}

	@Override
	public boolean updateApns(String taskId, int apnsSuccess, int apnsFailure) {
		Document match = new Document(DBF.ID, taskId);
		Document update = new Document("$inc",
				new Document(DBF.APNS_SUCCESS_COUNT, apnsSuccess).append(DBF.APNS_FAILURE_COUNT, apnsFailure));
		update.append("$set", new Document(DBF.LAST_MODIFIED, currentTime()));
//		update.append("$currentDate", new Document(DBF.LAST_MODIFIED,
//				new Document("$type", "timestamp")));
		UpdateResult result = getCollection().updateOne(match, update);
		return result.getModifiedCount() > 0 ? true : false;
	}

	@Override
	public boolean updateTaskState(String taskId, boolean isDone) {
		Document match = new Document(DBF.ID, taskId);
		Document update = new Document("$set",new Document(DBF.IS_DONE,isDone)
				.append(DBF.LAST_MODIFIED, currentTime()));
//		update.append("$currentDate", new Document(DBF.LAST_MODIFIED,
//				new Document("$type", "timestamp")));
		
		UpdateResult result = getCollection().updateOne(match, update);
		return result.getModifiedCount() > 0 ? true : false;
	}
	
	private long currentTime(){
		return System.currentTimeMillis()/1000;
	}
}
