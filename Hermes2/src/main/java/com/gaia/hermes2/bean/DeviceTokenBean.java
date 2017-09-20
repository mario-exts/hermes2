package com.gaia.hermes2.bean;

import org.bson.Document;

import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.nhb.common.db.beans.AbstractMongoBean;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter

public class DeviceTokenBean extends AbstractMongoBean{
	
	private static final long serialVersionUID = 8708973556290411958L;
	private String id;
	private String token;
	private String serviceType;
	private String checksum;
	private String appId;
	private String productId;
	private String authenticatorId;
	private boolean sandbox;
	
	@Override
	public Document toDocument() {
		Document doc=new Document();
		doc.put(DBF.ID, this.id);
		doc.put(DBF.TOKEN, this.token);
		doc.put(DBF.SERVICE_TYPE, this.serviceType);
		doc.put(DBF.CHECKSUM, this.checksum);
		doc.put(DBF.APPLICATION_ID, this.appId);
		doc.put(DBF.PRODUCT_ID, productId);
		doc.put(DBF.AUTHENTICATOR_ID, this.authenticatorId);
		doc.put(DBF.SANDBOX, sandbox);
		return doc;
	}
	
	public static DeviceTokenBean fromDocument(Document doc){
		DeviceTokenBean bean=new DeviceTokenBean();
		bean.setObjectId(doc.getObjectId(DBF._ID));
		bean.setId(doc.getString(DBF.ID));
		bean.setAppId(doc.getString(DBF.APPLICATION_ID));
		if(doc.containsKey(F.PRODUCT_ID)){
			bean.setProductId(doc.getString(F.PRODUCT_ID));
		}
		bean.setAuthenticatorId(doc.getString(DBF.AUTHENTICATOR_ID));
		bean.setChecksum(doc.getString(DBF.CHECKSUM));
		bean.setServiceType(doc.getString(DBF.SERVICE_TYPE));
		bean.setToken(doc.getString(DBF.TOKEN));
		bean.setSandbox(doc.getBoolean(DBF.SANDBOX, false));
		return bean;
	}
	
}
