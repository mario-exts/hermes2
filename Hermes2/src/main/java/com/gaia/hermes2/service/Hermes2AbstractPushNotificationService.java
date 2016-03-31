package com.gaia.hermes2.service;

import java.io.IOException;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObjectRO;

public abstract class Hermes2AbstractPushNotificationService extends BaseLoggable
		implements Hermes2PushNotificationService {

	private String name;

	@Override
	public void close() throws IOException {

	}

	@Override
	public void init(PuObjectRO properties) {

	}

	@Override
	public final void setName(String name) {
		this.name = name;
	}

	@Override
	public final String getName() {
		return this.name;
	}

}
