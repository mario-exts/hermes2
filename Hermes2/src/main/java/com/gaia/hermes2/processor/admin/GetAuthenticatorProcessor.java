package com.gaia.hermes2.processor.admin;

import java.util.List;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class GetAuthenticatorProcessor extends Hermes2BaseProcessor{

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		String appId=data.getString(F.APPLICATION_ID, null);
		if(appId==null){
			return new Hermes2Result(Status.PARAMS_MISSING);
		}
		List<ServiceAuthenticatorBean> beans=getAuthenticatorModel().findByAppId(appId);
		PuArray arr=new PuArrayList();
		for(ServiceAuthenticatorBean b:beans){
			PuObject puo=b.toPuObject();
			puo.remove(F.APPLICATION_ID);
			if(puo.getString(F.SERVICE_TYPE).equals("apns")){
				puo.remove(F.AUTHENTICATOR);
			}
			arr.addFrom(puo);
		}
		PuObject puo=new PuObject();
		puo.set(F.AUTHENTICATOR, arr);
		return new Hermes2Result(Status.SUCCESS, puo);
		
	}

}
