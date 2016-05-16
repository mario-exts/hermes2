package com.gaia.hermes2.service.sms;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gaia.hermes2.processor.PushTaskReporter;
import com.gaia.hermes2.service.Hermes2AbstractPushNotificationService;
import com.gaia.hermes2.service.Hermes2Notification;
import com.gaia.hermes2.service.gcm.Hermes2GCMService;
import com.gaia.hermes2.statics.F;
import com.google.android.gcm.server.AsyncSender;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class Hermes2SmsSservice extends Hermes2AbstractPushNotificationService{
	private ExecutorService executor;
	private PuObjectRO clientConfig;
	private PuObjectRO applicationConfig;
	private AsyncSmsSender asyncClient;

	@Override
	public void init(PuObjectRO properties) {
		this.clientConfig = properties.getPuObject(F.CLIENT_CONFIG, new PuObject());
		this.applicationConfig = properties.getPuObject(F.APPLICATION_CONFIG, new PuObject());
		this.asyncClient = new AsyncSmsSender(applicationConfig.getString(F.AUTHENTICATOR));
		this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
				.setNameFormat("Hermes2GCM " + applicationConfig.getString(F.ID) + " #%d").build());
	}
	@Override
	public void push(Hermes2Notification notification, PushTaskReporter taskReporter) {

		this.executor.submit(new Runnable() {

			@Override
			public void run() {
//				try {
//					asyncClient.sendSMS(message, recipients, 1,taskReporter);
//				} catch (IOException e) {
//					taskReporter.getThreadCount().decrementAndGet();
//					getLogger().error("Unable to send message: ", e);
//				}
			}
		});
		
	}
	
	private void doSendSms(String message,List<String> recipients,PushTaskReporter taskReporter){
		
	}
	
	

}
