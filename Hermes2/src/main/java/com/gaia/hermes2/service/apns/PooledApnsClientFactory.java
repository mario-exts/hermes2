package com.gaia.hermes2.service.apns;

import static com.gaia.hermes2.service.apns.Hermes2APNSService.APNS_HOST;
import static com.gaia.hermes2.service.apns.Hermes2APNSService.APNS_HOST_SANDBOX;
import static com.gaia.hermes2.service.apns.Hermes2APNSService.APNS_PORT;
import static com.gaia.hermes2.service.apns.Hermes2APNSService.APNS_PORT_SANDBOX;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gaia.hermes2.statics.F;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuObjectRO;
import com.relayrides.pushy.apns.ApnsClient;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;

class PooledApnsClientFactory extends BasePooledObjectFactory<ApnsClient<NotificationItem>> implements Loggable {

	private final PuObjectRO clientConfig;
	private final PuObjectRO applicationConfig;
	private final NioEventLoopGroup nioEventLoopGroup;

	PooledApnsClientFactory(PuObjectRO clientConfig, PuObjectRO applicationConfig,
			NioEventLoopGroup nioEventLoopGroup) {
		this.clientConfig = clientConfig;
		this.applicationConfig = applicationConfig;
		this.nioEventLoopGroup = nioEventLoopGroup;
	}

	@Override
	public Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	@Override
	public Logger getLogger(String name) {
		return LoggerFactory.getLogger(name);
	}

	@Override
	public ApnsClient<NotificationItem> create() throws Exception {
		byte[] authenticator = applicationConfig.getRaw(F.AUTHENTICATOR);
		String password = applicationConfig.getString(F.PASSWORD, "");
		try (InputStream is = new ByteArrayInputStream(authenticator)) {
			ApnsClient<NotificationItem> client = new ApnsClient<>(is, password == null ? "" : password,
					this.nioEventLoopGroup);
			return client;
		} catch (IOException e) {
			throw new RuntimeException("Unable to read authenticator", e);
		}
	}

	@Override
	public PooledObject<ApnsClient<NotificationItem>> wrap(ApnsClient<NotificationItem> client) {
		return new PooledApnsClient(client);
	}

	@Override
	public void destroyObject(PooledObject<ApnsClient<NotificationItem>> p) throws Exception {
		p.getObject().disconnect();
	}

	@Override
	public boolean validateObject(PooledObject<ApnsClient<NotificationItem>> p) {
		return p.getObject().isConnected();
	}

	@Override
	public void activateObject(PooledObject<ApnsClient<NotificationItem>> p) throws Exception {

		ApnsClient<NotificationItem> client = p.getObject();
		boolean isSandbox = this.applicationConfig.getBoolean(F.SANDBOX);
		String host = this.clientConfig.getString(isSandbox ? APNS_HOST_SANDBOX : APNS_HOST);

		final Future<Void> future;
		if (this.clientConfig.variableExists(isSandbox ? APNS_PORT_SANDBOX : APNS_PORT)) {
			int port = this.clientConfig.getInteger(isSandbox ? APNS_PORT_SANDBOX : APNS_PORT);
			getLogger().debug("connecting to apple push gateway via: {}:{} ", host, port);
			future = client.connect(host, port);
		} else {
			getLogger().debug("connecting to apple push gateway via: {} (default port) ", host);
			future = client.connect(host);
		}

		future.await();
		if (!future.isSuccess()) {
			throw new RuntimeException("Connect to APNS gateway has failed", future.cause());
		}
	}

}
