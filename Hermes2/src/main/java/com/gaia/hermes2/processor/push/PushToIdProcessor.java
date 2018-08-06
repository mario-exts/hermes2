package com.gaia.hermes2.processor.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.gaia.hermes2.bean.DeviceTokenBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.service.BaseHermes2Notification;
import com.gaia.hermes2.service.Hermes2PushNotificationService;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

public class PushToIdProcessor extends Hermes2BaseProcessor {
	private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
		int count = 0;

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, String.format("[PUSH IDS] Thread #%d", count++));
		}
	});

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		String title = data.getString(F.TITLE, "");
		String message = data.getString(F.MESSAGE, null);
		PuArray idArr = data.getPuArray("ids", null);
		String appId = data.getString(F.APPLICATION_ID, null);
		if (appId == null || message == null || idArr == null) {
			return new Hermes2Result(Status.PARAMS_MISSING);
		}
		Set<String> ids = new HashSet<>();
		for (PuValue val : idArr) {
			ids.add(val.getString());
		}
		DeviceTokenModel model = getDeviceTokenModel();
		List<DeviceTokenBean> beans = model.findByTokens(appId, ids);
		Map<String, List<String>> authenticator2Tokens = new HashMap<>(20);
		for (DeviceTokenBean bean : beans) {
			if (authenticator2Tokens.containsKey(bean.getAuthenticatorId())) {
				authenticator2Tokens.get(bean.getAuthenticatorId()).add(bean.getToken());
			} else {
				ArrayList<String> list = new ArrayList<>();
				list.add(bean.getToken());
				authenticator2Tokens.put(bean.getAuthenticatorId(), list);
			}
		}
		getLogger().debug("start push to {} service, total size: {}", authenticator2Tokens.size(), idArr.size());
		authenticator2Tokens.entrySet().forEach(entry -> {
			Hermes2PushNotificationService service = getPushService(entry.getKey());
			if (service != null) {
				this.executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							service.push(new BaseHermes2Notification(entry.getValue(), message, title, "1"), null);
							getLogger().debug("push for: {} ", entry.getKey());
						} catch (Exception e) {
							getLogger().error("Error when push for " + entry.getKey(), e);
						}

					}
				});
			}
		});
		return new Hermes2Result(Status.SUCCESS);
	}

}
