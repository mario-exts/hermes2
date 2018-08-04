package com.gaia.hermes2.processor.admin;

import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.PuObjectRO;

public class RemoveAuthenticatorProcessor extends Hermes2BaseProcessor{

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		String id=data.getString(F.ID, null);
		if(id==null){
			return new Hermes2Result(Status.PARAMS_MISSING);
		}
		boolean b=getAuthenticatorModel().remove(id);
		if(b){
			return new Hermes2Result(Status.SUCCESS);
		}
		return new Hermes2Result(Status.UNKNOWN);
	}

}
