package com.gaia.hermes2.service.apns;

import java.util.Date;

import com.relayrides.pushy.apns.ApnsPushNotification;
import com.relayrides.pushy.apns.DeliveryPriority;

class NotificationItem implements ApnsPushNotification {

	private final String topic;
	private final String token;
	private final String payload;
	private final Date expiration;
	private final DeliveryPriority priority;

	NotificationItem(String token, String payload, DeliveryPriority priority, Date expDate, String topic) {
		this.token = token;
		this.payload = payload;
		this.topic = topic;
		this.expiration = expDate;
		this.priority = priority;
	}

	NotificationItem(String token, String payload) {
		this(token, payload, null, null, null);
	}

	NotificationItem(String token, String payload, DeliveryPriority priority) {
		this(token, payload, priority, null, null);
	}

	NotificationItem(String token, String payload, Date expDate) {
		this(token, payload, null, expDate, null);
	}

	NotificationItem(String token, String payload, DeliveryPriority priority, Date expDate) {
		this(token, payload, priority, expDate, null);
	}

	NotificationItem(String token, String payload, String topic) {
		this(token, payload, null, null, topic);
	}

	@Override
	public String getToken() {
		return this.token;
	}

	@Override
	public String getPayload() {
		return this.payload;
	}

	@Override
	public Date getExpiration() {
		return this.expiration;
	}

	@Override
	public DeliveryPriority getPriority() {
		return this.priority;
	}

	@Override
	public String getTopic() {
		return this.topic;
	}

}