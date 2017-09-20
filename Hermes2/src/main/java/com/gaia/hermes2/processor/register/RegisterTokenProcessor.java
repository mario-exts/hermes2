package com.gaia.hermes2.processor.register;

import java.util.UUID;

import com.gaia.hermes2.bean.DeviceTokenBean;
import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class RegisterTokenProcessor extends Hermes2BaseProcessor {

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		if (this.isFromRegisterHandler()) {

			DeviceTokenModel deviceModel = getDeviceTokenModel();

			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			String applicationId = data.getString(F.APPLICATION_ID, null);
			String token = data.getString(F.TOKEN, null);
			String serviceType = data.getString(F.SERVICE_TYPE);
			String bundleId = data.getString(F.BUNDLE_ID, null);
			String productId = data.getString(F.PRODUCT_ID, null);
			if (token == null || applicationId == null) {
				return new Hermes2Result(Status.PARAMS_MISSING);
			}

			String authenticatorId = null;
			
			if (bundleId != null) {
				ServiceAuthenticatorModel authModel = getAuthenticatorModel();
				ServiceAuthenticatorBean authBean = authModel.findByBundleId(bundleId, serviceType, sandbox);
				if (authBean != null) {
					authenticatorId = authBean.getId();
					productId=authBean.getProductId();
				}
			}
//			if (authenticatorId == null && productId != null) {
//				ServiceAuthenticatorModel authModel = getAuthenticatorModel();
//				ServiceAuthenticatorBean authBean = authModel.findByProductId(productId, serviceType, sandbox);
//				if (authBean != null) {
//					authenticatorId = authBean.getId();
//				}
//			}

			if (authenticatorId == null) {
				return new Hermes2Result(Status.AUTHENTICATOR_NOT_FOUND);
			}
			String plaintext = applicationId + token + authenticatorId;
			if (productId != null) {
				plaintext = plaintext + productId;
			}
			String checksum = SHAEncryptor.sha512Hex(plaintext);
			DeviceTokenBean bean = deviceModel.findByChecksum(checksum);
			if (bean != null) {
				PuObject result = new PuObject();
				result.set(F.ID, bean.getId());
				return new Hermes2Result(Status.DUPLICATE_TOKEN, result);
			}

			bean = new DeviceTokenBean();
			bean.setId(UUID.randomUUID().toString());
			bean.setToken(token);
			bean.setChecksum(checksum);
			bean.setServiceType(serviceType);
			bean.setAppId(applicationId);
			bean.setAuthenticatorId(authenticatorId);
			bean.setSandbox(sandbox);
			bean.setProductId(productId);
			deviceModel.insert(bean);
			getLogger().debug("register token to " + serviceType + ", authenId: " + authenticatorId);
			PuObject result = PuObject.fromObject(
					new MapTuple<>(F.ID, bean.getId(), F.DESCRIPTION, "The ID use for push notification later"));
			return new Hermes2Result(Status.SUCCESS, result);
		}
		return null;
	}
}
