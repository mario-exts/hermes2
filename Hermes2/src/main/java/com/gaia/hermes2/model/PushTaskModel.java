package com.gaia.hermes2.model;

import com.gaia.hermes2.bean.PushTaskBean;

public interface PushTaskModel {
	PushTaskBean insert(PushTaskBean bean);
	boolean update(PushTaskBean bean);
	PushTaskBean findByTaskId(String id);
}
