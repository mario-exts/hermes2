package com.gaia.hermes2.service;

import java.io.Closeable;

import com.gaia.hermes2.service.sms.SmsContent;
import com.nhb.common.data.PuObjectRO;

public interface Hermes2SmsService extends Closeable{
	
	void init(PuObjectRO properties);

	void setName(String name);

	String getName();
	
	void sendSms(SmsContent content);
	
	void checkSendStatus(String transId);
	
	
	
}
