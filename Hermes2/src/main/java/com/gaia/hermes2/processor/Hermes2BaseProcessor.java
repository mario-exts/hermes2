package com.gaia.hermes2.processor;

import com.gaia.hermes2.statics.F;
import com.mario.entity.MessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.strategy.CommandController;
import com.nhb.strategy.CommandProcessor;
import com.nhb.strategy.CommandRequestParameters;
import com.nhb.strategy.CommandResponseData;

public abstract class Hermes2BaseProcessor extends BaseLoggable implements CommandProcessor {

	@Override
	public final CommandResponseData execute(CommandController context, CommandRequestParameters request) {
		if (request instanceof Message) {
			PuElement result = this.process(context.getEnvironment(F.HANDLER),
					(PuObjectRO) ((Message) request).getData());
			return new Hermes2ProcessorResponseData(result);
		}
		return null;
	}

	protected abstract PuElement process(MessageHandler handler, PuObjectRO data);
}
