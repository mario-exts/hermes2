package com.gaia.hermes2.model;

import com.gaia.hermes2.bean.PushSmsBean;

public interface PushSmsModel {
	PushSmsBean insert(PushSmsBean bean);

	boolean update(PushSmsBean bean);

	PushSmsBean findById(String id);

}
