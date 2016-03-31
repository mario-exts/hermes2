package com.gaia.hermes2.processor.register;

import java.util.UUID;

import org.bson.Document;

import com.gaia.hermes2.Hermes2RegisterHandler;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.statics.F;
import com.mario.entity.MessageHandler;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class AddAuthenticatorProcessor extends Hermes2BaseProcessor {

	@Override
	protected PuElement process(MessageHandler handler, PuObjectRO data) {
		if (handler instanceof Hermes2RegisterHandler) {
			Hermes2RegisterHandler registerHandler = (Hermes2RegisterHandler) handler;
			MongoDatabase database = registerHandler.getDatabase();
			MongoCollection<Document> collection = database.getCollection(F.DATABASE_SERVICE_AUTHENTICATOR);

			String applicationId = data.getString(F.APPLICATION_ID);
			String serviceType = data.getString(F.SERVICE_TYPE);
			byte[] authenticator = data.getRaw(F.AUTHENTICATOR);
			String password = data.getString(F.PASSWORD, "");
			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			String topic = data.getString(F.TOPIC, null);

			String checksum = SHAEncryptor.sha512Hex(new String(authenticator) + String.valueOf(sandbox));

			Document criteria = new Document();
			criteria.append(F.APPLICATION_ID, applicationId);
			criteria.append(F.CHECKSUM, checksum);
			FindIterable<Document> finder = collection.find(criteria);
			MongoCursor<Document> it = finder.iterator();
			if (it.hasNext()) {
				return PuObject.fromObject(new MapTuple<>(F.STATUS, 1, F.DESCRIPTION, "Authenticator is existing",
						F.AUTHENTICATOR_ID, it.next().get(F.ID)));
			}

			Document document = new Document();
			document.append(F.ID, UUID.randomUUID().toString());
			document.append(F.APPLICATION_ID, applicationId);
			document.append(F.SERVICE_TYPE, serviceType);
			document.append(F.AUTHENTICATOR, authenticator);
			document.append(F.PASSWORD, password);
			document.append(F.CHECKSUM, checksum);
			document.append(F.SANDBOX, sandbox);
			document.append(F.TOPIC, topic);

			collection.insertOne(document);

			return PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.AUTHENTICATOR_ID, document.get(F.ID)));
		}
		return null;
	}

}
