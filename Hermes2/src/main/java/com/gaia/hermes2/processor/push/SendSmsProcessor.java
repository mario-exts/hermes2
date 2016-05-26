package com.gaia.hermes2.processor.push;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.gaia.hermes2.bean.PushSmsBean;
import com.gaia.hermes2.model.PushSmsModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.service.Hermes2SmsService;
import com.gaia.hermes2.service.sms.SmsContent;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

public class SendSmsProcessor extends Hermes2BaseProcessor {

	@Override
	protected PuElement process(PuObjectRO data) {
		PuObject response = new PuObject();

		if (!data.variableExists(F.MESSAGE)) {
			response.set(F.STATUS, "Thiếu tham số message");
			return response;
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
				response.set("status", "Tham số phones không đúng");
				return response;
			}
		} else {
			response.set("status", "Thiếu tham số phone");
			return response;
		}
		if(data.variableExists(F.TYPE)){
			smsType=data.getInteger(F.TYPE);
			if(smsType<1 || smsType>3){
				smsType=SmsContent.QC;
			}
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
		
		SmsContent content=new SmsContent();
		content.setMessage(message);
		content.setRecipients(recipients);
		if(brandName!=null){
			content.setBrandName(brandName);
		}else{
			if(smsType!=SmsContent.CSKH){
				content.setSmsType(SmsContent.QC);
			}
		}
		
		Hermes2SmsService service=getDefaultSmsService();
		if(service!=null){
			service.sendSms(content);
		}else{
			getLogger().debug("can not get SMS service");
		}
		PuObject result = PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.TARGETS, recipients.size()));
		result.set(F.ID, bean.getId());
		return result;
	}

}
