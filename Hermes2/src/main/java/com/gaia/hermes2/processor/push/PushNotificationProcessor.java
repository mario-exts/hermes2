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
import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.processor.PushTaskReporter;
import com.gaia.hermes2.service.BaseHermes2Notification;
import com.gaia.hermes2.service.Hermes2PushNotificationService;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

public class PushNotificationProcessor extends Hermes2BaseProcessor {

	private ExecutorService executor;
	private AtomicInteger counter = new AtomicInteger(0);

	@Override
	protected Hermes2Result process(PuObjectRO data) {

		DeviceTokenModel deviceModel = getDeviceTokenModel();
		boolean sandbox = data.getBoolean(F.SANDBOX, false);
		List<DeviceTokenBean> beans = new ArrayList<>();

		String applicationId = data.getString(F.APPLICATION_ID);
		String authenticatorId = null;
		String serviceType = data.getString(F.SERVICE_TYPE, null);
		if (serviceType != null && serviceType.length() < 3) {
			serviceType = null;
		}
		String productId = data.getString(F.PRODUCT_ID, null);
		ServiceAuthenticatorModel authenModel = getAuthenticatorModel();
		if (data.variableExists(F.BUNDLE_ID)) {
			String bundleId = data.getString(F.BUNDLE_ID);
			ServiceAuthenticatorBean service = authenModel.findByBundleId(bundleId, serviceType, sandbox);
			if (service != null) {
				authenticatorId = service.getId();
			} else {
				getLogger().debug("Authenticator for bundle: " + bundleId + "  not found");
				return new Hermes2Result(Status.AUTHENTICATOR_NOT_FOUND);
			}
		}

		if (data.variableExists(F.TOKEN)) {
			String token = data.getString(F.TOKEN);
			DeviceTokenBean bean = deviceModel.findByToken(token, authenticatorId, sandbox);
			if (bean != null) {
				getLogger().debug("Found device token: " + bean.getToken());
				beans.add(bean);
			} else {
				getLogger().warn("Unable to find DeviceToken for tokenId: " + token);
			}
		} else {
			if (serviceType != null) {
				beans = deviceModel.findByAppIdAndServiceType(applicationId, productId, serviceType, authenticatorId,
						sandbox);
			} else {
				beans = deviceModel.findByAppId(applicationId, productId, authenticatorId, sandbox);
			}
		}

		Map<String, Collection<String>> targetDevicesByService = new HashMap<>();

		Map<String, Integer> countByService = new HashMap<>();
		for (DeviceTokenBean bean : beans) {
			String authenId = bean.getAuthenticatorId();
			if (!targetDevicesByService.containsKey(authenId)) {
				targetDevicesByService.put(authenId, new HashSet<>());
			}
			targetDevicesByService.get(authenId).add(bean.getToken());
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
		int fcmCount = 0;
		if (countByService.containsKey("gcm")) {
			gcmCount = countByService.get("gcm");
		}
		if (countByService.containsKey("apns")) {
			apnsCount = countByService.get("apns");
		}
		if (countByService.containsKey("fcm")) {
			fcmCount = countByService.get("fcm");
		}

		bean.setTotalCount(gcmCount + apnsCount);
		bean.setApnsCount(apnsCount);
		bean.setGcmCount(gcmCount + fcmCount);
		pushTaskModel.insert(bean);
		taskReporter.setTaskId(bean.getId());
		taskReporter.addAndGetSubTaskCount(targetDevicesByService.size());

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
			synchronized (this) {
				if (this.executor == null) {
					this.executor = Executors.newCachedThreadPool(
							new ThreadFactoryBuilder().setNameFormat("Hermes2PushProcessor " + " #%d").build());
				}
			}
		}
		getLogger().debug("start push with {} thread", targetDevicesByService.size());
		targetDevicesByService.entrySet().forEach(entry -> {
			Hermes2PushNotificationService service = getPushService(entry.getKey());
			if (service != null) {
				this.executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							service.push(new BaseHermes2Notification(entry.getValue(), message,
									data.getString(F.TITLE, null), messageId), taskReporter);
							getLogger().debug("push for: {} ", entry.getKey());
						} catch (Exception e) {
							getLogger().error("Error when push for " + entry.getKey(), e);
							taskReporter.decrementSubTaskCount();
						}

					}
				});
			} else {
				taskReporter.decrementSubTaskCount();
				getLogger().warn("Unable to get notification service for authenticator id " + entry.getKey());
			}
		});
		PuObject result = new PuObject();
		result.set(F.ID, bean.getId());
		result.set(F.GCM, gcmCount);
		result.set(F.FCM, fcmCount);
		result.set(F.APNS, apnsCount);
		return new Hermes2Result(Status.SUCCESS, result);

	}
}
