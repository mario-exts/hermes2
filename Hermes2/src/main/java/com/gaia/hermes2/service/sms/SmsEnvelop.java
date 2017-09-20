package com.gaia.hermes2.service.sms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;

import vn.speedsms.client.enums.SpeedSMSType;

public class SmsEnvelop {
	private String content;
	private Set<String> recipients;
	private SpeedSMSType smsType=SpeedSMSType.ADVERTISEMENT;
	private String brandName;
	
	
	public PuObject toPuObject(){
		PuObject puo=new PuObject();
		try {
			puo.set(F.CONTENT, URLEncoder.encode(content, "utf8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			puo.set(F.CONTENT, this.content);
		}
		puo.set(F.SMS_TYPE, this.smsType);
		if(this.smsType==SpeedSMSType.BRAND_NAME){
			puo.set(F.BRAND_NAME, this.brandName);
		}
		PuArray arr=new PuArrayList();
		for(String s:recipients){
			arr.addFrom(s);
		}
		puo.set(F.TO, arr);
		return puo;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Set<String> getRecipients() {
		return recipients;
	}
	public void setRecipients(Set<String> recipients) {
		this.recipients = recipients;
	}
	
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public SpeedSMSType getSmsType() {
		return smsType;
	}

	public void setSmsType(SpeedSMSType smsType) {
		this.smsType = smsType;
	}
	
}
