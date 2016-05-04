package com.gaia.hermes2.service.apns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.service.Hermes2AbstractPushNotificationService;
import com.gaia.hermes2.service.Hermes2Notification;
import com.gaia.hermes2.statics.F;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;

import io.netty.channel.nio.NioEventLoopGroup;

public class Hermes2APNSService extends Hermes2AbstractPushNotificationService {

	public static final int DEFAULT_LOOP_GROUP_NUM_THREADS = 4;
	public static final int DEFAULT_MAX_CONNECTION = 10;
	public static final int DEFAULT_MAX_IDLE = 5;
	public static final long DEFAULT_SLEEP_ON_ERROR_MS = 1000; // ms

	static final String APNS_HOST = "apns.host";
	static final String APNS_PORT = "apns.port";
	static final String APNS_HOST_SANDBOX = "apns.sandbox.host";
	static final String APNS_PORT_SANDBOX = "apns.sandbox.port";

	static final String APNS_MAX_IDLE = "apns.client.max.idle";
	static final String APNS_LOOP_GROUP_NUM_THREADS = "apns.client.nthreads";

	static final String APNS_MAX_CONNECTION = "apns.client.max.connection";
	static final String APNS_MIN_MESSAGE_PER_CONNECTION = "apns.client.minimum.message.per.connection";

	private PuObject clientConfig;
	private PuObject applicationConfig;
	private NioEventLoopGroup nioEventLoopGroup;

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private ApnsClientPool apnsClientPool;

	@Override
	public void init(PuObjectRO initParams) {
//		getLogger().debug("Initializing {} with properties: {}", Hermes2APNSService.class.getName(), initParams);
		this.clientConfig = initParams.getPuObject(F.CLIENT_CONFIG);
		this.applicationConfig = initParams.getPuObject(F.APPLICATION_CONFIG);
		this.nioEventLoopGroup = new NioEventLoopGroup(
				clientConfig.getInteger(APNS_LOOP_GROUP_NUM_THREADS, DEFAULT_LOOP_GROUP_NUM_THREADS),
				new ThreadFactoryBuilder().setNameFormat("Hermes2APNS " + applicationConfig.getString(F.ID) + " #%d")
						.build());

		this.apnsClientPool = new ApnsClientPool(clientConfig, applicationConfig, nioEventLoopGroup);

	}

	@Override
	public void close() throws IOException {
		try {
			this.nioEventLoopGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.apnsClientPool.close();

		this.executor.shutdown();
		try {
			if (this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
				this.executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void push(Hermes2Notification notification,PushTaskBean bean,PushTaskModel model) {

		final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
		payloadBuilder.setAlertBody(notification.getMessage());
		if (notification.getBadge() > 0) {
			payloadBuilder.setBadgeNumber(notification.getBadge());
		}
		final String payload = payloadBuilder.buildWithDefaultMaximumLength();
		final String[] recipients = notification.getRecipients()
				.toArray(new String[notification.getRecipients().size()]);
		int minMessagePerConnection = this.clientConfig.getInteger(APNS_MIN_MESSAGE_PER_CONNECTION);

		int numClients = Double.valueOf(Math.ceil(Double.valueOf(recipients.length) / minMessagePerConnection))
				.intValue();
		numClients = Math.min(numClients, this.clientConfig.getInteger(APNS_MAX_CONNECTION, DEFAULT_MAX_CONNECTION));

		getLogger().debug("Calculated number clients to be borrow: {}", numClients);

		final List<ApnsClient<NotificationItem>> clients = new ArrayList<>();

		for (int i = 0; i < numClients; i++) {
			try {
				clients.add(this.apnsClientPool.borrowObject());
			} catch (Exception e) {
				e.printStackTrace();
//				bean.getTotalFailureCount().addAndGet(numClients);
//				break;
			}
		}

		getLogger().debug("Did borrow {} clients", clients.size());

		final int numMessagePerClient = Double
				.valueOf(Math.ceil(Double.valueOf(recipients.length) / Double.valueOf(clients.size()))).intValue();

		getLogger().debug("will send {} message(s) per client", numMessagePerClient);
		final String topic = this.applicationConfig.getString(F.TOPIC, null);
		CountDownLatch countDown=new CountDownLatch(clients.size());
		for (int i = 0; i < clients.size(); i++) {
			final int index = i;
			this.executor.submit(new Runnable() {

				private final int clientId = index;
				private final ApnsClient<NotificationItem> client = clients.get(this.clientId);

				@Override
				public void run() {
					try {
						
						int startId = clientId * numMessagePerClient;
						int endId = startId + numMessagePerClient;
						int successCount=0;
						int failureCount=0;
						getLogger().debug("start sending from token {} to {}", startId, endId);
						
						for (int i = startId; i < endId; i++) {
							NotificationItem notificationItem = new NotificationItem(recipients[i], payload, topic);
							Future<PushNotificationResponse<NotificationItem>> future=this.client.sendNotification(notificationItem);
							if(future.get().isAccepted()){
								successCount++;
							}else{
								failureCount++;
							}
						}
						bean.getApnsSuccessCount().addAndGet(successCount);
						bean.getApnsFailureCount().addAndGet(failureCount);
						bean.autoLastModify();
						model.updateApnsPushCount(bean);
//						int i=bean.getThreadCount().decrementAndGet();
//						getLogger().debug("thread 1 : "+i);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						countDown.countDown();
						apnsClientPool.returnObject(this.client);
						
					}
				}
			});
		}
		
		try{
			countDown.await();
			int i=bean.getThreadCount().decrementAndGet();
			getLogger().debug("thread: "+i);
			if(i<=0){
				bean.setDone(true);
//				bean.getTotalFailureCount()
//				.addAndGet(bean.getApnsFailureCount().get() + bean.getGcmFailureCount().get());
				model.doneTask(bean);
				getLogger().debug("done task.....................");
			}
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}

}
