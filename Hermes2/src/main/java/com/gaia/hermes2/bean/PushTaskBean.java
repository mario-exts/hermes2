package com.gaia.hermes2.bean;

import java.util.UUID;
import org.bson.Document;

import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuObject;
import com.nhb.common.db.beans.AbstractMongoBean;

public class PushTaskBean extends AbstractMongoBean {
	private static final long serialVersionUID = -7588756741029099692L;

	private String id;
	private String appId;
	private int totalCount;
	private int gcmCount;
	private int apnsCount;
	private int gcmSuccessCount;
	private int apnsSuccessCount;
	private int gcmFailureCount;
	private int apnsFailureCount;
	private long startTime;
	private long lastModified;
	private boolean isDone;
	private String pushRequest;

	public PushTaskBean() {
		totalCount = 0;
	}

	@Override
	public Document toDocument() {
		Document doc = new Document();
		doc.put(F.APPLICATION_ID, this.appId);
		doc.put(DBF.ID, this.id);
		doc.put(DBF.TOTAL_COUNT, this.totalCount);
		doc.put(DBF.APNS_COUNT, this.apnsCount);
		doc.put(DBF.GCM_COUNT, this.gcmCount);
		doc.put(DBF.APNS_SUCCESS_COUNT, this.apnsSuccessCount);
		doc.put(DBF.GCM_SUCCESS_COUNT, this.gcmSuccessCount);
		doc.put(DBF.GCM_FAILURE_COUNT, this.gcmFailureCount);
		doc.put(DBF.APNS_FAILURE_COUNT, this.apnsFailureCount);
		doc.put(DBF.START_TIME, this.startTime);
		doc.put(DBF.LAST_MODIFIED, this.lastModified);
		doc.put(DBF.IS_DONE, this.isDone);
		doc.put(DBF.PUSH_REQUEST_DATA, this.pushRequest);
		return doc;
	}

	public static PushTaskBean fromDocument(Document doc) {
		PushTaskBean bean = new PushTaskBean();
		bean.setAppId(doc.getString(DBF.APPLICATION_ID));
		bean.setId(doc.getString(DBF.ID));
		bean.setTotalCount(doc.getInteger(DBF.TOTAL_COUNT, 0));
		bean.setGcmCount(doc.getInteger(DBF.GCM_COUNT, 0));
		bean.setApnsCount(doc.getInteger(DBF.APNS_COUNT, 0));
		bean.setApnsSuccessCount(doc.getInteger(DBF.APNS_SUCCESS_COUNT, 0));
		bean.setGcmSuccessCount(doc.getInteger(DBF.GCM_SUCCESS_COUNT, 0));
		bean.setApnsFailureCount(doc.getInteger(DBF.APNS_FAILURE_COUNT, 0));
		bean.setGcmFailureCount(doc.getInteger(DBF.GCM_FAILURE_COUNT, 0));
		bean.setStartTime(doc.getLong(DBF.START_TIME));
		bean.setLastModified(doc.getLong(DBF.LAST_MODIFIED));
		bean.setDone(doc.getBoolean(DBF.IS_DONE, false));
		bean.setPushRequest(doc.getString(DBF.PUSH_REQUEST_DATA));
		return bean;
	}

	@Override
	public PuObject toPuObject() {
		PuObject puo = new PuObject();
		puo.set(F.ID, this.id);
		puo.set(F.APPLICATION_ID, this.appId);
		puo.set(F.TOTAL_COUNT, this.totalCount);
		PuObject gcm = new PuObject();
		gcm.set(F.GCM_COUNT, this.gcmCount);
		gcm.set(F.GCM_SUCCESS_COUNT, this.gcmSuccessCount);
		gcm.set(F.GCM_FAILURE_COUNT, this.gcmFailureCount);
		puo.set(F.GCM, gcm);
		PuObject apns = new PuObject();
		apns.set(F.APNS_COUNT, this.apnsCount);
		apns.set(F.APNS_SUCCESS_COUNT, this.apnsSuccessCount);
		apns.set(F.APNS_FAILURE_COUNT, this.apnsFailureCount);
		puo.set(F.APNS, apns);
		puo.set(F.START_TIME, this.startTime);
		puo.set(F.LAST_MODIFIED, this.lastModified);
		puo.set(F.IS_DONE, this.isDone);
		puo.set(F.PUSH_REQUEST_DATA, this.pushRequest);
		return puo;
	}
	

	public String getPushRequest() {
		return pushRequest;
	}

	public void setPushRequest(String pushRequest) {
		this.pushRequest = pushRequest;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long doneTime) {
		this.lastModified = doneTime;
	}

	public void autoStartTime() {
		this.startTime = System.currentTimeMillis() / 1000;
	}

	public int getGcmCount() {
		return gcmCount;
	}

	public void setGcmCount(int gcmCount) {
		this.gcmCount = gcmCount;
	}

	public int getApnsCount() {
		return apnsCount;
	}

	public void setApnsCount(int apnsCount) {
		this.apnsCount = apnsCount;
	}

	public int getGcmSuccessCount() {
		return gcmSuccessCount;
	}

	public void setGcmSuccessCount(int gcmSuccessCount) {
		this.gcmSuccessCount = gcmSuccessCount;
	}

	public int getApnsSuccessCount() {
		return apnsSuccessCount;
	}

	public void setApnsSuccessCount(int apnsSuccessCount) {
		this.apnsSuccessCount = apnsSuccessCount;
	}

	public void autoLastModified() {
		this.lastModified = System.currentTimeMillis() / 1000;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public int getGcmFailureCount() {
		return gcmFailureCount;
	}

	public void setGcmFailureCount(int gcmFailureCount) {
		this.gcmFailureCount = gcmFailureCount;
	}

	public int getApnsFailureCount() {
		return apnsFailureCount;
	}

	public void setApnsFailureCount(int apnsFailureCount) {
		this.apnsFailureCount = apnsFailureCount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void autoId() {
		this.id = UUID.randomUUID().toString();
	}
}
