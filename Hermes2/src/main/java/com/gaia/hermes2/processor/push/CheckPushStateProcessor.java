package com.gaia.hermes2.processor.push;

import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.statics.F;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class CheckPushStateProcessor extends Hermes2BaseProcessor {

	@Override
	protected PuElement process(PuObjectRO data) {
		PushTaskModel model=getPushTaskModel();
		if (model!=null) {
			if (data.variableExists(F.ID)) {
				String taskId = data.getString(F.ID);
				PushTaskBean bean = model.findByTaskId(taskId);
				if (bean != null) {
					return bean.toPuObject();
				}
			}
		}
		PuObject puo = new PuObject();
		puo.set("status", "Kiểm tra thất bại");
		return puo;
	}

}
