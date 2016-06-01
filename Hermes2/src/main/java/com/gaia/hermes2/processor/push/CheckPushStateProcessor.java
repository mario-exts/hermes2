package com.gaia.hermes2.processor.push;

import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.processor.Hermes2Result;
import com.gaia.hermes2.statics.F;
import com.gaia.hermes2.statics.Status;
import com.nhb.common.data.PuObjectRO;

public class CheckPushStateProcessor extends Hermes2BaseProcessor {

	@Override
	protected Hermes2Result process(PuObjectRO data) {
		PushTaskModel model=getPushTaskModel();
		if (data.variableExists(F.ID)) {
			String taskId = data.getString(F.ID);
			getLogger().debug("check for "+taskId);
			PushTaskBean bean = model.findByTaskId(taskId);
			if (bean != null) {
				getLogger().debug("checkTask with ID: "+taskId+"  is success");
				return new Hermes2Result(Status.SUCCESS,bean.toPuObject());
			}
		}
		getLogger().debug("checkTask is error");
		return new Hermes2Result(Status.CHECK_TASK_UNSUCCESSFUL);
	}

}
