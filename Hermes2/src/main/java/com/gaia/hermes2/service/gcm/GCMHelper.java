package com.gaia.hermes2.service.gcm;

import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_BADGE;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_BODY;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_BODY_LOC_ARGS;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_BODY_LOC_KEY;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_CLICK_ACTION;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_COLOR;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_ICON;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_SOUND;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_TAG;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_TITLE;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_TITLE_LOC_ARGS;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_TITLE_LOC_KEY;
import static com.google.android.gcm.server.Constants.JSON_PAYLOAD;
import static com.google.android.gcm.server.Constants.PARAM_COLLAPSE_KEY;
import static com.google.android.gcm.server.Constants.PARAM_CONTENT_AVAILABLE;
import static com.google.android.gcm.server.Constants.PARAM_DELAY_WHILE_IDLE;
import static com.google.android.gcm.server.Constants.PARAM_DRY_RUN;
import static com.google.android.gcm.server.Constants.PARAM_PRIORITY;
import static com.google.android.gcm.server.Constants.PARAM_RESTRICTED_PACKAGE_NAME;
import static com.google.android.gcm.server.Constants.PARAM_TIME_TO_LIVE;

import java.util.Map;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Notification;
import com.nhb.common.data.PuObject;

public class GCMHelper {

	public static PuObject convertMessageToPuObject(Message message) {
		if (message == null) {
			return null;
		}

		PuObject result = new PuObject();

		result.set(PARAM_PRIORITY, message.getPriority());
		result.set(PARAM_CONTENT_AVAILABLE, message.getContentAvailable());
		result.set(PARAM_TIME_TO_LIVE, message.getTimeToLive());
		result.set(PARAM_COLLAPSE_KEY, message.getCollapseKey());
		result.set(PARAM_RESTRICTED_PACKAGE_NAME, message.getRestrictedPackageName());
		result.set(PARAM_DELAY_WHILE_IDLE, message.isDelayWhileIdle());
		result.set(PARAM_DRY_RUN, message.isDryRun());

		Map<String, String> payload = message.getData();
		if (!payload.isEmpty()) {
			result.set(JSON_PAYLOAD, payload);
		}

		if (message.getNotification() != null) {
			Notification notification = message.getNotification();
			PuObject nMap = new PuObject();
			result.set(JSON_NOTIFICATION, nMap);

			if (notification.getBadge() != null) {
				nMap.set(JSON_NOTIFICATION_BADGE, notification.getBadge().toString());
			}
			nMap.set(JSON_NOTIFICATION_BODY, notification.getBody());
			nMap.set(JSON_NOTIFICATION_BODY_LOC_ARGS, notification.getBodyLocArgs());
			nMap.set(JSON_NOTIFICATION_BODY_LOC_KEY, notification.getBodyLocKey());
			nMap.set(JSON_NOTIFICATION_CLICK_ACTION, notification.getClickAction());
			nMap.set(JSON_NOTIFICATION_COLOR, notification.getColor());
			nMap.set(JSON_NOTIFICATION_ICON, notification.getIcon());
			nMap.set(JSON_NOTIFICATION_SOUND, notification.getSound());
			nMap.set(JSON_NOTIFICATION_TAG, notification.getTag());
			nMap.set(JSON_NOTIFICATION_TITLE, notification.getTitle());
			nMap.set(JSON_NOTIFICATION_TITLE_LOC_ARGS, notification.getTitleLocArgs());
			nMap.set(JSON_NOTIFICATION_TITLE_LOC_KEY, notification.getTitleLocKey());
		}
		return result;
	}
}
