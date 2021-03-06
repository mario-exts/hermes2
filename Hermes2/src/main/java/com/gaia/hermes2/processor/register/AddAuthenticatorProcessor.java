package com.gaia.hermes2.processor.register;

import java.util.UUID;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class AddAuthenticatorProcessor extends Hermes2BaseProcessor {

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		if (this.isFromRegisterHandler()) {

			if (!data.variableExists(F.SERVICE_TYPE)
					|| !data.variableExists(F.AUTHENTICATOR) || !data.variableExists(F.APPLICATION_ID)) {
				return new Hermes2Result(Status.PARAMS_MISSING);
			}
			ServiceAuthenticatorModel serviceModel = getAuthenticatorModel();

			String applicationId = data.getString(F.APPLICATION_ID);
			String serviceType = data.getString(F.SERVICE_TYPE);
			byte[] authenticator = data.getRaw(F.AUTHENTICATOR);
			String password = data.getString(F.PASSWORD, "");
			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			String topic = data.getString(F.TOPIC, null);
			String bundleId = data.getString(F.BUNDLE_ID, null);
			String productId=data.getString(F.PRODUCT_ID, null);
			if (topic == null) {
				topic = bundleId;
			}
			String plainText=new String(authenticator) + String.valueOf(sandbox) + bundleId+applicationId;
//			if(productId!=null){
//				plainText=plainText+productId;
//			}
			String checksum = SHAEncryptor.sha512Hex(plainText);
			ServiceAuthenticatorBean bean = serviceModel.findByAppIdAndChecksum(applicationId, checksum);
			if (bean != null) {
				PuObject result = new PuObject();
				result.set(F.AUTHENTICATOR_ID, bean.getId());
				return new Hermes2Result(Status.DUPLICATE_AUTHENTICATOR_ID, result);
			} else if (bundleId != null) {
				bean = serviceModel.findByBundleId(bundleId, serviceType, sandbox);
				if (bean != null) {
					PuObject result = PuObject
							.fromObject(new MapTuple<>(F.AUTHENTICATOR_ID, bean.getId(),
									F.BUNDLE_ID, bundleId,
									F.PRODUCT_ID, productId));
					return new Hermes2Result(Status.DUPLICATE_BUNDLE_ID, result);
				}
			}
//			else if (productId!=null){
//				bean = serviceModel.findByProductId(productId, serviceType, sandbox);
//				if (bean != null) {
//					PuObject result = PuObject
//							.fromObject(new MapTuple<>(F.AUTHENTICATOR_ID, bean.getId(),
//									F.PRODUCT_ID, productId));
//					return new Hermes2Result(Status.DUPLICATE_PRODUCT_ID, result);
//				}
//			}
			bean = new ServiceAuthenticatorBean();
			bean.setId(UUID.randomUUID().toString());
			bean.setAppId(applicationId);
			bean.setServiceType(serviceType);
			bean.setAuthenticator(authenticator);
			bean.setPassword(password);
			bean.setChecksum(checksum);
			bean.setSandbox(sandbox);
			bean.setTopic(topic);
			bean.setBundleId(bundleId);
			bean.setProductId(productId);
			serviceModel.insert(bean);
			PuObject result = new PuObject();
			result.set(F.AUTHENTICATOR_ID, bean.getId());
			return new Hermes2Result(Status.SUCCESS, result);
		}
		return null;
	}

}
