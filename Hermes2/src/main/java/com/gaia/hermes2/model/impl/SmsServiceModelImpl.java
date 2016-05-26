package com.gaia.hermes2.model.impl;

import org.bson.Document;

import com.gaia.hermes2.bean.SmsServiceBean;
import com.gaia.hermes2.model.SmsServiceModel;
import com.gaia.hermes2.statics.DBF;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class SmsServiceModelImpl extends HermesAbstractModel implements SmsServiceModel{
	private MongoCollection<Document> collection;

	protected MongoCollection<Document> getCollection() {
		if (this.collection == null) {
			synchronized (this) {
				if (this.collection == null) {
					this.collection = this.getDatabase().getCollection(DBF.DATABASE_SMS_SERVICE);
				}
			}
		}
		return this.collection;
	}
	@Override
	public SmsServiceBean insert(SmsServiceBean bean) {
		Document doc=bean.toDocument();
		getCollection().insertOne(doc);
		if(doc.getObjectId(DBF._ID)!=null){
			bean.setObjectId(doc.getObjectId(DBF._ID));
			return bean;
		}
		return null;
	}

	@Override
	public boolean update(SmsServiceBean bean) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SmsServiceBean findById(String id) {
		Document doc=new Document(DBF.ID, id);
		FindIterable<Document> found=getCollection().find(doc);
		if(found.first()!=null){
			return SmsServiceBean.fromDocument(found.first());
		}
		return null;
	}

	@Override
	public SmsServiceBean findByAppId(String appId) {
		Document doc=new Document(DBF.APPLICATION_ID, appId);
		FindIterable<Document> found=getCollection().find(doc);
		if(found.first()!=null){
			return SmsServiceBean.fromDocument(found.first());
		}
		return null;
	}

	@Override
	public SmsServiceBean findByAppIdAndChecksum(String appId,String checksum){
		Document match = new Document(DBF.APPLICATION_ID, appId);
		match.append(DBF.CHECKSUM, checksum);
		FindIterable<Document> found = getCollection().find(match);
		if (found.first() != null) {
			return SmsServiceBean.fromDocument(found.first());
		}
		return null;
	}
	@Override
	public SmsServiceBean findDefault() {
		Document doc=new Document(DBF.IS_DEFAULT,true);
		FindIterable<Document> found=getCollection().find(doc);
		if(found.first()!=null){
			return SmsServiceBean.fromDocument(found.first());
		}
		return null;
	}
}
