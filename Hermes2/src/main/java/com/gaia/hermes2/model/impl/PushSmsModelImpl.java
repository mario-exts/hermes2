package com.gaia.hermes2.model.impl;

import org.bson.Document;

import com.gaia.hermes2.bean.PushSmsBean;
import com.gaia.hermes2.model.PushSmsModel;
import com.gaia.hermes2.statics.DBF;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

public class PushSmsModelImpl extends HermesAbstractModel implements PushSmsModel {

	private MongoCollection<Document> collection;

	protected MongoCollection<Document> getCollection() {
		if (this.collection == null) {
			synchronized (this) {
				if (this.collection == null) {
					this.collection = this.getDatabase().getCollection(DBF.DATABASE_PUSH_SMS_TASK);
				}
			}
		}
		return this.collection;
	}

	@Override
	public PushSmsBean insert(PushSmsBean bean) {
		Document doc = bean.toDocument();
		getCollection().insertOne(doc);
		if (doc.getObjectId(DBF._ID) != null) {
			bean.setObjectId(doc.getObjectId(DBF._ID));
			return bean;
		}
		return null;
	}

	@Override
	public boolean update(PushSmsBean bean) {
		Document doc = bean.toDocument();
		Document match = new Document(DBF.ID, bean.getId());
		UpdateResult result = getCollection().updateOne(match, doc);
		return result.getModifiedCount() > 0 ? true : false;
	}

	@Override
	public PushSmsBean findById(String id) {
		Document match = new Document(DBF.ID, id);
		FindIterable<Document> found = getCollection().find(match);
		Document doc = found.first();
		if (doc != null) {
			return PushSmsBean.fromDocument(doc);
		}
		return null;
	}

}
