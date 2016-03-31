package com.gaia.hermes2.processor.register;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;

import com.gaia.hermes2.Hermes2RegisterHandler;
import com.gaia.hermes2.processor.Hermes2BaseProcessor;
import com.gaia.hermes2.statics.F;
import com.mario.entity.MessageHandler;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.encrypt.sha.SHAEncryptor;

public class BatchRegisterTokenProcessor extends Hermes2BaseProcessor {

	@Override
	protected PuElement process(MessageHandler handler, PuObjectRO data) {
		if (handler instanceof Hermes2RegisterHandler) {
			Hermes2RegisterHandler registerHandler = (Hermes2RegisterHandler) handler;
			MongoDatabase database = registerHandler.getDatabase();

			boolean sandbox = data.getBoolean(F.SANDBOX, false);
			MongoCollection<Document> collection = sandbox ? database.getCollection(F.DATABASE_DEVICE_TOKEN_SANDBOX)
					: database.getCollection(F.DATABASE_DEVICE_TOKEN);

			PuArray array = data.getPuArray(F.DEVICE_TOKENS);
			List<Document> batchDoc = new ArrayList<>();

			for (PuValue puValue : array) {
				PuObject deviceToken = puValue.getPuObject();

				String applicationId = deviceToken.getString(F.APPLICATION_ID);
				String authenticatorId = deviceToken.getString(F.AUTHENTICATOR_ID);
				String token = deviceToken.getString(F.TOKEN);
				String serviceType = deviceToken.getString(F.SERVICE_TYPE);

				String checksum = SHAEncryptor.sha512Hex(applicationId + token + authenticatorId);

				Document document = new Document();
				document.append(F.ID, UUID.randomUUID().toString());
				document.append(F.TOKEN, token);
				document.append(F.CHECKSUM, checksum);
				document.append(F.SERVICE_TYPE, serviceType);
				document.append(F.APPLICATION_ID, applicationId);
				document.append(F.AUTHENTICATOR_ID, authenticatorId);
				batchDoc.add(document);
			}

			collection.insertMany(batchDoc);
			String message = batchDoc.size() + " tokends was inserted";
			return PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.MESSAGE, message));
		}

		return null;
	}

}
