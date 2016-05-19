package com.gaia.hermes2.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BaseHermes2Notification implements Hermes2Notification {

	private final Set<String> recipients = new HashSet<>();
	private String message;
	private int badge;
	private String title;
	private String messageId;

	public BaseHermes2Notification(Collection<String> recipients, String message) {
		this.addRecipients(recipients);
		this.setMessage(message);
	}

	public BaseHermes2Notification(Collection<String> recipients, String message, String title) {
		this.addRecipients(recipients);
		this.setMessage(message);
		this.setTitle(title);
	}

	public BaseHermes2Notification(Collection<String> recipients, String message, String title, String messageId) {
		this.addRecipients(recipients);
		this.setMessage(message);
		this.setTitle(title);
		this.setMessageId(messageId);
	}
	
	public void addRecipients(Collection<String> recipients) {
		if (recipients != null) {
			this.recipients.addAll(recipients);
		}
	}

	public void addRecipients(String... recipients) {
		if (recipients != null) {
			this.addRecipients(Arrays.asList(recipients));
		}
	}

	public void removeRecipients(Collection<String> tobeRemovedRecipients) {
		if (tobeRemovedRecipients != null) {
			this.recipients.removeAll(tobeRemovedRecipients);
		}
	}

	public void removeRecipients(String... tobeRemovedRecipients) {
		if (tobeRemovedRecipients != null) {
			this.removeRecipients(Arrays.asList(tobeRemovedRecipients));
		}
	}

	@Override
	public Set<String> getRecipients() {
		return this.recipients;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int getBadge() {
		return this.badge;
	}

	public void setBadge(int value) {
		this.badge = value;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}	

	
}
