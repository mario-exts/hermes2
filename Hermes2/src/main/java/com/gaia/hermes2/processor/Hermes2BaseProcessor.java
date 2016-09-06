package com.gaia.hermes2.processor;

import com.gaia.hermes2.Hermes2AdminHandler;
import com.gaia.hermes2.Hermes2PushHandler;
import com.gaia.hermes2.Hermes2RegisterHandler;
import com.gaia.hermes2.model.DeviceTokenModel;
import com.gaia.hermes2.model.PushSmsModel;
import com.gaia.hermes2.model.PushTaskModel;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.model.SmsServiceModel;
import com.gaia.hermes2.service.Hermes2PushNotificationService;
import com.gaia.hermes2.service.Hermes2SmsService;
import com.gaia.hermes2.statics.F;
import com.mario.entity.MessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.strategy.CommandController;
import com.nhb.strategy.CommandProcessor;
import com.nhb.strategy.CommandRequestParameters;
import com.nhb.strategy.CommandResponseData;

public abstract class Hermes2BaseProcessor extends BaseLoggable implements CommandProcessor {
	private DeviceTokenModel deviceTokenModel;
	private PushTaskModel pushTaskModel;
	private ServiceAuthenticatorModel authenticatorModel;
	private PushSmsModel pushSmsModel;
	private SmsServiceModel smsServiceModel;
	private MessageHandler handler;

	@Override
	public final CommandResponseData execute(CommandController context, CommandRequestParameters request) {
		if (request instanceof Message) {
			if (this.handler == null) {
				this.handler = context.getEnvironment(F.HANDLER);
			}
			Hermes2Result result = this.process((PuObjectRO) ((Message) request).getData());
			PuObject data = new PuObject();
			data.set(F.STATUS, result.getStatus().getId() + "");
			data.set(F.MESSAGE, result.getStatus().getMessage());
			if (result.getResult() != null) {
				data.set(F.DATA, result.getResult());
			}
			return new Hermes2ProcessorResponseData(data);
		}

		return null;
	}

	// protected abstract PuElement process(MessageHandler handler, PuObjectRO
	// data);
	protected abstract Hermes2Result process(PuObjectRO data);

	protected Hermes2PushHandler getHandler(){
		return (Hermes2PushHandler) this.handler;
	}
	
	protected Hermes2PushNotificationService getPushService(String authenticatorId) {
		if (this.handler instanceof Hermes2PushHandler) {
			return ((Hermes2PushHandler) handler).getPushService(authenticatorId);
		}
		return null;

	}

	protected Hermes2SmsService getDefaultSmsService() {
		if (this.handler instanceof Hermes2PushHandler) {
			return ((Hermes2PushHandler) handler).getDefaultSmsService();
		}
		getLogger().debug("return null");
		return null;

	}

	protected DeviceTokenModel getDeviceTokenModel() {
		if (this.deviceTokenModel == null) {
			synchronized (this) {
				if (this.deviceTokenModel == null) {
					if (this.handler instanceof Hermes2PushHandler) {
						this.deviceTokenModel = ((Hermes2PushHandler) handler).getModelFactory()
								.getModel(DeviceTokenModel.class.getName());
					} else {
						this.deviceTokenModel = ((Hermes2RegisterHandler) handler).getModelFactory()
								.getModel(DeviceTokenModel.class.getName());
					}
				}
			}
		}
		return this.deviceTokenModel;
	}

	public PushTaskModel getPushTaskModel() {
		if (this.pushTaskModel == null) {
			synchronized (this) {
				if (this.pushTaskModel == null) {
					this.pushTaskModel = ((Hermes2PushHandler) handler).getModelFactory()
							.getModel(PushTaskModel.class.getName());
				}
			}
		}
		return this.pushTaskModel;
	}

	public PushSmsModel getPushSmsModel() {
		if (this.pushSmsModel == null) {
			synchronized (this) {
				if (this.pushSmsModel == null) {
					this.pushSmsModel = ((Hermes2PushHandler) handler).getModelFactory()
							.getModel(PushSmsModel.class.getName());
				}
			}
		}
		return this.pushSmsModel;
	}

	public SmsServiceModel getSmsServiceModel() {
		if (this.smsServiceModel == null) {
			synchronized (this) {
				if (this.smsServiceModel == null) {
					if (this.handler instanceof Hermes2PushHandler) {
						this.smsServiceModel = ((Hermes2PushHandler) handler).getModelFactory()
								.getModel(SmsServiceModel.class.getName());
					} else {
						this.smsServiceModel = ((Hermes2RegisterHandler) handler).getModelFactory()
								.getModel(SmsServiceModel.class.getName());
					}

				}
			}
		}
		return this.smsServiceModel;
	}

	public ServiceAuthenticatorModel getAuthenticatorModel() {
		if (this.authenticatorModel == null) {
			synchronized (this) {
				if (this.authenticatorModel == null) {
					if (this.handler instanceof Hermes2PushHandler) {
						this.authenticatorModel = ((Hermes2PushHandler) handler).getModelFactory()
								.getModel(ServiceAuthenticatorModel.class.getName());
					} else if(this.handler instanceof Hermes2RegisterHandler){
						this.authenticatorModel = ((Hermes2RegisterHandler) handler).getModelFactory()
								.getModel(ServiceAuthenticatorModel.class.getName());
					}else{
						this.authenticatorModel = ((Hermes2AdminHandler) handler).getModelFactory()
								.getModel(ServiceAuthenticatorModel.class.getName());
					}
				}
			}
		}
		return this.authenticatorModel;
	}

	protected boolean isFromPushHandler() {
		return this.handler instanceof Hermes2PushHandler ? true : false;
	}

	protected boolean isFromRegisterHandler() {
		return this.handler instanceof Hermes2RegisterHandler ? true : false;
	}

}
