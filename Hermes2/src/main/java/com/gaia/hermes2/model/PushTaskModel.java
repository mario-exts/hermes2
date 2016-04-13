package com.gaia.hermes2.model;

import javax.print.Doc;

import org.bson.Document;

import com.gaia.hermes2.Hermes2PushHandler;
import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.statics.F;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.nhb.common.db.models.AbstractModel;

public class PushTaskModel extends AbstractModel{
	MongoDatabase db=null;
	Hermes2PushHandler handler;
	public PushTaskModel(Hermes2PushHandler handler) {
		this.handler=handler;
		db=handler.getDatabase();
	}
	
	public PushTaskBean insert(PushTaskBean bean){
		MongoCollection<Document> collection=db.getCollection(F.DATABASE_PUSH_TASK);
		Document doc=bean.toDocument();
		collection.insertOne(doc);
		bean.setObjectId(doc.getObjectId(F._ID));
		return bean;
	}
	
	public boolean updateGcmPushCount(PushTaskBean bean){
		MongoCollection<Document> collection=db.getCollection(F.DATABASE_PUSH_TASK);
		Document update=new Document(F.GCM_SUCCESS_COUNT,bean.getGcmSuccessCount().intValue());
		update.append(F.GCM_FAILURE_COUNT, bean.getGcmFailureCount().intValue());
		update.append(F.LAST_MODIFY,bean.getLastModify());
		UpdateResult result=collection.updateOne(new Document(F._ID, bean.getObjectId()),
				new Document("$set", update));
		return result.getModifiedCount() > 0?true:false;
	}
	
	public boolean updateApnsPushCount(PushTaskBean bean){
		MongoCollection<Document> collection=db.getCollection(F.DATABASE_PUSH_TASK);
		Document update=new Document(F.APNS_SUCCESS_COUNT,bean.getApnsSuccessCount().intValue());
		update.append(F.APNS_FAILURE_COUNT, bean.getApnsFailureCount().intValue());
		update.append(F.LAST_MODIFY,bean.getLastModify());
		UpdateResult result=collection.updateOne(new Document(F._ID, bean.getObjectId()),
				new Document("$set", update));
		return result.getModifiedCount() > 0?true:false;
	}
	
	public boolean doneTask(PushTaskBean bean){
		MongoCollection<Document> collection=db.getCollection(F.DATABASE_PUSH_TASK);
		Document update=new Document(F.IS_DONE,bean.isDone());
		UpdateResult result=collection.updateOne(new Document(F._ID, bean.getObjectId()),
				new Document("$set", update));
		return result.getModifiedCount() > 0?true:false;
	}
	
	public PushTaskBean findTaskById(String id){
		MongoCollection<Document> collection=db.getCollection(F.DATABASE_PUSH_TASK);
		Document find=new Document(F.ID,id);
		FindIterable<Document> found=collection.find(find);
		if(found!=null && found.first()!=null){
			return PushTaskBean.fromDocument(found.first());
		}
		return null;
	}
}
