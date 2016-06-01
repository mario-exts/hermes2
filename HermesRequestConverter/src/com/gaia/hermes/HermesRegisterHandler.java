package com.gaia.hermes;

import com.gaia.hermes2.statics.F;
import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class HermesRegisterHandler extends BaseMessageHandler {

	private static final String APNS = "apns";
	private static final String GCM = "gcm";
	private static final String WP = "wp";

	private static final String PLATFORM_ID = "platformId";
	private static final String HERMES2_HANDLER_NAME = "hermes2HandlerName";

	private String hermes2HandlerName;
	private String applicationId;

	@Override
	public void init(PuObjectRO initParams) {
		this.hermes2HandlerName = initParams.getString(HERMES2_HANDLER_NAME);
		this.applicationId = initParams.getString(F.APPLICATION_ID);
	}

	@Override
	public PuElement handle(Message message) {
		PuElement puEle = message.getData();
		if (puEle instanceof PuObject) {
			PuObject puo = (PuObject) puEle;
			if (puo.variableExists(F.COMMAND) && puo.getString(F.COMMAND).equalsIgnoreCase("register")) {
				if (puo.variableExists(PLATFORM_ID)) {
					int platformId = puo.getInteger(PLATFORM_ID);
					puo.remove(PLATFORM_ID);
					String serviceType = null;
					switch (platformId) {
					case 1:
						serviceType = APNS;
						break;
					case 2:
						serviceType = GCM;
						break;
					case 3:
						serviceType = WP;
						break;
					}
					if (serviceType != null) {
						puo.setString(F.SERVICE_TYPE, serviceType);
					}
				}

				puo.setString(F.COMMAND, "registerToken");
				puo.setString(F.APPLICATION_ID, this.applicationId);

				PuElement result = getApi().call(this.hermes2HandlerName, puo);
				getLogger().debug("Register token: " + puo + " --> result: " + result);
				return result;
			}
		}
		return PuObject.fromObject(new MapTuple<>(F.STATUS, 1));
	}
}
