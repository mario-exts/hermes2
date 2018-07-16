package com.gaia.hermes2.model;

import java.util.Collection;
import java.util.List;

import com.gaia.hermes2.bean.DeviceTokenBean;

public interface DeviceTokenModel {
	DeviceTokenBean insert(DeviceTokenBean bean);

	void insert(List<DeviceTokenBean> beans);

	List<DeviceTokenBean> findByAppId(String appId, String productId, String authenticatorId, boolean sandbox);

	List<DeviceTokenBean> findByAppIdAndServiceType(String appId, String productId, String serviceType,
			String authenticatorId, boolean sandbox);

	DeviceTokenBean findByToken(String token, String authenticatorId, boolean sandbox);

	DeviceTokenBean findByChecksum(String checksum);
	
	List<DeviceTokenBean> findByTokens(String appId, Collection<String> tokens);

	int removeMulti(List<String> tokens);

}
