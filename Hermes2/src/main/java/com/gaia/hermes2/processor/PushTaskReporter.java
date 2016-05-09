package com.gaia.hermes2.processor;

import java.util.concurrent.atomic.AtomicInteger;

import com.gaia.hermes2.model.PushTaskModel;

public class PushTaskReporter {
	private String taskId;
	private PushTaskModel model;
	private AtomicInteger threadCount;

	public PushTaskReporter(PushTaskModel model) {
		this.model = model;
		threadCount = new AtomicInteger(0);
	}

	public boolean increaseGcmCount(int success, int failure) {
		return model.updateGcm(this.taskId, success, failure);
	}

	public boolean increaseApnsCount(int success, int failure) {
		return model.updateApns(this.taskId, success, failure);
	}

	public boolean complete() {
		return model.updateTaskState(this.taskId, true);
	}

	// public PushTaskBean getTask(){
	// return model.findByTaskId(taskId);
	// }

	public void setTaskId(String id) {
		this.taskId = id;
	}

	public String getTaskId() {
		return this.taskId;
	}

	public PushTaskModel getModel() {
		return model;
	}

	public void setModel(PushTaskModel model) {
		this.model = model;
	}

	public AtomicInteger getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(AtomicInteger threadCount) {
		this.threadCount = threadCount;
	}

}
