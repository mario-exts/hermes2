package com.gaia.hermes2.service;

import java.util.Set;

public interface Hermes2Notification {

	/**
	 * Target device's tokens specific for each service type (APNS, GCM...) to
	 * be received message
	 * 
	 * @return
	 */
	Set<String> getRecipients();

	String getMessage();

	int getBadge();

	String getTitle();
	
	String getMessageId();
	
}
