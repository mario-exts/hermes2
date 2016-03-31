package com.gaia.hermes2.service.apns;

import static com.gaia.hermes2.service.apns.Hermes2APNSService.*;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.nhb.common.data.PuObjectRO;
import com.relayrides.pushy.apns.ApnsClient;

import io.netty.channel.nio.NioEventLoopGroup;

public class ApnsClientPool extends GenericObjectPool<ApnsClient<NotificationItem>> {

	public ApnsClientPool(PuObjectRO clientConfig, PuObjectRO applicationConfig, NioEventLoopGroup nioEventLoopGroup) {
		super(new PooledApnsClientFactory(clientConfig, applicationConfig, nioEventLoopGroup));
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(clientConfig.getInteger(APNS_MAX_CONNECTION, DEFAULT_MAX_CONNECTION));
		config.setMaxIdle(clientConfig.getInteger(APNS_MAX_IDLE, DEFAULT_MAX_IDLE));
		this.setConfig(config);
	}
}
