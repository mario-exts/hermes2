package com.gaia.hermes2.service.gcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.service.Hermes2AbstractPushNotificationService;
import com.gaia.hermes2.service.Hermes2Notification;
import com.gaia.hermes2.statics.F;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class Hermes2GCMService extends Hermes2AbstractPushNotificationService {

	private final String RETRIES = "gcm.retries";
	private final String BATCH_CHUNK_SIZE = "gcm.batchChunkSize";

	public static int DEFAULT_RETRIES = 1;

	private ExecutorService executor;

	private Sender client;
	private PuObjectRO clientConfig;
	private PuObjectRO applicationConfig;

	@Override
	public void init(PuObjectRO properties) {
		getLogger().debug("initializing {} with properties: {}", Hermes2GCMService.class.getName(), properties);
		this.clientConfig = properties.getPuObject(F.CLIENT_CONFIG, new PuObject());
		this.applicationConfig = properties.getPuObject(F.APPLICATION_CONFIG, new PuObject());

		this.client = new Sender(applicationConfig.getString(F.AUTHENTICATOR));
		this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
				.setNameFormat("Hermes2GCM " + applicationConfig.getString(F.ID) + " #%d").build());
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}

	private void _send(Message message, List<String> recipients) throws IOException {
		getLogger().debug("sending message {} to {} recipients", message, recipients.size());
		MulticastResult result = this.client.send(message, recipients,
				this.clientConfig.getInteger(RETRIES, DEFAULT_RETRIES));
		getLogger().debug("success: " + result.getSuccess() + ", failure: " + result.getFailure());
		
	}
	
	private void _send(Message message, List<String> recipients,PushTaskBean bean) throws IOException {
		getLogger().debug("sending message {} to {} recipients", message, recipients.size());
		MulticastResult result = this.client.send(message, recipients,
				this.clientConfig.getInteger(RETRIES, DEFAULT_RETRIES));
		getLogger().debug("success: " + result.getSuccess() + ", failure: " + result.getFailure());
		
		bean.getGcmSuccessCount().addAndGet(result.getSuccess());
		bean.getGcmFailureCount().addAndGet(result.getFailure());
		
	}

	@Override
	public void push(Hermes2Notification notification,PushTaskBean taskBean,PushTaskModel model) {
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

		CountDownLatch doneSignal = new CountDownLatch(partitionCount);
		String title = notification.getTitle();
		Builder messageBuilder = new Message.Builder().addData(F.MESSAGE, notification.getMessage());
		if (title != null) {
			messageBuilder.addData(F.TITLE, title);
		}
		final Message message = messageBuilder.build();
		
		
		for (List<String> recipients : batchs) {
			this.executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						Hermes2GCMService.this._send(message, recipients,taskBean);
					} catch (IOException e) {
						getLogger().error("Unable to send message: ", e);
					} finally {
						doneSignal.countDown();
					}
				}
			});
		}

		try {
			doneSignal.await();
			taskBean.autoLastModify();
			model.updateGcmPushCount(taskBean);
			getLogger().debug("update bean: "+taskBean.getGcmSuccessCount()+"  thread: "+taskBean.getThreadCount().intValue());
			if(taskBean.getThreadCount().decrementAndGet() <= 0){
				taskBean.setDone(true);
				model.doneTask(taskBean);
				getLogger().debug("done task.....................");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
