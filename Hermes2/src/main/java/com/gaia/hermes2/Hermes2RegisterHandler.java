package com.gaia.hermes2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bson.Document;

import com.gaia.hermes2.processor.Hermes2ProcessorResponseData;
import com.gaia.hermes2.statics.F;
import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseMessage;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.strategy.CommandController;
import com.nhb.strategy.CommandProcessor;
import com.nhb.strategy.InvalidProcessorType;
import com.nhb.strategy.NoProcessorRegisteredException;

public class Hermes2RegisterHandler extends BaseMessageHandler {

	private MongoDatabase database;
	private final CommandController controller = new CommandController();

	@Override
	public void init(PuObjectRO initParams) {
		getLogger().debug("Initializing Hermes2Handler with params: " + initParams);

		MongoClient mongoClient = getApi().getMongoClient(initParams.getString(F.MONGODB));

		if (mongoClient == null) {
			throw new RuntimeException("MongoDB config cannot be found");
		}

		this.initDatabase(mongoClient.getDatabase(F.DATABASE_NAME));

		this.initController(initParams.getPuObject(F.COMMANDS, new PuObject()));
	}

	private void createDatabaseIndexes(MongoCollection<Document> collection, List<Document> tobeIndexed) {
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

	private void initDatabase(MongoDatabase database) {
		this.database = database;

		createDatabaseIndexes(this.database.getCollection(F.DATABASE_SERVICE_AUTHENTICATOR),
				new ArrayList<>(Arrays.asList(new Document().append(F.APPLICATION_ID, 1).append(F.CHECKSUM, 1))));

		createDatabaseIndexes(this.database.getCollection(F.DATABASE_DEVICE_TOKEN),
				new ArrayList<>(Arrays.asList(new Document().append(F.ID, 1), new Document().append(F.CHECKSUM, 1),
						new Document().append(F.APPLICATION_ID, 1), new Document().append(F.SERVICE_TYPE, 1))));

		createDatabaseIndexes(this.database.getCollection(F.DATABASE_DEVICE_TOKEN_SANDBOX),
				new ArrayList<>(Arrays.asList(new Document().append(F.ID, 1), new Document().append(F.CHECKSUM, 1),
						new Document().append(F.APPLICATION_ID, 1), new Document().append(F.SERVICE_TYPE, 1))));
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

	public MongoDatabase getDatabase() {
		return this.database;
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
}
