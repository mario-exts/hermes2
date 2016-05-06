package com.gaia.hermes2.processor.register;

import java.util.UUID;

import com.gaia.hermes2.Hermes2RegisterHandler;
import com.gaia.hermes2.bean.DeviceTokenBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.statics.F;
import com.mario.entity.MessageHandler;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class RegisterTokenProcessor extends Hermes2BaseProcessor {

	@Override
	protected PuElement process(MessageHandler handler, PuObjectRO data) {
		if (handler instanceof Hermes2RegisterHandler) {
			Hermes2RegisterHandler registerHandler = (Hermes2RegisterHandler) handler;

			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			DeviceTokenModel deviceModel = registerHandler.getModelFactory()
					.getModel(DeviceTokenModel.class.toString());
			deviceModel.setSandbox(sandbox);
			String applicationId = data.getString(F.APPLICATION_ID);
			String authenticatorId = data.getString(F.AUTHENTICATOR_ID);
			String token = data.getString(F.TOKEN);
			String serviceType = data.getString(F.SERVICE_TYPE);

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
