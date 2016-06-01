package com.gaia.hermes2.processor.register;

import java.util.UUID;

import com.gaia.hermes2.bean.SmsServiceBean;
import com.gaia.hermes2.model.SmsServiceModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class AddSmsServiceProcessor extends Hermes2BaseProcessor{

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		if (this.isFromRegisterHandler()) {

			SmsServiceModel serviceModel = getSmsServiceModel();

			String applicationId = data.getString(F.APPLICATION_ID);
			String serviceName = data.getString(F.SERVICE_NAME);
			String accessToken= data.getString(F.ACCESS_TOKEN);
			String password = data.getString(F.PASSWORD, "");
			boolean isDefault=data.getBoolean(F.IS_DEFAULT,false);
			boolean sandbox = data.getBoolean(F.SANDBOX, false);

			String checksum = SHAEncryptor.sha512Hex(new String(accessToken) + String.valueOf(sandbox));
			SmsServiceBean bean = serviceModel.findByAppIdAndChecksum(applicationId, checksum);
			if (bean != null) {
				PuObject result=new PuObject();
				result.set(F.SMS_SERVICE_ID, bean.getId());
				return new Hermes2Result(Status.DUPLICATE_SMS_SERVICE,result);
			}
			bean = new SmsServiceBean();
			bean.setId(UUID.randomUUID().toString());
			bean.setAppId(applicationId);
			bean.setServiceName(serviceName);
			bean.setAccessToken(accessToken);
			bean.setPassword(password);
			bean.setChecksum(checksum);
			bean.setSandbox(sandbox);
			bean.setDefault(isDefault);
			serviceModel.insert(bean);
			
			PuObject result=new PuObject();
			result.set(F.SMS_SERVICE_ID, bean.getId());
			return new Hermes2Result(Status.SUCCESS,result);
		}
		return new Hermes2Result(Status.UNKNOWN);
	}

}
