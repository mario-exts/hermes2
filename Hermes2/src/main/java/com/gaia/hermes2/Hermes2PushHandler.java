package com.gaia.hermes2;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.bson.types.Binary;

import com.gaia.hermes2.processor.Hermes2ProcessorResponseData;
import com.gaia.hermes2.service.Hermes2PushNotificationService;
import com.gaia.hermes2.statics.F;
import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseMessage;
import com.mario.statics.Fields;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.strategy.CommandController;
import com.nhb.strategy.CommandProcessor;
import com.nhb.strategy.InvalidProcessorType;
import com.nhb.strategy.NoProcessorRegisteredException;

public class Hermes2PushHandler extends BaseMessageHandler {

	private final Map<String, Properties> serviceProperties = new ConcurrentHashMap<>();
	private final Map<String, Class<? extends Hermes2PushNotificationService>> serviceHandleClasses = new ConcurrentHashMap<>();

	private MongoDatabase database;
	private final CommandController controller = new CommandController();

	@Override
	public void init(PuObjectRO initParams) {

		getLogger().debug("Initializing Hermes2Handler with params: " + initParams);

		MongoClient mongoClient = getApi().getMongoClient(initParams.getString(F.MONGODB));

		if (mongoClient == null) {
			throw new RuntimeException("MongoDB config cannot be found");
		}

		Properties serviceConfig = new Properties();
		String serviceConfigFile = initParams.getString(F.SERVICE_CONFIG_FILE);

		try (InputStream is = new FileInputStream(FileSystemUtils.createAbsolutePathFrom(
				System.getProperty(Fields.EXTENSION_FOLDER), getExtensionName(), serviceConfigFile))) {
			serviceConfig.load(is);
		} catch (Exception e) {
			throw new RuntimeException("Services configuration file invalid", e);
		}

		this.initServices(serviceConfig);
		this.initDatabase(mongoClient.getDatabase(F.DATABASE_NAME));
		this.initController(initParams.getPuObject(F.COMMANDS));
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
			}
		}
	}

	private void initDatabase(MongoDatabase mongoDatabase) {
		this.database = mongoDatabase;
	}

	@Override
	public PuElement handle(Message message) {
		getLogger().debug("Handling message: " + message.getData());
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

	public MongoDatabase getDatabase() {
		return this.database;
	}

	private final Map<String, Hermes2PushNotificationService> pushNotificationServices = new ConcurrentHashMap<>();

	public Hermes2PushNotificationService getPushService(String authenticatorId) {
		if (!pushNotificationServices.containsKey(authenticatorId)) {
			getLogger().debug("Service is not existing for authenticator id " + authenticatorId);
			synchronized (pushNotificationServices) {
				if (!pushNotificationServices.containsKey(authenticatorId)) {
					MongoCollection<Document> collection = this.database
							.getCollection(F.DATABASE_SERVICE_AUTHENTICATOR);
					Document criteria = new Document();
					criteria.append(F.ID, authenticatorId);
					FindIterable<Document> cursor = collection.find(criteria);
					MongoCursor<Document> it = cursor.iterator();
					if (it.hasNext()) {
						Document row = it.next();
						String serviceType = row.getString(F.SERVICE_TYPE);
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
								for (String key : row.keySet()) {
									if (key.equals("_id")) {
										continue;
									} else if (key.equals(F.AUTHENTICATOR)) {
										Binary bytes = row.get(F.AUTHENTICATOR, Binary.class);
										applicationConfig.setRaw(F.AUTHENTICATOR, bytes.getData());
									} else {
										applicationConfig.set(key, row.get(key));
									}
								}
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
}
