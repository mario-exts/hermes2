package com.gaia.hermes2.processor.register;

import java.util.UUID;

import com.gaia.hermes2.bean.DeviceTokenBean;
import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class RegisterTokenProcessor extends Hermes2BaseProcessor {

	@Override
	protected PuElement process(PuObjectRO data) {
		if (this.isFromRegisterHandler()) {

			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			DeviceTokenModel deviceModel = getDeviceTokenModel();
			deviceModel.setSandbox(sandbox);
			String applicationId = data.getString(F.APPLICATION_ID, null);
			String authenticatorId = data.getString(F.AUTHENTICATOR_ID, null);
			String token = data.getString(F.TOKEN, null);
			String serviceType = data.getString(F.SERVICE_TYPE);
			String bundleId = data.getString(F.BUNDLE_ID, null);

			if (token == null || applicationId == null) {
				return new PuValue("Parameters is missing");
			}

			if (bundleId != null) {
				ServiceAuthenticatorModel authModel = getAuthenticatorModel();
				ServiceAuthenticatorBean authBean = authModel.findByBundleId(bundleId, sandbox);
				if (authBean != null) {
					authenticatorId = authBean.getId();
				}
			}
			if (authenticatorId == null) {
				return new PuValue("Service authenticator not found");
			}
			String checksum = SHAEncryptor.sha512Hex(applicationId + token + authenticatorId);
			DeviceTokenBean bean = deviceModel.findByChecksum(checksum);
			if (bean != null) {
				return PuObject
						.fromObject(new MapTuple<>(F.STATUS, 1, F.DESCRIPTION, "Duplicate token", F.ID, bean.getId()));
			}

			bean = new DeviceTokenBean();
			bean.setId(UUID.randomUUID().toString());
			bean.setToken(token);
			bean.setChecksum(checksum);
			bean.setServiceType(serviceType);
			bean.setAppId(applicationId);
			bean.setAuthenticatorId(authenticatorId);

			deviceModel.insert(bean);

			return PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.ID, bean.getId(), F.DESCRIPTION,
					"The ID use for push notification later"));
		}
		return null;
	}
}
