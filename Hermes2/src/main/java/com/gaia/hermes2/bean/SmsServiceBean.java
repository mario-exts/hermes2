package com.gaia.hermes2.bean;

import java.util.UUID;

import org.bson.Document;

import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuObject;
import com.nhb.common.db.beans.AbstractMongoBean;

public class SmsServiceBean extends AbstractMongoBean{

	private static final long serialVersionUID = 1222100592141603459L;
	
	private String id;
	private String serviceName;
//	private String profileName;
	private String accessToken;
	private String appId;
	private String password;
	private String checksum;
	private boolean isDefault;
	private boolean sandbox;
	
	@Override
	public Document toDocument() {
		Document doc=new Document();
		doc.put(F.ID, this.id);
		doc.put(F.SERVICE_NAME, this.serviceName);
//		doc.put(F.PROFILE_NAME, this.profileName);
		doc.put(F.ACCESS_TOKEN, this.accessToken);
		doc.put(F.PASSWORD, this.password);
		doc.put(F.CHECKSUM, this.checksum);
		doc.put(F.IS_DEFAULT, this.isDefault);
		doc.put(F.SANDBOX, this.sandbox);
		if(appId!=null){
			doc.put(F.APPLICATION_ID, this.appId);
		}
		return doc;
	}
	
	public static SmsServiceBean fromDocument(Document doc){
		SmsServiceBean bean=new SmsServiceBean();
		bean.setObjectId(doc.getObjectId(DBF._ID));
		bean.setId(doc.getString(F.ID));
		bean.setServiceName(doc.getString(F.SERVICE_NAME));
//		bean.setProfileName(doc.getString(F.PROFILE_NAME));
		bean.setAccessToken(doc.getString(F.ACCESS_TOKEN));
		bean.setPassword(doc.getString(F.PASSWORD));
		bean.setChecksum(doc.getString(F.CHECKSUM));
		bean.setSandbox(doc.getBoolean(F.SANDBOX, false));
		bean.setDefault(doc.getBoolean(F.IS_DEFAULT, false));
		if(doc.containsKey(F.APPLICATION_ID)){
			bean.setAppId(doc.getString(F.APPLICATION_ID));
		}
		
		return bean;
	}

	@Override
	public PuObject toPuObject() {
		PuObject puo=new PuObject();
		puo.set(F.ID, this.id);
		puo.set(F.SERVICE_NAME, this.serviceName);
//		puo.set(F.PROFILE_NAME, this.profileName);
		puo.set(F.ACCESS_TOKEN, this.accessToken);
		puo.set(F.PASSWORD, this.password);
		puo.set(F.CHECKSUM, this.checksum);
		puo.set(F.IS_DEFAULT, this.isDefault);
		puo.set(F.SANDBOX, this.sandbox);
		if(appId!=null){
			puo.set(F.APPLICATION_ID, this.appId);
		}
		return puo;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public void autoId(){
		this.id=UUID.randomUUID().toString();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
//
//	public String getProfileName() {
//		return profileName;
//	}
//
//	public void setProfileName(String profileName) {
//		this.profileName = profileName;
//	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public boolean isSandbox() {
		return sandbox;
	}

	public void setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
	}
	
	
	
}
