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

public class RegisterTokenProcessor extends Hermes2BaseProcessor {

	@Override
	protected PuElement process(MessageHandler handler, PuObjectRO data) {
		if (handler instanceof Hermes2RegisterHandler) {
			Hermes2RegisterHandler registerHandler = (Hermes2RegisterHandler) handler;
			MongoDatabase database = registerHandler.getDatabase();

			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			MongoCollection<Document> collection = sandbox ? database.getCollection(F.DATABASE_DEVICE_TOKEN_SANDBOX)
					: database.getCollection(F.DATABASE_DEVICE_TOKEN);

			String applicationId = data.getString(F.APPLICATION_ID);
			String authenticatorId = data.getString(F.AUTHENTICATOR_ID);
			String token = data.getString(F.TOKEN);
			String serviceType = data.getString(F.SERVICE_TYPE);

			String checksum = SHAEncryptor.sha512Hex(applicationId + token + authenticatorId);

			Document criteria = new Document();
			criteria.append(F.CHECKSUM, checksum);
			FindIterable<Document> finder = collection.find(criteria);
			MongoCursor<Document> it = finder.iterator();
			if (it.hasNext()) {
				return PuObject.fromObject(
						new MapTuple<>(F.STATUS, 1, F.DESCRIPTION, "Duplicate token", F.ID, it.next().get(F.ID)));
			}

			Document document = new Document();
			document.append(F.ID, UUID.randomUUID().toString());
			document.append(F.TOKEN, token);
			document.append(F.CHECKSUM, checksum);
			document.append(F.SERVICE_TYPE, serviceType);
			document.append(F.APPLICATION_ID, applicationId);
			document.append(F.AUTHENTICATOR_ID, authenticatorId);

			collection.insertOne(document);

			return PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.ID, document.get(F.ID), F.DESCRIPTION,
					"The ID use for push notification later"));
		}
		return null;
	}
}
