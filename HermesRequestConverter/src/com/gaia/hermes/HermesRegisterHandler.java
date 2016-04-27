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
	private static final String APNS_AUTHENTICATOR_ID = "apnsAuthenticatorId";
	private static final String GCM_AUTHENTICATOR_ID = "gcmAuthenticatorId";
	private static final String WP_AUTHENTICATOR_ID = "wpAuthenticatorId";
	private static final String IS_SANDBOX = "isSandbox";

	private String hermes2HandlerName;
	private String applicationId;
	private String apnsAuthenticatorId;
	private String gcmAuthenticatorId;
	private String wpAuthenticatorId;

	private boolean isSandbox = false;

	@Override
	public void init(PuObjectRO initParams) {
		this.hermes2HandlerName = initParams.getString(HERMES2_HANDLER_NAME);
		this.applicationId = initParams.getString(F.APPLICATION_ID);

		this.apnsAuthenticatorId = initParams.getString(APNS_AUTHENTICATOR_ID, null);
		this.gcmAuthenticatorId = initParams.getString(GCM_AUTHENTICATOR_ID, null);
		this.wpAuthenticatorId = initParams.getString(WP_AUTHENTICATOR_ID, null);

		this.isSandbox = initParams.getBoolean(IS_SANDBOX, false);
	}

	@Override
	public PuElement handle(Message message) {
		PuElement puEle = message.getData();
		if (puEle instanceof PuObject) {
			PuObject puo = (PuObject) puEle;
			getLogger().debug("Hermes got request: " + puo);
			if (puo.variableExists(F.COMMAND) && puo.getString(F.COMMAND).equalsIgnoreCase("register")) {
				if (puo.variableExists(PLATFORM_ID)) {
					int platformId = puo.getInteger(PLATFORM_ID);
					puo.remove(PLATFORM_ID);

					String serviceType = null;
					String authenticatorId = null;
					switch (platformId) {
					case 1:
						authenticatorId = this.apnsAuthenticatorId;
						serviceType = APNS;
						break;
					case 2:
						authenticatorId = this.gcmAuthenticatorId;
						serviceType = GCM;
						break;
					case 3:
						authenticatorId = this.wpAuthenticatorId;
						serviceType = WP;
						break;
					}
					if (serviceType != null && authenticatorId != null) {
						puo.setString(F.AUTHENTICATOR_ID, authenticatorId);
						puo.setString(F.SERVICE_TYPE, serviceType);
					}
				}
				puo.setBoolean(F.SANDBOX, this.isSandbox);
				puo.setString(F.COMMAND, "registerToken");
				puo.setString(F.APPLICATION_ID, this.applicationId);
				getLogger().debug("Register token: " + puo);
				PuElement result = getApi().call(this.hermes2HandlerName, puo);
				return PuObject.fromObject(new MapTuple<>(new Object[] { F.STATUS, 0, F.DATA, result }));
			}
		}
		return PuObject.fromObject(new MapTuple<>(F.STATUS, 1));
	}
}
