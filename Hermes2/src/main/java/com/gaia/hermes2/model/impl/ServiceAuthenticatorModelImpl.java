package com.gaia.hermes2.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class ServiceAuthenticatorModelImpl extends HermesAbstractModel implements ServiceAuthenticatorModel {
	private MongoCollection<Document> collection;

	protected MongoCollection<Document> getCollection() {
		if (this.collection == null) {
			synchronized (this) {
				if (this.collection == null) {
					this.collection = this.getDatabase().getCollection(DBF.DATABASE_SERVICE_AUTHENTICATOR);
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
	public List<ServiceAuthenticatorBean> findByAppId(String appId) {
		Document match = new Document(DBF.APPLICATION_ID, appId);
		FindIterable<Document> found = getCollection().find(match);
		List<ServiceAuthenticatorBean> beans = new ArrayList<>();
		for (Document d : found) {
			beans.add(ServiceAuthenticatorBean.fromDocument(d));
		}
		return beans;
	}

	@Override
	public ServiceAuthenticatorBean findByAppIdAndChecksum(String appId, String checksum) {
		Document match = new Document(DBF.APPLICATION_ID, appId);
		match.append(DBF.CHECKSUM, checksum);
		FindIterable<Document> found = getCollection().find(match);
		if (found.first() != null) {
			return ServiceAuthenticatorBean.fromDocument(found.first());
		}
		return null;
	}

	@Override
	public ServiceAuthenticatorBean findByBundleId(String bundleId, String serviceType, boolean sandbox) {
		Document match = new Document(DBF.BUNDLE_ID, bundleId);
		match.append(DBF.SANDBOX, sandbox);
		if (serviceType != null) {
			match.append(DBF.SERVICE_TYPE, serviceType);
		}
		FindIterable<Document> found = getCollection().find(match);
		if (found.first() != null) {
			return ServiceAuthenticatorBean.fromDocument(found.first());
		}
		return null;
	}

	@Override
	public boolean update(String id, ServiceAuthenticatorBean bean) {
		Document doc = new Document(F.ID, id);
		UpdateResult result = getCollection().updateOne(doc, new Document("$set", bean.toDocument()));
		return result.getModifiedCount() > 0;
	}

	@Override
	public boolean remove(String id) {
		Document doc = new Document(F.ID, id);
		DeleteResult result = getCollection().deleteOne(doc);
		return result.getDeletedCount() > 0;
	}

}
