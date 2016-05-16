package com.gaia.hermes2.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.gaia.hermes2.bean.DeviceTokenBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.statics.DBF;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.WriteModel;

public class DeviceTokenModelImpl extends HermesAbstractModel implements DeviceTokenModel {
	private MongoCollection<Document> collection;
	private boolean useSandbox = false;

	protected MongoCollection<Document> getCollection() {
		if (this.collection == null) {
			synchronized (this) {
				if (this.collection == null) {
					if (useSandbox) {
						this.collection = this.getDatabase().getCollection(DBF.DATABASE_DEVICE_TOKEN_SANDBOX);
					} else {
						this.collection = this.getDatabase().getCollection(DBF.DATABASE_DEVICE_TOKEN);
					}
				}
			}
		}
		return this.collection;
	}

	@Override
	public DeviceTokenBean insert(DeviceTokenBean bean) {
		Document doc = bean.toDocument();
		getCollection().insertOne(doc);
		if (doc.getObjectId(DBF._ID) != null) {
			bean.setObjectId(doc.getObjectId(DBF._ID));
			return bean;
		}
		return null;
	}

	@Override
	public DeviceTokenBean findById(String id) {
		Document match = new Document(DBF.ID, "id");
		FindIterable<Document> found = getCollection().find(match);
		if (found != null && found.first() != null) {
			return DeviceTokenBean.fromDocument(found.first());
		}
		return null;
	}

	@Override
	public List<DeviceTokenBean> findByAppId(String appId) {
		Document match = new Document(DBF.APPLICATION_ID, appId);
		FindIterable<Document> found = getCollection().find(match);
		List<DeviceTokenBean> beans = new ArrayList<>();
		for (Document doc : found) {
			beans.add(DeviceTokenBean.fromDocument(doc));
		}
		return beans;
	}

	@Override
	public void setSandbox(boolean useSandbox) {
		this.useSandbox = useSandbox;
	}

	@Override
	public List<DeviceTokenBean> findByAppIdAndServiceType(String appId, String serviceType) {
		Document match = new Document(DBF.APPLICATION_ID, appId);
		match.append(DBF.SERVICE_TYPE, serviceType);
		FindIterable<Document> found = getCollection().find(match);
		List<DeviceTokenBean> beans = new ArrayList<>();
		for (Document doc : found) {
			beans.add(DeviceTokenBean.fromDocument(doc));
		}
		return beans;
	}

	@Override
	public List<DeviceTokenBean> findByToken(String token) {
		Document match = new Document(DBF.TOKEN, token);
		FindIterable<Document> found = getCollection().find(match);
		List<DeviceTokenBean> beans = new ArrayList<>();
		for (Document doc : found) {
			beans.add(DeviceTokenBean.fromDocument(doc));
		}
		return beans;
	}

	@Override
	public DeviceTokenBean findByChecksum(String checksum) {
		Document match = new Document(DBF.CHECKSUM, checksum);
		FindIterable<Document> found = getCollection().find(match);
		if (found != null && found.first() != null) {
			return DeviceTokenBean.fromDocument(found.first());
		}
		return null;
	}

	@Override
	public void insert(List<DeviceTokenBean> beans) {
		List<Document> batchDoc=new ArrayList<>();
		for(DeviceTokenBean bean:beans){
			batchDoc.add(bean.toDocument());
		}
		getCollection().insertMany(batchDoc);
	}

	@Override
	public int removeMulti(List<String> tokens) {
		MongoCollection<Document> collection = this.getCollection();
		List<WriteModel<Document>> removes = new ArrayList<>();
		for (String token:tokens) {
			removes.add(new DeleteOneModel<>(new Document().append(DBF.TOKEN, token)));
		}
		BulkWriteResult result = collection.bulkWrite(removes);
		getLogger().info("Attempt to remove {} keys, success {}", tokens.size(), result.getModifiedCount());
		return result.getModifiedCount();
	}

	

}
