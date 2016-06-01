package com.gaia.hermes2.service.sms;

import java.io.IOException;

import com.gaia.hermes2.service.Hermes2AbstractSmsService;
import com.gaia.hermes2.statics.F;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class Hermes2SmsServiceImpl extends Hermes2AbstractSmsService {
	// private ExecutorService executor;
	// private PuObjectRO clientConfig;

	private PuObjectRO applicationConfig;
	private SmsAsyncSender asyncSender;

	@Override
	public void init(PuObjectRO properties) {
		// this.clientConfig = properties.getPuObject(F.CLIENT_CONFIG, new
		// PuObject());
		this.applicationConfig = properties.getPuObject(F.APPLICATION_CONFIG, new PuObject());
		getLogger().debug("appconfig: " + applicationConfig);
		this.asyncSender = new SmsAsyncSender(applicationConfig.getString(F.ACCESS_TOKEN));
		// this.executor = Executors.newCachedThreadPool(new
		// ThreadFactoryBuilder()
		// .setNameFormat("Hermes2GCM " + applicationConfig.getString(F.ID) + "
		// #%d").build());
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendSms(SmsContent content) {
		Callback<SendResult> successCallback = new Callback<SendResult>() {

			@Override
			public void apply(SendResult result) {
				getLogger().debug("Sending SMS success: " + result.getMessage());

			}

		};
		Callback<Throwable> failureCallback = new Callback<Throwable>() {

			@Override
			public void apply(Throwable cause) {
				getLogger().error("An error occur when sending SMS: ", cause);
				// executor.submit(new Runnable() {
				//
				// @Override
				// public void run() {
				// }
				// });
			}
		};
		asyncSender.sendSMS(content, successCallback, failureCallback);

	}

	@Override
	public void checkSendStatus(String transId) {
		// TODO Auto-generated method stub

	}

}
