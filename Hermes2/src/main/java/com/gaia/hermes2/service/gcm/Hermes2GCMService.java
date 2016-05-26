package com.gaia.hermes2.service.gcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gaia.hermes2.processor.PushTaskReporter;
import com.gaia.hermes2.service.Hermes2AbstractPushNotificationService;
import com.gaia.hermes2.service.Hermes2Notification;
import com.gaia.hermes2.statics.F;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class Hermes2GCMService extends Hermes2AbstractPushNotificationService {

	private final String BATCH_CHUNK_SIZE = "gcm.batchChunkSize";

	public static int DEFAULT_RETRIES = 1;

	private ExecutorService executor;
	private PuObjectRO clientConfig;
	private PuObjectRO applicationConfig;
	private GCMAsyncSender sender;

	@Override
	public void init(PuObjectRO properties) {
		this.clientConfig = properties.getPuObject(F.CLIENT_CONFIG, new PuObject());
		this.applicationConfig = properties.getPuObject(F.APPLICATION_CONFIG, new PuObject());
		this.sender = new GCMAsyncSender(applicationConfig.getString(F.AUTHENTICATOR));
		this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
				.setNameFormat("Hermes2GCM " + applicationConfig.getString(F.ID) + " #%d").build());
	}

	@Override
	public void close() throws IOException {
		this.sender.close();
	}

	private void asyncSend(Message message, List<String> recipients, PushTaskReporter taskReporter) {
		getLogger().debug("sending message {} recipients", recipients.size());

		Callback<MulticastResult> callback = new Callback<MulticastResult>() {

			@Override
			public void apply(final MulticastResult result) {

				// delegate result handling to another thread
				executor.submit(new Runnable() {
					public void run() {
						getLogger().debug("GCM push is complete, success: " + result.getSuccess() + ", failure: "
								+ result.getFailure() + ", remaining thread: " + taskReporter.getThreadCount());

						taskReporter.increaseGcmCount(result.getSuccess(), result.getFailure());

						if (taskReporter.decrementSubTaskCount() == 0) {
							getLogger().debug("Hermes2Push is done..................... ");
						}
						// TODO Remove error tokens
					}
				});
			}
		};

		Callback<Throwable> failureCallback = new Callback<Throwable>() {

			@Override
			public void apply(Throwable cause) {
				getLogger().error("An error occur when sending message: ", cause);
				executor.submit(new Runnable() {

					@Override
					public void run() {
						if (taskReporter.decrementSubTaskCount() == 0) {
							getLogger().debug("Hermes2Push is done..................... ");
						}
					}
				});
			}
		};

		this.sender.send(message, recipients, callback, failureCallback);
	}

	@Override
	public void push(Hermes2Notification notification, PushTaskReporter taskReporter) {
		List<List<String>> batchs = new ArrayList<>();

		int partitionCount = notification.getRecipients().size() / this.clientConfig.getInteger(BATCH_CHUNK_SIZE);
		partitionCount = partitionCount == 0 ? 1 : partitionCount;

		for (int i = 0; i < partitionCount; i++) {
			batchs.add(new ArrayList<String>());
		}

		int index = 0;
		for (String recipient : notification.getRecipients()) {
			batchs.get(index++ % partitionCount).add(recipient);
		}

		Builder messageBuilder = new Message.Builder().addData(F.MESSAGE, notification.getMessage());

		String title = notification.getTitle();
		if (title != null) {
			messageBuilder.addData(F.TITLE, title);
		}

		if (notification.getBadge() > 0) {
			messageBuilder.addData(F.BADGE, String.valueOf(notification.getBadge()));
		}

		String messgeId = notification.getMessageId();
		if (messgeId != null) {
			messageBuilder.addData(F.MESSAGE_ID, messgeId);
		}

		if (batchs.size() == 0) {
			taskReporter.decrementSubTaskCount();
		} else {
			taskReporter.addAndGetSubTaskCount(batchs.size() - 1);
			Message message = messageBuilder.build();
			for (List<String> recipients : batchs) {
				Hermes2GCMService.this.asyncSend(message, recipients, taskReporter);
			}
		}
	}

}
