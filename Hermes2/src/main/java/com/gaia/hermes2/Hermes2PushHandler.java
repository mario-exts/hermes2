package com.gaia.hermes2;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.gaia.hermes2.bean.ServiceAuthenticatorBean;
import com.gaia.hermes2.bean.SmsServiceBean;
import com.gaia.hermes2.model.ServiceAuthenticatorModel;
import com.gaia.hermes2.model.impl.SmsServiceModelImpl;
import com.gaia.hermes2.processor.Hermes2ProcessorResponseData;
import com.gaia.hermes2.service.Hermes2PushNotificationService;
import com.gaia.hermes2.service.Hermes2SmsService;
import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseMessage;
import com.mario.statics.Fields;
import com.mongodb.MongoClient;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.db.models.ModelFactory;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.strategy.CommandController;
import com.nhb.strategy.CommandProcessor;
import com.nhb.strategy.InvalidProcessorType;
import com.nhb.strategy.NoProcessorRegisteredException;

public class Hermes2PushHandler extends BaseMessageHandler {

	private final Map<String, Properties> serviceProperties = new ConcurrentHashMap<>();
	private final Map<String, Class<? extends Hermes2PushNotificationService>> serviceHandleClasses = new ConcurrentHashMap<>();
	private final Map<String, Class<? extends Hermes2SmsService>> smsServiceHandleClasses = new ConcurrentHashMap<>();
	private final CommandController controller = new CommandController();
	private ModelFactory modelFactory;
	private MongoClient mongoClient;

	@Override
	public void init(PuObjectRO initParams) {

		getLogger().debug("Initializing Hermes2Handler with params: " + initParams);

		mongoClient = getApi().getMongoClient(initParams.getString(F.MONGODB));

		if (mongoClient == null) {
			throw new RuntimeException("MongoDB config cannot be found");
		}

		String dbName = initParams.getString(F.DATABASE_NAME, DBF.MONGO_DATABASE_NAME);

		initModelFactory(initParams.getString(F.MODEL_MAPPING_FILE, null), dbName);

		Properties serviceConfig = new Properties();
		String serviceConfigFile = initParams.getString(F.SERVICE_CONFIG_FILE);

		try (InputStream is = new FileInputStream(FileSystemUtils.createAbsolutePathFrom(
				System.getProperty(Fields.EXTENSION_FOLDER), getExtensionName(), serviceConfigFile))) {
			serviceConfig.load(is);
		} catch (Exception e) {
			throw new RuntimeException("Services configuration file invalid", e);
		}

		this.initServices(serviceConfig);

		this.initController(initParams.getPuObject(F.COMMANDS));
	}

	private void initModelFactory(String filePath, String databaseName) {
		modelFactory = new ModelFactory();
		modelFactory.setMongoClient(this.mongoClient);
		modelFactory.setClassLoader(this.getClass().getClassLoader());
		modelFactory.setEnvironmentVariable(F.DATABASE_NAME, databaseName);
		if (filePath != null) {
			Properties props = new Properties();
			try (InputStream is = new FileInputStream(
					FileSystemUtils.createAbsolutePathFrom("extensions", getExtensionName(), filePath))) {
				props.load(is);
				modelFactory.addClassImplMapping(props);
			} catch (Exception e) {
				throw new RuntimeException("An error occurs while loading model mapping file", e);
			}
		}
	}

