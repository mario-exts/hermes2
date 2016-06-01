package com.gaia.hermes2.service.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.gaia.hermes2.service.Hermes2AbstractSmsService;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

import vn.speedsms.client.SpeedSMSClient;
import vn.speedsms.client.SpeedSMSSendingFuture;
import vn.speedsms.client.SpeedSMSSendingResponse;

public class Hermes2SmsServiceImpl extends Hermes2AbstractSmsService {
	// private ExecutorService executor;
	// private PuObjectRO clientConfig;
	private PuObjectRO applicationConfig;
	// private SmsAsyncSender asyncSender;
	private SpeedSMSClient smsClient;

	@Override
	public void init(PuObjectRO properties) {
		this.applicationConfig = properties.getPuObject(F.APPLICATION_CONFIG, new PuObject());
		getLogger().debug("appconfig: " + applicationConfig);
		smsClient = new SpeedSMSClient(applicationConfig.getString(F.ACCESS_TOKEN));
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public void sendSms(SmsEnvelop envelop) {
		ArrayList<String> recipients=new ArrayList<>(envelop.getRecipients());
		SpeedSMSSendingFuture future = smsClient.send(envelop.getContent(), recipients,
				envelop.getSmsType(), envelop.getBrandName());
		try {
			SpeedSMSSendingResponse response = future.get();
			getLogger().debug("send sms response: " + response.getStatus());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void checkSendStatus(String transId) {

	}

}
