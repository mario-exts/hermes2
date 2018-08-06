package com.gaia.hermes2.processor;

import com.gaia.hermes2.statics.Status;

public class Hermes2Result {
	private Status status;
	private Object result;

	public Hermes2Result(Status status) {
		this.setStatus(status);
	}

	public Hermes2Result(Status status, Object result) {
		this(status);
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
