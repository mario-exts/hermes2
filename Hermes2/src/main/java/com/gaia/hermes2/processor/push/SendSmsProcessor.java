package com.gaia.hermes2.processor.push;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.gaia.hermes2.bean.PushSmsBean;
import com.gaia.hermes2.model.PushSmsModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.service.Hermes2SmsService;
import com.gaia.hermes2.service.sms.SmsEnvelop;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

import vn.speedsms.client.enums.SpeedSMSType;

public class SendSmsProcessor extends Hermes2BaseProcessor {

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		if (!data.variableExists(F.MESSAGE)) {
			return new Hermes2Result(Status.PARAMS_MISSING);
		}
		String message = data.getString(F.MESSAGE);
		Set<String> recipients = new HashSet<>();
		int smsType=1;
		String brandName=null;
		if (data.variableExists(F.PHONE)) {
			String phone = data.getString(F.PHONE);
			recipients.add(phone);
		} else if (data.variableExists(F.PHONES)) {
			if (data.typeOf(F.PHONES) == PuDataType.PUARRAY) {
				PuArray arr = data.getPuArray(F.PHONES);
				for (PuValue val : arr) {
					recipients.add(val.getString());
				}
			} else {
				return new Hermes2Result(Status.WRONG_PARAMS);
			}
		} else {
			return new Hermes2Result(Status.PARAMS_MISSING);
		}
		if(data.variableExists(F.TYPE)){
			smsType=data.getInteger(F.TYPE);
		}
		if(data.variableExists(F.BRAND_NAME)){
			brandName=data.getString(F.BRAND_NAME);
		}
		PushSmsBean bean = new PushSmsBean();
		bean.setMessage(message);
		bean.setRecipients(recipients);
		bean.setTotalCount(recipients.size());
		bean.setDone(false);
		bean.autoCreatedTime();
		bean.setId(UUID.randomUUID().toString());
		bean.setTotalPrice(0L);
		PushSmsModel model = getPushSmsModel();
		model.insert(bean);
		
		SmsEnvelop sms=new SmsEnvelop();
		sms.setContent(message);
		sms.setRecipients(recipients);
		if(brandName!=null){
			sms.setBrandName(brandName);
			sms.setSmsType(SpeedSMSType.BRAND_NAME);
		}else{
			sms.setSmsType(SpeedSMSType.fromType(smsType));
		}
		
		Hermes2SmsService service=getDefaultSmsService();
		if(service!=null){
			service.sendSms(sms);
		}else{
			getLogger().debug("can not get SMS service");
		}
		PuObject result = PuObject.fromObject(new MapTuple<>(F.TARGETS, recipients.size()));
		result.set(F.ID, bean.getId());
		return new Hermes2Result(Status.SUCCESS, result);
	}

}
