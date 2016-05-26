package com.gaia.hermes2.service.sms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;

public class SmsContent {
	public static final int QC=1;
	public static final int CSKH=2;
	public static final int BRAND=3;
	private String message;
	private Set<String> recipients;
	private int smsType=QC;
	private String brandName;
	
	
	public PuObject toPuObject(){
		PuObject puo=new PuObject();
		try {
			puo.set(F.CONTENT, URLEncoder.encode(message, "utf8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			puo.set(F.CONTENT, this.message);
		}
		puo.set(F.SMS_TYPE, this.smsType);
		if(this.smsType==BRAND){
			puo.set(F.BRAND_NAME, this.brandName);
		}
		PuArray arr=new PuArrayList();
		for(String s:recipients){
			arr.addFrom(s);
		}
		puo.set(F.TO, arr);
		return puo;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Set<String> getRecipients() {
		return recipients;
	}
	public void setRecipients(Set<String> recipients) {
		this.recipients = recipients;
	}
	public int getSmsType() {
		return smsType;
	}
	public void setSmsType(int smsType) {
		this.smsType = smsType;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
		this.smsType=BRAND;
	}
	
	
}
