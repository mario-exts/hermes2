package com.gaia.hermes2.bean;

import org.bson.Document;
import org.bson.types.Binary;

import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuObject;
import com.nhb.common.db.beans.AbstractMongoBean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceAuthenticatorBean extends AbstractMongoBean{

	private static final long serialVersionUID = 181129615256052415L;
	
	private String id;
	private String appId;
	private String serviceType;
	private byte[] authenticator;
	private String password;
	private String checksum;
	private boolean sandbox;
	private String topic;
	private String bundleId;
	
	@Override
	public Document toDocument() {
		Document doc=new Document();
		doc.put(DBF.ID, this.id);
		doc.put(DBF.APPLICATION_ID, this.appId);
		doc.put(DBF.SERVICE_TYPE, this.serviceType);
		doc.put(DBF.AUTHENTICATOR, this.authenticator);
		doc.put(DBF.PASSWORD, this.password);
		doc.put(DBF.CHECKSUM, this.checksum);
		doc.put(DBF.SANDBOX, this.sandbox);
		if(topic!=null){
			doc.put(DBF.TOPIC, this.topic);
		}
		if(bundleId!=null){
			doc.put(DBF.BUNDLE_ID, this.bundleId);
		}
		return doc;
	}
	
	public static ServiceAuthenticatorBean fromDocument(Document doc){
		ServiceAuthenticatorBean bean=new ServiceAuthenticatorBean();
		
		bean.setObjectId(doc.getObjectId(DBF._ID));
		bean.setId(doc.getString(DBF.ID));
		bean.setAppId(doc.getString(DBF.APPLICATION_ID));
		bean.setServiceType(doc.getString(DBF.SERVICE_TYPE));
		bean.setAuthenticator(doc.get(DBF.AUTHENTICATOR,Binary.class).getData());
		bean.setPassword(doc.getString(DBF.PASSWORD));
		bean.setChecksum(doc.getString(DBF.CHECKSUM));
		bean.setSandbox(doc.getBoolean(DBF.SANDBOX, false));
		if(doc.containsKey(DBF.TOPIC)){
			bean.setTopic(doc.getString(DBF.TOPIC));
		}
		if(doc.containsKey(DBF.BUNDLE_ID)){
			bean.setTopic(doc.getString(DBF.BUNDLE_ID));
		}
		return bean;
	}
	
	public PuObject toPuObject(){
		PuObject puo=new PuObject();
		puo.set(F.ID, this.id);
		puo.set(F.APPLICATION_ID, this.appId);
		puo.set(F.SERVICE_TYPE, this.serviceType);
		puo.setRaw(F.AUTHENTICATOR, this.authenticator);
		puo.set(F.PASSWORD, this.password);
		puo.set(F.CHECKSUM, this.checksum);
		puo.set(F.SANDBOX, this.sandbox);
		if(topic!=null){
			puo.set(F.TOPIC, this.topic);
		}
		if(bundleId!=null){
			puo.set(F.BUNDLE_ID, this.bundleId);
		}
		return puo;
	}
	
}