	private void initController(PuObject commands) {
		this.controller.setEnviroiment(F.HANDLER, this);
		for (Entry<String, PuValue> entry : commands) {
			String command = entry.getKey();
			String clazz = entry.getValue().getString();
			try {
				this.controller.registerCommand(command,
						(CommandProcessor) this.getClass().getClassLoader().loadClass(clazz).newInstance());
			} catch (ClassNotFoundException | InvalidProcessorType | InstantiationException
					| IllegalAccessException e) {
				throw new RuntimeException("Invalid processor class", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initServices(Properties serviceConfig) {
		for (Object objKey : serviceConfig.keySet()) {
			String key = String.valueOf(objKey);
			String serviceType = key.split("\\.")[1];
			if (key.startsWith(F.SERVICE)) {
				try {
					this.serviceHandleClasses.put(serviceType, (Class<? extends Hermes2PushNotificationService>) this
							.getClass().getClassLoader().loadClass(serviceConfig.getProperty(key)));
				} catch (Exception e) {
					throw new RuntimeException("Class not found or invalid type for service handler: " + key + " --> "
							+ serviceConfig.getProperty(key), e);
				}
			} else if (key.startsWith(F.PROPERTIES)) {
				Properties properties = new Properties();
				try (InputStream is = new FileInputStream(
						FileSystemUtils.createAbsolutePathFrom(System.getProperty(Fields.EXTENSION_FOLDER),
								getExtensionName(), serviceConfig.getProperty(key)))) {
					properties.load(is);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				serviceProperties.put(serviceType, properties);
			}else if(key.startsWith(F.SMS_SERVICE)){
				try {
					this.smsServiceHandleClasses.put(serviceType, (Class<? extends Hermes2SmsService>) this
							.getClass().getClassLoader().loadClass(serviceConfig.getProperty(key)));
				} catch (Exception e) {
					throw new RuntimeException("Class not found or invalid type for service handler: " + key + " --> "
							+ serviceConfig.getProperty(key), e);
				}
			}
		}
	}
	
	@Override
	public PuElement handle(Message message) {
		getLogger().debug("Handling message: " + message.getData());
		if (message.getData() != null) {
			PuObject request = (PuObject) message.getData();
			String command = request.getString(F.COMMAND, null);
			if (command != null) {
				try {
					Hermes2ProcessorResponseData result = (Hermes2ProcessorResponseData) this.controller
							.processCommand(command, message);
					if (result != null) {
						return result.getResult();
					}
				} catch (InstantiationException | IllegalAccessException | NoProcessorRegisteredException
						| InvalidProcessorType e) {
					throw new RuntimeException("Error while handling message", e);
				}
			}
			return new PuValue("Missing command");
		}
		return new PuValue("Request data is null");
	}

	@Override
	public PuElement interop(PuElement params) {
		PuObject request = (PuObject) params;
		String command = request.getString(F.COMMAND, null);
		if (command != null) {
			try {
				BaseMessage baseMessage = new BaseMessage();
				baseMessage.setData(request);
				Hermes2ProcessorResponseData result = (Hermes2ProcessorResponseData) this.controller
						.processCommand(command, baseMessage);
				if (result != null) {
					return result.getResult();
				}
			} catch (InstantiationException | IllegalAccessException | NoProcessorRegisteredException
					| InvalidProcessorType e) {
				throw new RuntimeException("Error while handling message", e);
			}
		}
		return new PuValue("Missing command");
	}

	private final Map<String, Hermes2PushNotificationService> pushNotificationServices = new ConcurrentHashMap<>();
	public Hermes2PushNotificationService getPushService(String authenticatorId) {
		if (!pushNotificationServices.containsKey(authenticatorId)) {
			getLogger().debug("Service is not existing for authenticator id " + authenticatorId);
			synchronized (pushNotificationServices) {
				if (!pushNotificationServices.containsKey(authenticatorId)) {
					ServiceAuthenticatorModel model = getModelFactory()
							.getModel(ServiceAuthenticatorModel.class.getName());
					ServiceAuthenticatorBean bean = model.findById(authenticatorId);
					if (bean != null) {
						String serviceType = bean.getServiceType();
						Class<? extends Hermes2PushNotificationService> clazz = this.serviceHandleClasses
								.get(serviceType);
						if (clazz != null) {
							try {
								Hermes2PushNotificationService instance = clazz.newInstance();
								PuObject applicationConfig = new PuObject();
								PuObject clientConfig = new PuObject();
								if (this.serviceProperties.containsKey(serviceType)) {
									Properties props = this.serviceProperties.get(serviceType);
									for (Object obj : props.keySet()) {
										String key = String.valueOf(obj);
										clientConfig.setString(key, props.getProperty(key));
									}
								}
								applicationConfig = bean.toPuObject();
								PuObject initParams = new PuObject();
								initParams.setPuObject(F.CLIENT_CONFIG, clientConfig);
								initParams.setPuObject(F.APPLICATION_CONFIG, applicationConfig);
								instance.init(initParams);

								this.pushNotificationServices.put(authenticatorId, instance);

							} catch (InstantiationException | IllegalAccessException e) {
								getLogger().error(
										"Error while creating new service instance for service type " + serviceType, e);
								throw new RuntimeException(
										"Error while creating new service instance for service type " + serviceType, e);
							}
						}
					}

				}
			}
		}
		return pushNotificationServices.get(authenticatorId);
	}
	
	private Map<String,Hermes2SmsService> smsServices=new ConcurrentHashMap<>();
	
	public Hermes2SmsService getSmsService(String serviceId){
		if (!smsServices.containsKey(serviceId)) {
			getLogger().debug("Service is not existing for authenticator id " + serviceId);
			synchronized (smsServices) {
				if (!smsServices.containsKey(serviceId)) {
					SmsServiceModelImpl model = getModelFactory()
							.getModel(SmsServiceModelImpl.class.getName());
					SmsServiceBean bean = model.findById(serviceId);
					if (bean != null) {
						String serviceType = bean.getServiceName();
						Class<? extends Hermes2SmsService> clazz = this.smsServiceHandleClasses
								.get(serviceType);
						if (clazz != null) {
							try {
								Hermes2SmsService instance = clazz.newInstance();
								PuObject applicationConfig = new PuObject();
								PuObject clientConfig = new PuObject();
								if (this.serviceProperties.containsKey(serviceType)) {
									Properties props = this.serviceProperties.get(serviceType);
									for (Object obj : props.keySet()) {
										String key = String.valueOf(obj);
										clientConfig.setString(key, props.getProperty(key));
									}
								}
								applicationConfig = bean.toPuObject();
								PuObject initParams = new PuObject();
								initParams.setPuObject(F.CLIENT_CONFIG, clientConfig);
								initParams.setPuObject(F.APPLICATION_CONFIG, applicationConfig);
								instance.init(initParams);

								this.smsServices.put(serviceId, instance);

							} catch (InstantiationException | IllegalAccessException e) {
								getLogger().error(
										"Error while creating new service instance for service type " + serviceType, e);
								throw new RuntimeException(
										"Error while creating new service instance for service type " + serviceType, e);
							}
						}
					}

				}
			}
		}
		return smsServices.get(serviceId);
	}
	
	public Hermes2SmsService getDefaultSmsService(){
		SmsServiceModelImpl model=getModelFactory().getModel(SmsServiceModelImpl.class.getName());
		SmsServiceBean bean = model.findDefault();
		if(bean!=null){
			return getSmsService(bean.getId());
		}
		return null;
	}
	
	@Override
	public void destroy() throws Exception {
		for (Hermes2PushNotificationService service : this.pushNotificationServices.values()) {
			try {
				service.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				getLogger().error("error while closing service " + service.getClass().getName(), ex);
			}
		}
	}

	public ModelFactory getModelFactory() {
		return modelFactory;
	}

	public void setModelFactory(ModelFactory modelFactory) {
		this.modelFactory = modelFactory;
	}

}
