package com.gaia.hermes2.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.db.models.AbstractModel;

abstract public class HermesBaseModel extends AbstractModel {

	private String databaseName;
	private MongoDatabase database = null;

	@Override
	protected void init() {
		if (this.getEnvironmentVariables().containsKey(F.DATABASE_NAME)) {
			this.databaseName = (String) this.getEnvironmentVariables().get(F.DATABASE_NAME);
		} else {
			this.databaseName = DBF.MONGO_DATABASE_NAME;
		}
	}

	protected MongoDatabase getDatabase() {
		if (this.database == null) {
			synchronized (this) {
				if (this.database == null) {
					this.database = this.getMongoClient().getDatabase(this.databaseName);
				}
			}
		}
		return this.database;
	}

	public void createDatabaseIndexes(String connectionName, List<Document> tobeIndexed) {
		MongoCollection<Document> collection=getDatabase().getCollection(connectionName);
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
