package com.gaia.hermes2.service.gcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gaia.hermes2.model.PushTaskReporter;
import com.gaia.hermes2.service.Hermes2AbstractPushNotificationService;
import com.gaia.hermes2.service.Hermes2Notification;
import com.gaia.hermes2.statics.F;
import com.google.android.gcm.server.AsyncSender;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class Hermes2GCMService extends Hermes2AbstractPushNotificationService {

	private final String RETRIES = "gcm.retries";
	private final String BATCH_CHUNK_SIZE = "gcm.batchChunkSize";

	public static int DEFAULT_RETRIES = 1;

	private ExecutorService executor;
	private PuObjectRO clientConfig;
	private PuObjectRO applicationConfig;
	private AsyncSender asyncClient;

	@Override
	public void init(PuObjectRO properties) {
		// getLogger().debug("initializing {} with properties: {}",
		// Hermes2GCMService.class.getName(), properties);
		this.clientConfig = properties.getPuObject(F.CLIENT_CONFIG, new PuObject());
		this.applicationConfig = properties.getPuObject(F.APPLICATION_CONFIG, new PuObject());
		this.asyncClient = new AsyncSender(applicationConfig.getString(F.AUTHENTICATOR));
		this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
				.setNameFormat("Hermes2GCM " + applicationConfig.getString(F.ID) + " #%d").build());
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}

	private void asyncSend(Message message, List<String> recipients, PushTaskReporter taskReporter)
			throws IOException {
		getLogger().debug("sending message {} to {} recipients", message, recipients.size());
		Callback<MulticastResult> callback = new Callback<MulticastResult>() {

			@Override
			public void apply(MulticastResult result) {

				getLogger().debug("success: " + result.getSuccess() + ", failure: " + result.getFailure() + "  thread: "
						+ taskReporter.getTask().getThreadCount().get());
				taskReporter.getTask().getGcmSuccessCount().addAndGet(result.getSuccess());
				taskReporter.getTask().getGcmFailureCount().addAndGet(result.getFailure());
				taskReporter.getTask().autoLastModify();
				taskReporter.updateGcm();
				if (taskReporter.getTask().getThreadCount().decrementAndGet() < 1) {
					taskReporter.getTask().setDone(true);
//					taskReporter.getTask().getTotalFailureCount()
//							.addAndGet(taskReporter.getTask().getApnsFailureCount().get() + taskReporter.getTask().getGcmFailureCount().get());
					taskReporter.doneTaske();
					getLogger().debug("done task..................... ");
				}
			}
		};

		this.asyncClient.send(message, recipients, this.clientConfig.getInteger(RETRIES, DEFAULT_RETRIES), callback);

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
		String title = notification.getTitle();
		Builder messageBuilder = new Message.Builder().addData(F.MESSAGE, notification.getMessage());
		if (title != null) {
			messageBuilder.addData(F.TITLE, title);
		}
		String messgeId = notification.getMessageId();
		if (messgeId != null) {
			messageBuilder.addData(F.MESSAGE_ID, messgeId);
		}

		final Message message = messageBuilder.build();
		if (batchs.size() == 0) {
			taskReporter.getTask().getThreadCount().decrementAndGet();
		}
		for (List<String> recipients : batchs) {
			this.executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						Hermes2GCMService.this.asyncSend(message, recipients, taskReporter);
					} catch (IOException e) {
						taskReporter.getTask().getThreadCount().decrementAndGet();
						getLogger().error("Unable to send message: ", e);
					}
				}
			});
		}
	}

}
