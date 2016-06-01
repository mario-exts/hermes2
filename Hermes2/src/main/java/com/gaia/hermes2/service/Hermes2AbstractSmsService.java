package com.gaia.hermes2.service;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObjectRO;

import vn.speedsms.client.SmsEnvelop;

public abstract class Hermes2AbstractSmsService extends BaseLoggable implements Hermes2SmsService{
	private String name;

	@Override
	public void init(PuObjectRO properties) {
		
	}

	@Override
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public String getName() {
		return this.name;
	}


	@Override
	public void sendSms(SmsEnvelop content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkSendStatus(String transId) {
		// TODO Auto-generated method stub
		
	}
	
	
}
