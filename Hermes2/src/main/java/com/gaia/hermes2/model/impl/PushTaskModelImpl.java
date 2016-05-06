package com.gaia.hermes2.model.impl;

import org.bson.Document;

import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.model.HermesBaseModel;
import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

public class PushTaskModelImpl extends HermesBaseModel implements com.gaia.hermes2.model.PushTaskModel {
	
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
}
