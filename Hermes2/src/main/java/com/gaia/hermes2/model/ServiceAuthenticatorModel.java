package com.gaia.hermes2.model;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;

public interface ServiceAuthenticatorModel {
	ServiceAuthenticatorBean insert(ServiceAuthenticatorBean bean);
	
	ServiceAuthenticatorBean findById(String id);

	ServiceAuthenticatorBean findByAppIdAndChecksum(String appId, String checksum);
}
