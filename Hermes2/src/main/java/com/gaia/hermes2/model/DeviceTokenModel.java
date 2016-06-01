package com.gaia.hermes2.model;

import java.util.List;

import com.gaia.hermes2.bean.DeviceTokenBean;

public interface DeviceTokenModel {
	DeviceTokenBean insert(DeviceTokenBean bean);

	void insert(List<DeviceTokenBean> beans);

	List<DeviceTokenBean> findByAppId(String appId,boolean sandbox);

	List<DeviceTokenBean> findByAppIdAndServiceType(String appId, String serviceType,boolean sandbox);

	DeviceTokenBean findByToken(String token,boolean sandbox);

	DeviceTokenBean findByChecksum(String checksum);
	
	int removeMulti(List<String> tokens);

}
