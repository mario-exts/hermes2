package vn.speedsms.client;

import org.apache.http.client.methods.RequestBuilder;

public class SpeedSMSProxiedClient extends SpeedSMSClient {

	private final String url;

	public SpeedSMSProxiedClient(String url) {
		super(null);
		this.url = url;
	}

	@Override
	protected RequestBuilder createRequestBuilder() {
		RequestBuilder builder = RequestBuilder.create(POST);
		builder.setUri(this.url);
		return builder;
	}

}
