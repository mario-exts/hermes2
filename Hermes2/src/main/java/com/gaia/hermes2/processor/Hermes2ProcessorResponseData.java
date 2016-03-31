package com.gaia.hermes2.processor;

import com.nhb.common.data.PuElement;
import com.nhb.strategy.CommandResponseData;

public class Hermes2ProcessorResponseData implements CommandResponseData {

	private PuElement result;

	public Hermes2ProcessorResponseData() {

	}

	public Hermes2ProcessorResponseData(PuElement result) {
		this.setResult(result);
	}

	public PuElement getResult() {
		return result;
	}

	public void setResult(PuElement result) {
		this.result = result;
	}
}
