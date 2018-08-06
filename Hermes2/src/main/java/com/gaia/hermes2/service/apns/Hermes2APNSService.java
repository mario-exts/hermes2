package com.gaia.hermes2.service.apns;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.gaia.hermes2.processor.PushTaskReporter;
import com.gaia.hermes2.service.Hermes2AbstractPushNotificationService;
import com.gaia.hermes2.service.Hermes2Notification;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.FileSystemUtils;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;

import io.netty.util.concurrent.GenericFutureListener;

public class Hermes2APNSService extends Hermes2AbstractPushNotificationService {

	private static final String APNS_ENTRUST_ROOT_CA = "apns.entrust.root.ca";
	private static final String APNS_HOST = "apns.host";
	private static final String APNS_PORT = "apns.port";

	private static final String APNS_HOST_SANDBOX = "apns.sandbox.host";
	private static final String APNS_PORT_SANDBOX = "apns.sandbox.port";

	private ApnsClient apnsClient;
	private String topic;

	@Override
	public void init(PuObjectRO initParams) {
		PuObject apnsConfig = initParams.getPuObject(F.CLIENT_CONFIG, new PuObject());
		PuObject pushConfig = initParams.getPuObject(F.APPLICATION_CONFIG, new PuObject());

		try {
			this.apnsClient = initApnsClient(apnsConfig, pushConfig);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create new apns client", e);
		}

		this.topic = pushConfig.getString(F.TOPIC, pushConfig.getString(F.BUNDLE_ID, null));
	}

	private ApnsClient initApnsClient(PuObject apnsConfig, PuObject pushConfig) throws Exception {
		ApnsClientBuilder builder = new ApnsClientBuilder();

		byte[] p12 = pushConfig.getRaw(F.AUTHENTICATOR, null);
		if (p12 == null) {
			throw new NullPointerException("Cannot init apns client without p12 info");
		} else {
			try (InputStream p12InputStream = new ByteArrayInputStream(p12)) {
				String password = pushConfig.getString(F.PASSWORD, "");
				builder.setClientCredentials(p12InputStream, password);
			}
		}

		String hostname = null;
		int port = -1;

		boolean sandbox = pushConfig.getBoolean(F.SANDBOX, false);
		if (sandbox) {
			hostname = apnsConfig.getString(APNS_HOST_SANDBOX, ApnsClientBuilder.DEVELOPMENT_APNS_HOST);
			port = apnsConfig.getInteger(APNS_PORT_SANDBOX, -1);
		} else {
			hostname = apnsConfig.getString(APNS_HOST, ApnsClientBuilder.PRODUCTION_APNS_HOST);
			port = apnsConfig.getInteger(APNS_PORT, -1);
		}

		if (port == -1) {
			builder.setApnsServer(hostname);
		} else {
			builder.setApnsServer(hostname, port);
		}

		String entrustRootCAFilePath = apnsConfig.getString(APNS_ENTRUST_ROOT_CA, null);
		if (entrustRootCAFilePath != null) {
			String basePath = FileSystemUtils.getBasePathForClass(this.getClass());
			builder.setTrustedServerCertificateChain(new File(basePath + entrustRootCAFilePath));
		}

		return builder.build();
	}

	@Override
	public void close() throws IOException {
		this.apnsClient.close();
	}

	@Override
	public void push(Hermes2Notification notification, PushTaskReporter taskReporter) {

		try {
			final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
			payloadBuilder.setAlertBody(notification.getMessage());

			if (notification.getBadge() > 0) {
				payloadBuilder.setBadgeNumber(notification.getBadge());
			}

			final String payload = payloadBuilder.buildWithDefaultMaximumLength();
			final String[] recipients = notification.getRecipients()
					.toArray(new String[notification.getRecipients().size()]);

			final AtomicBoolean execReportFlag = new AtomicBoolean(false);
			final AtomicInteger successCount = new AtomicInteger(0);
			final AtomicInteger failureCount = new AtomicInteger(0);
			final List<String> failureTokens = new ArrayList<>();

			final GenericFutureListener<PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>> listener = new GenericFutureListener<PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>>() {

				@Override
				public void operationComplete(
						PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> future)
						throws Exception {
					boolean success = false;
					try {
						PushNotificationResponse<SimpleApnsPushNotification> response = future.get();
						getLogger().debug("Got response from APNS: " + response.getPushNotification().getToken() + "  "
								+ response.getRejectionReason() + "  " + response.getPushNotification().getTopic());
						success = response.isAccepted();
						if (!success) {
							getLogger().debug("APNS get response failed, resion: {}", response.getRejectionReason());
							if (response.getRejectionReason().equalsIgnoreCase("Unregistered")
									|| response.getRejectionReason().equalsIgnoreCase("BadDeviceToken")) {
								failureTokens.add(response.getPushNotification().getToken());
							}
						}
					} catch (InterruptedException | ExecutionException e) {
						getLogger().error("Error while trying to get response", e);
					}

					if (success) {
						successCount.incrementAndGet();
					} else {
						failureCount.incrementAndGet();
					}

					int doneCount = successCount.get() + failureCount.get();
					if (doneCount == recipients.length && taskReporter != null) {
						if (execReportFlag.compareAndSet(false, true)) {
							getLogger().debug("ApnsPush get {} success, {} failure at thread {}", successCount.get(),
									failureCount.get());

							taskReporter.increaseApnsCount(successCount.get(), failureCount.get());

							if (taskReporter.decrementSubTaskCount() == 0) {
								getLogger().debug("Hermes2Push is done .....................");
							}

							int removedCount = taskReporter.removeTokens(failureTokens);
							if (removedCount > 0) {
								getLogger().debug("Trying to remove {} Unregisted tokens, success is {}",
										failureTokens.size(), removedCount);
							}
						}
					}
				}
			};

			for (int i = 0; i < recipients.length; i++) {
				this.apnsClient.sendNotification(new SimpleApnsPushNotification(recipients[i], topic, payload))
						.addListener(listener);
			}

			getLogger().debug("DONE push APNs");
		} catch (Exception e) {
			getLogger().error("Error APNs push", e);
			if (taskReporter != null && taskReporter.decrementSubTaskCount() == 0) {
				getLogger().debug("Hermes2Push is done .....................");
			}
		}
	}

}
