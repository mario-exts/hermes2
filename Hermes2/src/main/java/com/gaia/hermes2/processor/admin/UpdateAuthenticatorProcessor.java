package com.gaia.hermes2.processor.admin;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.PuObjectRO;

public class UpdateAuthenticatorProcessor extends Hermes2BaseProcessor{

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		String id=data.getString(F.ID, null);
		if(id==null){
			return new Hermes2Result(Status.PARAMS_MISSING);
		}
		ServiceAuthenticatorBean bean=getAuthenticatorModel().findById(id);
		if(bean==null){
			return new Hermes2Result(Status.AUTHENTICATOR_NOT_FOUND);
		}
		if(data.variableExists(F.BUNDLE_ID)){
			bean.setBundleId(data.getString(F.BUNDLE_ID));
			bean.setTopic(bean.getBundleId());
		}
		if(data.variableExists(F.TOPIC)){
			bean.setTopic(data.getString(F.TOPIC));
		}
		if(data.variableExists(F.SANDBOX)){
			bean.setSandbox(data.getBoolean(F.SANDBOX));
		}
		if(data.variableExists(F.APPLICATION_ID)){
			bean.setAppId(data.getString(F.APPLICATION_ID));
		}
		boolean b=getAuthenticatorModel().update(id, bean);
		if(b){
			return new Hermes2Result(Status.SUCCESS);
		}
		return new Hermes2Result(Status.UNKNOWN);
	}

}
