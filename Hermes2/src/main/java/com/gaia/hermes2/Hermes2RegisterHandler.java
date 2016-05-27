package com.gaia.hermes2;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.bson.Document;

import com.gaia.hermes2.processor.Hermes2ProcessorResponseData;
import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseMessage;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
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

public class Hermes2RegisterHandler extends BaseMessageHandler {

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

		this.initController(initParams.getPuObject(F.COMMANDS, new PuObject()));

		initDatabase();
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
	public PuElement interop(PuElement requestParams) {
		PuObject request = (PuObject) requestParams;
		String command = request.getString(F.COMMAND, null);
		if (command != null) {
			try {
				BaseMessage message = new BaseMessage();
				message.setData(request);
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

	public ModelFactory getModelFactory() {
		return modelFactory;
	}

	public void setModelFactory(ModelFactory modelFactory) {
		this.modelFactory = modelFactory;
	}

	public void initDatabase() {
		createDatabaseIndexes(DBF.DATABASE_SERVICE_AUTHENTICATOR,
				new ArrayList<>(Arrays.asList(new Document().append(F.APPLICATION_ID, 1).append(F.CHECKSUM, 1))));

		createDatabaseIndexes(DBF.DATABASE_DEVICE_TOKEN,
				new ArrayList<>(Arrays.asList(new Document().append(F.ID, 1), new Document().append(F.CHECKSUM, 1),
						new Document().append(F.TOKEN, 1), new Document().append(F.APPLICATION_ID, 1),
						new Document().append(F.APPLICATION_ID, 1).append(F.SERVICE_TYPE, 1),
						new Document().append(F.APPLICATION_ID, 1).append(F.SERVICE_TYPE, 1).append(F.TOKEN, 1),
						new Document().append(F.APPLICATION_ID, 1).append(F.TOKEN, 1),
						new Document().append(F.SERVICE_TYPE, 1))));

		createDatabaseIndexes(DBF.DATABASE_DEVICE_TOKEN_SANDBOX,
				new ArrayList<>(Arrays.asList(new Document().append(F.ID, 1), new Document().append(F.CHECKSUM, 1),
						new Document().append(F.TOKEN, 1), new Document().append(F.APPLICATION_ID, 1),
						new Document().append(F.APPLICATION_ID, 1).append(F.SERVICE_TYPE, 1),
						new Document().append(F.APPLICATION_ID, 1).append(F.SERVICE_TYPE, 1).append(F.TOKEN, 1),
						new Document().append(F.APPLICATION_ID, 1).append(F.TOKEN, 1),
						new Document().append(F.SERVICE_TYPE, 1))));
	}

	public void createDatabaseIndexes(String connectionName, List<Document> tobeIndexed) {
		MongoCollection<Document> collection = mongoClient.getDatabase(DBF.MONGO_DATABASE_NAME)
				.getCollection(connectionName);
		for (Document index : collection.listIndexes()) {
			index = (Document) index.get(F.KEY);
			List<Integer> markToRemove = new ArrayList<>();
			for (int i = 0; i < tobeIndexed.size(); i++) {
				if (tobeIndexed.get(i).equals(index)) {
					markToRemove.add(i);
				}
			}
			if (markToRemove.size() > 0) {
				while (markToRemove.size() > 0) {
					tobeIndexed.remove(markToRemove.remove(markToRemove.size() - 1).intValue());
				}
			}
			if (tobeIndexed.size() == 0) {
				break;
			}
		}
		for (Document index : tobeIndexed) {
			getLogger().debug("create index: " + index);
			collection.createIndex(index);
		}
	}
}
