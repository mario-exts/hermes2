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
import com.google.android.gcm.server.AsyncSender;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
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
//		getLogger().debug("initializing {} with properties: {}", Hermes2GCMService.class.getName(), properties);
		this.clientConfig = properties.getPuObject(F.CLIENT_CONFIG, new PuObject());
		this.applicationConfig = properties.getPuObject(F.APPLICATION_CONFIG, new PuObject());
		this.asyncClient=new AsyncSender(applicationConfig.getString(F.AUTHENTICATOR));
		this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
				.setNameFormat("Hermes2GCM " + applicationConfig.getString(F.ID) + " #%d").build());
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}
	
	private void asyncSend(Message message, List<String> recipients,PushTaskBean bean,PushTaskModel model) throws IOException {
		getLogger().debug("sending message {} to {} recipients", message, recipients.size());
		Callback<MulticastResult> callback=new Callback<MulticastResult>() {

			@Override
			public void apply(MulticastResult result) {
				getLogger().debug("success: " + result.getSuccess() + ", failure: " + result.getFailure()+"  thread: "+bean.getThreadCount().get());
				bean.getGcmSuccessCount().addAndGet(result.getSuccess());
				bean.getGcmFailureCount().addAndGet(result.getFailure());
				bean.autoLastModify();
				model.updateGcmPushCount(bean);
				if(bean.getThreadCount().decrementAndGet() < 1){					
					bean.setDone(true);
					model.doneTask(bean);
					getLogger().debug("done task.....................");
				}
			}
		};
		
		this.asyncClient.send(message, recipients,
				this.clientConfig.getInteger(RETRIES, DEFAULT_RETRIES)
				,callback);
		
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
		String title = notification.getTitle();
		Builder messageBuilder = new Message.Builder().addData(F.MESSAGE, notification.getMessage());
		if (title != null) {
			messageBuilder.addData(F.TITLE, title);
		}
		String messgeId=notification.getMessageId();
		if(messgeId!=null){
			messageBuilder.addData(F.MESSAGE_ID, messgeId);
		}
		
		final Message message = messageBuilder.build();
		if(batchs.size()==0){
			taskBean.getThreadCount().decrementAndGet();
		}
		for (List<String> recipients : batchs) {
			this.executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						Hermes2GCMService.this.asyncSend(message, recipients,taskBean,model);
					} catch (IOException e) {
						taskBean.getThreadCount().decrementAndGet();
						getLogger().error("Unable to send message: ", e);
					}
				}
			});
		}
	}

}
