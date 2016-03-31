package com.gaia.hermes2.service.apns;

import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.relayrides.pushy.apns.ApnsClient;

class PooledApnsClient extends DefaultPooledObject<ApnsClient<NotificationItem>> {

	PooledApnsClient(ApnsClient<NotificationItem> object) {
		super(object);
	}

}
