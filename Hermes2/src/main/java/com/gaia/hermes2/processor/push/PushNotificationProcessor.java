package com.gaia.hermes2.processor.push;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.gaia.hermes2.Hermes2PushHandler;
import com.gaia.hermes2.bean.DeviceTokenBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.model.impl.PushTaskReporter;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.service.BaseHermes2Notification;
import com.gaia.hermes2.service.Hermes2PushNotificationService;
import com.gaia.hermes2.statics.F;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mario.entity.MessageHandler;
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
			DeviceTokenModel deviceModel = pushHandler.getModelFactory()
						.getModel(DeviceTokenModel.class.toString());
			if(data.variableExists(F.SANDBOX) && data.getBoolean(F.SANDBOX)){
				deviceModel.setSandbox(true);
			}
			List<DeviceTokenBean> beans=null;
			
			String applicationId = data.getString(F.APPLICATION_ID);
			
			if (data.variableExists(F.TOKEN)) {
				String token= data.getString(F.TOKEN);
				beans = deviceModel.findByToken(token);
			}else{
				if (data.variableExists(F.SERVICE_TYPE)) {
					String serviceType=data.getString(F.SERVICE_TYPE);
					beans=deviceModel.findByAppIdAndServiceType(applicationId, serviceType);
				}else{
					beans=deviceModel.findByAppId(applicationId);
				}
			}

			Map<String, Collection<String>> targetDevicesByService = new HashMap<>();

			Map<String, Integer> countByService = new HashMap<>();
			for(DeviceTokenBean bean:beans) {
				String authenticatorId = bean.getAuthenticatorId();
				if (!targetDevicesByService.containsKey(authenticatorId)) {
					targetDevicesByService.put(authenticatorId, new HashSet<>());
				}
				targetDevicesByService.get(authenticatorId).add(bean.getToken());
				if (!countByService.containsKey(bean.getServiceType())) {
					countByService.put(bean.getServiceType(), 1);
				} else {
					countByService.put(bean.getServiceType(),
							countByService.get(bean.getServiceType()) + 1);
				}

			}
			PushTaskModel model = pushHandler.getModelFactory().getModel(PushTaskModel.class.toString());
			PushTaskReporter taskReporter = new PushTaskReporter(model);

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
					// bean.getTotalFailureCount().addAndGet(entry.getValue().size());
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
