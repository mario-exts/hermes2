package com.gaia.hermes2.model;

import java.util.List;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;

public interface ServiceAuthenticatorModel {
	ServiceAuthenticatorBean insert(ServiceAuthenticatorBean bean);
	
	boolean update(String id, ServiceAuthenticatorBean bean);
	
	boolean remove(String id);
	
	ServiceAuthenticatorBean findById(String id);
	
	List<ServiceAuthenticatorBean> findByAppId(String appId);

	ServiceAuthenticatorBean findByAppIdAndChecksum(String appId, String checksum);
	
	ServiceAuthenticatorBean findByBundleId(String bundleId, String serviceType, boolean sandbox);
	
	ServiceAuthenticatorBean findByProductId(String productId, String serviceType, boolean sandbox);
}
