package com.gaia.hermes2.bean;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;

import com.gaia.hermes2.statics.DBF;
import com.nhb.common.db.beans.AbstractMongoBean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class PushSmsBean extends AbstractMongoBean {
	private static final long serialVersionUID = 6455281720535839182L;

	private String id;
	private String message;
	private Set<String> recipients;
	private String status;
	private String code;
	private long totalCount;
	private long totalPrice;
	private Set<String> invalidPhone;
	private boolean isDone;
	private long createdTime;

	@Override
	public Document toDocument() {
		Document doc = new Document();
		doc.put(DBF.ID, this.id);
		doc.put(DBF.MESSAGE, this.message);
		doc.put(DBF.RECIPIENTS, this.recipients);
		doc.put(DBF.CREATED_TIME, this.createdTime);
		doc.put(DBF.IS_DONE, this.isDone);
		doc.put(DBF.TOTAL_COUNT, this.totalCount);
		if (this.status != null) {
			doc.put(DBF.STATUS, this.status);
		}
		if (this.code != null) {
			doc.put(DBF.CODE, this.code);
		}
		if (this.totalPrice > 0) {
			doc.put(DBF.TOTAL_PRICE, this.totalPrice);
		}
		if (this.invalidPhone != null) {
			doc.put(DBF.INVALID_PHONE, this.invalidPhone);
		}
		return doc;
	}

	@SuppressWarnings("unchecked")
	public static PushSmsBean fromDocument(Document doc) {
		PushSmsBean bean = new PushSmsBean();
		bean.setObjectId(doc.getObjectId(DBF._ID));
		bean.setId(doc.getString(DBF.ID));
		bean.setMessage(doc.getString(DBF.MESSAGE));
		bean.setRecipients(doc.get(DBF.RECIPIENTS, HashSet.class));
		bean.setCreatedTime(doc.getLong(DBF.CREATED_TIME));
		bean.setDone(doc.getBoolean(DBF.IS_DONE, false));
		bean.setTotalCount(doc.getLong(DBF.TOTAL_COUNT));
		if (doc.containsKey(DBF.TOTAL_PRICE)) {
			bean.setTotalPrice(doc.getLong(DBF.TOTAL_PRICE));
		}
		if (doc.containsKey(DBF.INVALID_PHONE)) {
			bean.setInvalidPhone(doc.get(DBF.INVALID_PHONE, HashSet.class));
		}
		if (doc.containsKey(DBF.STATUS)) {
			bean.setStatus(doc.getString(DBF.STATUS));
		}
		if (doc.containsKey(DBF.CODE)) {
			bean.setCode(doc.getString(DBF.CODE));
		}

		return bean;
	}

	public void autoCreatedTime() {
		this.createdTime = System.currentTimeMillis() / 1000;
	}

}
