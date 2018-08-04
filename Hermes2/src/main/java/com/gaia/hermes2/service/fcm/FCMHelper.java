package com.gaia.hermes2.service.fcm;

import java.util.Map;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Notification;
import com.nhb.common.data.PuObject;

public class FCMHelper {

	public static PuObject convertMessageToPuObject(Message message) {
		if (message == null) {
			return null;
		}

		PuObject result = new PuObject();

		result.set(Constants.PARAM_PRIORITY, message.getPriority());
		result.set(Constants.PARAM_CONTENT_AVAILABLE, message.getContentAvailable());
		result.set(Constants.PARAM_TIME_TO_LIVE, message.getTimeToLive());
		result.set(Constants.PARAM_COLLAPSE_KEY, message.getCollapseKey());
		result.set(Constants.PARAM_RESTRICTED_PACKAGE_NAME, message.getRestrictedPackageName());
		result.set(Constants.PARAM_DELAY_WHILE_IDLE, message.isDelayWhileIdle());
		result.set(Constants.PARAM_DRY_RUN, message.isDryRun());

		Map<String, String> payload = message.getData();
		if (!payload.isEmpty()) {
			result.set(Constants.JSON_PAYLOAD, payload);
		}

		if (message.getNotification() != null) {
			Notification notification = message.getNotification();
			PuObject nMap = new PuObject();
			result.set(Constants.JSON_NOTIFICATION, nMap);

			if (notification.getBadge() != null) {
				nMap.set(Constants.JSON_NOTIFICATION_BADGE, notification.getBadge().toString());
			}
			nMap.set(Constants.JSON_NOTIFICATION_BODY, notification.getBody());
			nMap.set(Constants.JSON_NOTIFICATION_BODY_LOC_ARGS, notification.getBodyLocArgs());
			nMap.set(Constants.JSON_NOTIFICATION_BODY_LOC_KEY, notification.getBodyLocKey());
			nMap.set(Constants.JSON_NOTIFICATION_CLICK_ACTION, notification.getClickAction());
			nMap.set(Constants.JSON_NOTIFICATION_COLOR, notification.getColor());
			nMap.set(Constants.JSON_NOTIFICATION_ICON, notification.getIcon());
			nMap.set(Constants.JSON_NOTIFICATION_SOUND, notification.getSound());
			nMap.set(Constants.JSON_NOTIFICATION_TAG, notification.getTag());
			nMap.set(Constants.JSON_NOTIFICATION_TITLE, notification.getTitle());
			nMap.set(Constants.JSON_NOTIFICATION_TITLE_LOC_ARGS, notification.getTitleLocArgs());
			nMap.set(Constants.JSON_NOTIFICATION_TITLE_LOC_KEY, notification.getTitleLocKey());
		}
		return result;
	}
}
