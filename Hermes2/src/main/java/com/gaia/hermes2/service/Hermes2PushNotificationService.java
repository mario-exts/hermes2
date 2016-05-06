package com.gaia.hermes2.service;

import java.io.Closeable;
import com.gaia.hermes2.model.PushTaskReporter;
import com.nhb.common.data.PuObjectRO;

public interface Hermes2PushNotificationService extends Closeable {

	void init(PuObjectRO properties);

	void setName(String name);

	String getName();
	
//	void push(Hermes2Notification notification);
			
	void push(Hermes2Notification notification,PushTaskReporter taskReporter);
}
