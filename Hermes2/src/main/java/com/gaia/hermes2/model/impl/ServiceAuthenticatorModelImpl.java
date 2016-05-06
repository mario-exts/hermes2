package com.gaia.hermes2.model.impl;

import org.bson.Document;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.model.HermesBaseModel;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.statics.DBF;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class ServiceAuthenticatorModelImpl extends HermesBaseModel implements ServiceAuthenticatorModel {
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
	public ServiceAuthenticatorBean insert(ServiceAuthenticatorBean bean) {
		Document doc = bean.toDocument();
		getCollection().insertOne(doc);
		if (doc.getObjectId(DBF._ID) != null) {
			bean.setObjectId(doc.getObjectId(DBF._ID));
			return bean;
		}
		return null;
	}

	@Override
	public ServiceAuthenticatorBean findById(String id) {
		Document match = new Document(DBF.ID, id);
		FindIterable<Document> found = getCollection().find(match);
		if (found.first() != null) {
			return ServiceAuthenticatorBean.fromDocument(found.first());
		}
		return null;
	}

	@Override
	public ServiceAuthenticatorBean findByAppIdAndChecksum(String appId,String checksum){
		Document match = new Document(DBF.APPLICATION_ID, appId);
		match.append(DBF.CHECKSUM, checksum);
		FindIterable<Document> found = getCollection().find(match);
		if (found.first() != null) {
			return ServiceAuthenticatorBean.fromDocument(found.first());
		}
		return null;
	}

}
