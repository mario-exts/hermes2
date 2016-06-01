package com.gaia.hermes2.service;

import java.io.Closeable;

import com.nhb.common.data.PuObjectRO;

import vn.speedsms.client.SmsEnvelop;

public interface Hermes2SmsService extends Closeable{
	
	void init(PuObjectRO properties);

	void setName(String name);

	String getName();
	
	void sendSms(SmsEnvelop content);
	
	void checkSendStatus(String transId);
	
	
	
}
