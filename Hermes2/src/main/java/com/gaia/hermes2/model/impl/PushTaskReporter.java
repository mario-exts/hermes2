package com.gaia.hermes2.model.impl;

import com.gaia.hermes2.bean.PushTaskBean;
import com.gaia.hermes2.model.PushTaskModel;

public class PushTaskReporter {

	private PushTaskBean bean;
	private PushTaskModel model;

	public PushTaskReporter(PushTaskModel model) {
		this.model = model;
		this.bean = new PushTaskBean();
	}

	public void saveTask() {
		if (model != null && bean != null) {
			model.insert(bean);
		}
	}

	public void update() {
		if (model != null && bean != null) {
			model.update(bean);
		}
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
