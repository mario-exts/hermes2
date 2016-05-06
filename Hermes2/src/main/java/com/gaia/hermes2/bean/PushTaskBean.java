package com.gaia.hermes2.bean;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.Document;

import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuObject;
import com.nhb.common.db.beans.AbstractMongoBean;

public class PushTaskBean extends AbstractMongoBean {
	private static final long serialVersionUID = -7588756741029099692L;

	private String id;
	private String appId;
	// private String serviceType;
	private int totalCount;
	private int gcmCount;
	private int apnsCount;
	private AtomicInteger gcmSuccessCount;
	private AtomicInteger apnsSuccessCount;
	private AtomicInteger gcmFailureCount;
	private AtomicInteger apnsFailureCount;
//	private AtomicInteger totalFailureCount;
	private long startTime;
	private long lastModify;
	private boolean isDone;
	private AtomicInteger threadCount;

	public PushTaskBean() {
		totalCount = 0;
		gcmSuccessCount = new AtomicInteger(0);
		gcmFailureCount = new AtomicInteger(0);
		apnsSuccessCount = new AtomicInteger(0);
		apnsFailureCount = new AtomicInteger(0);
		threadCount =new AtomicInteger(0);
//		totalFailureCount=new AtomicInteger(0);
	}

	@Override
	public Document toDocument() {
		Document doc = new Document();
		doc.put(F.APPLICATION_ID, this.appId);
		// doc.put(F.SERVICE_TYPE, this.serviceType);
		doc.put(F.ID, this.id);
		doc.put(F.TOTAL_COUNT, this.totalCount);
		doc.put(F.APNS_COUNT, this.apnsCount);
		doc.put(F.GCM_COUNT, this.gcmCount);
		doc.put(F.APNS_SUCCESS_COUNT, this.apnsSuccessCount.intValue());
		doc.put(F.GCM_SUCCESS_COUNT, this.gcmSuccessCount.intValue());
		doc.put(F.GCM_FAILURE_COUNT, this.gcmFailureCount.intValue());
		doc.put(F.APNS_FAILURE_COUNT, this.apnsFailureCount.intValue());
//		doc.put(F.TOTAL_FAILURE_COUNT, this.totalFailureCount.get());
		doc.put(F.START_TIME, this.startTime);
		doc.put(F.LAST_MODIFY, this.lastModify);
		doc.put(F.IS_DONE, this.isDone);
		return doc;
	}

	public static PushTaskBean fromDocument(Document doc) {
		PushTaskBean bean = new PushTaskBean();
		bean.setAppId(doc.getString(F.APPLICATION_ID));
		// bean.setServiceType(doc.getString(F.SERVICE_TYPE));
		bean.setId(doc.getString(F.ID));
		bean.setTotalCount(doc.getInteger(F.TOTAL_COUNT, 0));
		bean.setGcmCount(doc.getInteger(F.GCM_COUNT, 0));
		bean.setApnsCount(doc.getInteger(F.APNS_COUNT, 0));
		bean.setApnsSuccessCount(new AtomicInteger(doc.getInteger(F.APNS_SUCCESS_COUNT, 0)));
		bean.setGcmSuccessCount(new AtomicInteger(doc.getInteger(F.GCM_SUCCESS_COUNT, 0)));
		bean.setApnsFailureCount(new AtomicInteger(doc.getInteger(F.APNS_FAILURE_COUNT, 0)));
		bean.setGcmFailureCount(new AtomicInteger(doc.getInteger(F.GCM_FAILURE_COUNT, 0)));
//		bean.setTotalFailureCount(new AtomicInteger(doc.getInteger(F.TOTAL_FAILURE_COUNT, 0)));
		bean.setStartTime(doc.getLong(F.START_TIME));
		bean.setLastModify(doc.getLong(F.LAST_MODIFY));
		bean.setDone(doc.getBoolean(F.IS_DONE, false));
		return bean;
	}

	@Override
	public PuObject toPuObject() {
		PuObject puo = new PuObject();
		puo.set(F.ID, this.id);
		puo.set(F.APPLICATION_ID, this.appId);
		// puo.set(F.SERVICE_TYPE, this.serviceType);
		puo.set(F.TOTAL_COUNT, this.totalCount);
//		puo.set(F.TOTAL_FAILURE_COUNT, this.totalFailureCount);
		PuObject gcm=new PuObject();
		gcm.set(F.GCM_COUNT, this.gcmCount);
		gcm.set(F.GCM_SUCCESS_COUNT, this.gcmSuccessCount.get());
		gcm.set(F.GCM_FAILURE_COUNT, this.gcmFailureCount.get());
		puo.set(F.GCM, gcm);
		PuObject apns=new PuObject();
		apns.set(F.APNS_COUNT, this.apnsCount);
		apns.set(F.APNS_SUCCESS_COUNT, this.apnsSuccessCount.get());
		apns.set(F.APNS_FAILURE_COUNT, this.apnsFailureCount.get());
		puo.set(F.APNS, apns);
		puo.set(F.START_TIME, this.startTime);
		puo.set(F.LAST_MODIFY, this.lastModify);
		puo.set(F.IS_DONE, this.isDone);
		return puo;
	}

//	public AtomicInteger getTotalFailureCount() {
//		return totalFailureCount;
//	}
//
//	public void setTotalFailureCount(AtomicInteger totalFailureCount) {
//		this.totalFailureCount = totalFailureCount;
//	}

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

	public long getLastModify() {
		return lastModify;
	}

	public void setLastModify(long doneTime) {
		this.lastModify = doneTime;
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

	public AtomicInteger getGcmSuccessCount() {
		return gcmSuccessCount;
	}

	public void setGcmSuccessCount(AtomicInteger gcmSuccessCount) {
		this.gcmSuccessCount = gcmSuccessCount;
	}

	public AtomicInteger getApnsSuccessCount() {
		return apnsSuccessCount;
	}

	public void setApnsSuccessCount(AtomicInteger apnsSuccessCount) {
		this.apnsSuccessCount = apnsSuccessCount;
	}
	
	public void autoLastModify(){
		this.lastModify=System.currentTimeMillis()/1000;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public AtomicInteger getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(AtomicInteger threadCount) {
		this.threadCount = threadCount;
	}

	public AtomicInteger getGcmFailureCount() {
		return gcmFailureCount;
	}

	public void setGcmFailureCount(AtomicInteger gcmFailureCount) {
		this.gcmFailureCount = gcmFailureCount;
	}

	public AtomicInteger getApnsFailureCount() {
		return apnsFailureCount;
	}

	public void setApnsFailureCount(AtomicInteger apnsFailureCount) {
		this.apnsFailureCount = apnsFailureCount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void autoId(){
		this.id=UUID.randomUUID().toString();
	}
}
