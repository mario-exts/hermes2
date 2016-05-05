package com.gaia.hermes2.processor.push;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.Document;

import com.gaia.hermes2.Hermes2PushHandler;
import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.model.PushTaskReporter;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.service.BaseHermes2Notification;
import com.gaia.hermes2.service.Hermes2PushNotificationService;
import com.gaia.hermes2.statics.F;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mario.entity.MessageHandler;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class PushNotificationProcessor extends Hermes2BaseProcessor {

	private ExecutorService executor;
	private AtomicInteger counter = new AtomicInteger(0);

	@Override
	protected PuElement process(MessageHandler handler, PuObjectRO data) {
		if (handler instanceof Hermes2PushHandler) {
			Hermes2PushHandler pushHandler = (Hermes2PushHandler) handler;
			MongoDatabase database = pushHandler.getDatabase();
			MongoCollection<Document> collection = data.getBoolean(F.SANDBOX, false)
					? database.getCollection(F.DATABASE_DEVICE_TOKEN_SANDBOX)
					: database.getCollection(F.DATABASE_DEVICE_TOKEN);

			String applicationId = data.getString(F.APPLICATION_ID);
			Document criteria = new Document();
			criteria.append(F.APPLICATION_ID, applicationId);
			if (data.variableExists(F.SERVICE_TYPE)) {
				criteria.append(F.SERVICE_TYPE, data.getString(F.SERVICE_TYPE));
			}
			if (data.variableExists(F.TOKEN)) {
				criteria.append(F.ID, data.getString(F.TOKEN));
			}

			FindIterable<Document> cursor = collection.find(criteria);
			MongoCursor<Document> it = cursor.iterator();
			
//			for(Document doc:cursor){
//				getLogger().debug("iterator:  " + doc.toJson());
//			}
			Map<String, Collection<String>> targetDevicesByService = new HashMap<>();
			// Map<String, PushTaskBean> tasks = new HashMap<>();

			Map<String, Integer> countByService = new HashMap<>();
			while (it.hasNext()) {
				Document row = it.next();
				String authenticatorId = row.getString(F.AUTHENTICATOR_ID);
				if (!targetDevicesByService.containsKey(authenticatorId)) {
					targetDevicesByService.put(authenticatorId, new HashSet<>());
				}
				targetDevicesByService.get(authenticatorId).add(row.getString(F.TOKEN));
				if (!countByService.containsKey(row.getString(F.SERVICE_TYPE))) {
					countByService.put(row.getString(F.SERVICE_TYPE), 1);
				} else {
					countByService.put(row.getString(F.SERVICE_TYPE),
							countByService.get(row.getString(F.SERVICE_TYPE)) + 1);
				}

			}
//			getLogger().debug("taskbean:  " + targetDevicesByService.size());
			PushTaskReporter taskReporter=new PushTaskReporter((Hermes2PushHandler)handler);
			
			taskReporter.getTask().setAppId(applicationId);
			taskReporter.getTask().autoStartTime();
			int gcmCount = 0;
			int apnsCount = 0;
			if (countByService.containsKey("gcm")) {
				gcmCount = countByService.get("gcm");
			}
			if (countByService.containsKey("apns")) {
				apnsCount = countByService.get("apns");
			}
			taskReporter.getTask().autoId();
			taskReporter.getTask().setTotalCount(gcmCount + apnsCount);
			taskReporter.getTask().setApnsCount(apnsCount);
			taskReporter.getTask().setGcmCount(gcmCount);
			// bean.setLastModify(bean.getStartTime());
			taskReporter.getTask().getThreadCount().addAndGet(targetDevicesByService.size());
			taskReporter.saveTask();

			String message = data.getString(F.MESSAGE);
			String messageId;
			if (data.variableExists(F.MESSAGE_ID)) {
				messageId = data.getString(F.MESSAGE_ID, "1");
			} else {
				messageId = counter.incrementAndGet() + "";
				if (counter.get() > 20000) {
					counter.set(0);
				}
			}

			if (this.executor == null) {
				this.executor = Executors.newCachedThreadPool(
						new ThreadFactoryBuilder().setNameFormat("Hermes2PushProcessor " + " #%d").build());
			}

			targetDevicesByService.entrySet().forEach(entry -> {
				Hermes2PushNotificationService service = pushHandler.getPushService(entry.getKey());
				if (service != null) {
					this.executor.execute(new Runnable() {
						@Override
						public void run() {
							service.push(new BaseHermes2Notification(entry.getValue(), message,
									data.getString(F.TITLE, null), messageId), taskReporter);
						}
					});
				} else {
					taskReporter.getTask().getThreadCount().decrementAndGet();
//					bean.getTotalFailureCount().addAndGet(entry.getValue().size());
					getLogger().warn("Unable to get notification service for authenticator id " + entry.getKey());
				}

			});

			PuObject result = PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.TARGETS, countByService));
			result.set(F.ID, taskReporter.getTask().getId());
			return result;
		}
		return null;
	}
}
