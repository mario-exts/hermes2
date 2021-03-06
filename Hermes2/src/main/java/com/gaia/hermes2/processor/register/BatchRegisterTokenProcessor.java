package com.gaia.hermes2.processor.register;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gaia.hermes2.bean.DeviceTokenBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class BatchRegisterTokenProcessor extends Hermes2BaseProcessor {

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		if (this.isFromRegisterHandler()) {
			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			DeviceTokenModel deviceModel = getDeviceTokenModel();
			PuArray array = data.getPuArray(F.DEVICE_TOKENS);
			List<DeviceTokenBean> beans = new ArrayList<>();

			for (PuValue puValue : array) {
				PuObject deviceToken = puValue.getPuObject();

				String applicationId = deviceToken.getString(F.APPLICATION_ID);
				String authenticatorId = deviceToken.getString(F.AUTHENTICATOR_ID);
				String token = deviceToken.getString(F.TOKEN);
				String serviceType = deviceToken.getString(F.SERVICE_TYPE);

				String checksum = SHAEncryptor.sha512Hex(applicationId + token + authenticatorId);

				DeviceTokenBean bean = new DeviceTokenBean();
				bean.setId(UUID.randomUUID().toString());
				bean.setToken(token);
				bean.setChecksum(checksum);
				bean.setServiceType(serviceType);
				bean.setAppId(applicationId);
				bean.setAuthenticatorId(authenticatorId);
				bean.setSandbox(sandbox);
				beans.add(bean);
			}

			deviceModel.insert(beans);
			String message = beans.size() + " tokends was inserted";
			return new Hermes2Result(Status.SUCCESS,message);
		}

		return null;
	}

}
