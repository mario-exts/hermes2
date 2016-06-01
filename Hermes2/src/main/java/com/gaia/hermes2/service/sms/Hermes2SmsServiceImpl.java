package com.gaia.hermes2.service.sms;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.gaia.hermes2.service.Hermes2AbstractSmsService;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

import vn.speedsms.client.SmsEnvelop;
import vn.speedsms.client.SpeedSMSClient;
import vn.speedsms.client.SpeedSMSSendingFuture;
import vn.speedsms.client.SpeedSMSSendingResponse;

public class Hermes2SmsServiceImpl extends Hermes2AbstractSmsService {
//	private ExecutorService executor;
//	private PuObjectRO clientConfig;
	private PuObjectRO applicationConfig;
//	private SmsAsyncSender asyncSender;
	private SpeedSMSClient smsClient;

	@Override
	public void init(PuObjectRO properties) {
//		this.clientConfig = properties.getPuObject(F.CLIENT_CONFIG, new PuObject());
		this.applicationConfig = properties.getPuObject(F.APPLICATION_CONFIG, new PuObject());
		getLogger().debug("appconfig: "+applicationConfig);
//		this.asyncSender = new SmsAsyncSender(applicationConfig.getString(F.ACCESS_TOKEN));
//		this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
//				.setNameFormat("Hermes2GCM " + applicationConfig.getString(F.ID) + " #%d").build());
		smsClient=new SpeedSMSClient(applicationConfig.getString(F.ACCESS_TOKEN));
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendSms(SmsEnvelop envelop) {
		SpeedSMSSendingFuture future=smsClient.send(envelop);
		try {
			SpeedSMSSendingResponse response = future.get();
			getLogger().debug("send sms response: "+response.getStatus());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void checkSendStatus(String transId) {
		// TODO Auto-generated method stub
		
	}

}
