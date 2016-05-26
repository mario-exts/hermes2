package com.gaia.hermes2.model;

import com.gaia.hermes2.bean.SmsServiceBean;

public interface SmsServiceModel {
	SmsServiceBean insert(SmsServiceBean bean);

	boolean update(SmsServiceBean bean);

	SmsServiceBean findById(String id);

	SmsServiceBean findByAppId(String appId);

	SmsServiceBean findByAppIdAndChecksum(String appId, String checksum);
	
	SmsServiceBean findDefault();
}
