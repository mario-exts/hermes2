package com.gaia.hermes2.processor.push;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.gaia.hermes2.bean.DeviceTokenBean;
import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.PushTaskReporter;
import com.gaia.hermes2.service.BaseHermes2Notification;
import com.gaia.hermes2.service.Hermes2PushNotificationService;
import com.gaia.hermes2.statics.F;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

public class PushNotificationProcessor extends Hermes2BaseProcessor {

	private ExecutorService executor;
	private AtomicInteger counter = new AtomicInteger(0);

	@Override
	protected PuElement process(PuObjectRO data) {

		DeviceTokenModel deviceModel = getDeviceTokenModel();
		if (data.variableExists(F.SANDBOX) && data.getBoolean(F.SANDBOX)) {
			deviceModel.setSandbox(true);
		}
		List<DeviceTokenBean> beans = null;

		String applicationId = data.getString(F.APPLICATION_ID);

		if (data.variableExists(F.TOKEN)) {
			String token = data.getString(F.TOKEN);
			DeviceTokenBean bean = deviceModel.findByToken(token);
			if (bean != null) {
				getLogger().debug("Found device token: " + bean.getToken());
				if (beans == null) {
					beans = new ArrayList<>();
				}
				beans.add(bean);
			} else {
				getLogger().warn("Unable to find DeviceToken for tokenId: " + token);
			}
		} else {
			if (data.variableExists(F.SERVICE_TYPE)) {
				String serviceType = data.getString(F.SERVICE_TYPE);
				beans = deviceModel.findByAppIdAndServiceType(applicationId, serviceType);
			} else {
				beans = deviceModel.findByAppId(applicationId);
			}
		}

		Map<String, Collection<String>> targetDevicesByService = new HashMap<>();

		Map<String, Integer> countByService = new HashMap<>();
		for (DeviceTokenBean bean : beans) {
			String authenticatorId = bean.getAuthenticatorId();
			if (!targetDevicesByService.containsKey(authenticatorId)) {
				targetDevicesByService.put(authenticatorId, new HashSet<>());
			}
			targetDevicesByService.get(authenticatorId).add(bean.getToken());
			if (!countByService.containsKey(bean.getServiceType())) {
				countByService.put(bean.getServiceType(), 1);
			} else {
				countByService.put(bean.getServiceType(), countByService.get(bean.getServiceType()) + 1);
			}

		}
		PushTaskModel pushTaskModel = getPushTaskModel();
		PushTaskReporter taskReporter = new PushTaskReporter(pushTaskModel);
		taskReporter.setTokenModel(getDeviceTokenModel());
		PushTaskBean bean = new PushTaskBean();

		bean.setAppId(applicationId);
		bean.autoStartTime();
		bean.autoId();
		PuObject puo = new PuObject();
		Iterator<Entry<String, PuValue>> iterator = data.iterator();
		while (iterator.hasNext()) {
			Entry<String, PuValue> entry = iterator.next();
			puo.set(entry.getKey(), entry.getValue().getString());
		}
		bean.setPushRequest(puo.toJSON());

		int gcmCount = 0;
		int apnsCount = 0;
		if (countByService.containsKey("gcm")) {
			gcmCount = countByService.get("gcm");
		}
		if (countByService.containsKey("apns")) {
			apnsCount = countByService.get("apns");
		}

		bean.setTotalCount(gcmCount + apnsCount);
		bean.setApnsCount(apnsCount);
		bean.setGcmCount(gcmCount);
		pushTaskModel.insert(bean);
		taskReporter.setTaskId(bean.getId());
		taskReporter.getThreadCount().addAndGet(targetDevicesByService.size());

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
			Hermes2PushNotificationService service = getPushService(entry.getKey());
			if (service != null) {
				this.executor.execute(new Runnable() {
					@Override
					public void run() {
						service.push(new BaseHermes2Notification(entry.getValue(), message,
								data.getString(F.TITLE, null), messageId), taskReporter);
					}
				});
			} else {
				taskReporter.getThreadCount().decrementAndGet();
				getLogger().warn("Unable to get notification service for authenticator id " + entry.getKey());
			}

		});

		PuObject result = PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.TARGETS, countByService));
		result.set(F.ID, bean.getId());
		return result;

	}
}
