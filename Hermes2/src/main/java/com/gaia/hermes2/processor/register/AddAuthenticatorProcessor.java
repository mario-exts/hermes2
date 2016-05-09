package com.gaia.hermes2.processor.register;

import java.util.UUID;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class AddAuthenticatorProcessor extends Hermes2BaseProcessor {

	@Override
	protected PuElement process(PuObjectRO data) {
		if (this.isFromRegisterHandler()) {

			ServiceAuthenticatorModel serviceModel = getAuthenticatorModel();

			String applicationId = data.getString(F.APPLICATION_ID);
			String serviceType = data.getString(F.SERVICE_TYPE);
			byte[] authenticator = data.getRaw(F.AUTHENTICATOR);
			String password = data.getString(F.PASSWORD, "");
			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			String topic = data.getString(F.TOPIC, null);

			String checksum = SHAEncryptor.sha512Hex(new String(authenticator) + String.valueOf(sandbox));
			ServiceAuthenticatorBean bean = serviceModel.findByAppIdAndChecksum(applicationId, checksum);
			if (bean != null) {
				return PuObject.fromObject(new MapTuple<>(F.STATUS, 1, F.DESCRIPTION, "Authenticator is existing",
						F.AUTHENTICATOR_ID, bean.getId()));
			}
			bean = new ServiceAuthenticatorBean();
			bean.setId(UUID.randomUUID().toString());
			bean.setAppId(applicationId);
			bean.setServiceType(serviceType);
			bean.setAuthenticator(authenticator);
			bean.setPassword(password);
			bean.setChecksum(checksum);
			bean.setSandbox(sandbox);
			bean.setTopic(topic);

			serviceModel.insert(bean);
			return PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.AUTHENTICATOR_ID, bean.getId()));
		}
		return null;
	}

}
