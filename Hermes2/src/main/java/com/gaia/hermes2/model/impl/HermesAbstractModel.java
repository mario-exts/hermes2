package com.gaia.hermes2.model.impl;

import com.gaia.hermes2.statics.DBF;
import com.gaia.hermes2.statics.F;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.db.models.AbstractModel;

abstract public class HermesAbstractModel extends AbstractModel {

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

}
