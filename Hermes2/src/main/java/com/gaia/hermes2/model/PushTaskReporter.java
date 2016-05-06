package com.gaia.hermes2.model;

import com.gaia.hermes2.Hermes2PushHandler;
import com.gaia.hermes2.bean.PushTaskBean;

public class PushTaskReporter {
	private PushTaskBean bean;
	private PushTaskModel model;

	public PushTaskReporter(Hermes2PushHandler handler) {
		bean = new PushTaskBean();
		model = new PushTaskModel(handler);
	}

	public PushTaskReporter(PushTaskBean bean, PushTaskModel model) {
		this.bean = bean;
		this.model = model;
	}

	public void saveTask() {
		model.insert(bean);
	}

	public void updateGcm() {
		model.updateGcmPushCount(bean);
	}

	public void updateApns() {
		model.updateApnsPushCount(bean);
	}

	public void doneTaske() {
		model.doneTask(bean);
	}

	public PushTaskBean getTask() {
		return bean;
	}

	public void setTask(PushTaskBean bean) {
		this.bean = bean;
	}

	public PushTaskModel getModel() {
		return model;
	}

	public void setModel(PushTaskModel model) {
		this.model = model;
	}
}
