package com.gaia.hermes2.model;

import java.util.List;

import com.gaia.hermes2.bean.DeviceTokenBean;

public interface DeviceTokenModel {
	DeviceTokenBean insert(DeviceTokenBean bean);
	void insert(List<DeviceTokenBean> beans);
	DeviceTokenBean findById(String id);
	List<DeviceTokenBean> findByAppId(String appId);
	List<DeviceTokenBean> findByAppIdAndServiceType(String appId,String serviceType);
	List<DeviceTokenBean> findByToken(String token);
	DeviceTokenBean findByChecksum(String checksum);
	void setSandbox(boolean useSandbox);
}
