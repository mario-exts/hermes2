package com.gaia.hermes2.processor.push;

import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.PuObjectRO;

public class ResetServiceProcessor extends Hermes2BaseProcessor{

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		if(!data.variableExists(F.ID)){
			return new Hermes2Result(Status.PARAMS_MISSING);
		}
		String authenticatorId=data.getString(F.ID);
		boolean b=this.getHandler().removeServiceFromMap(authenticatorId);
		if(b){
			return new Hermes2Result(Status.SUCCESS);
		}else{
			return new Hermes2Result(Status.UNKNOWN);
		}
	}

}
