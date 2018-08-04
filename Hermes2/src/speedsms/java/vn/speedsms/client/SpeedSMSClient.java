package vn.speedsms.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.RequestBuilder;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObject;
import com.nhb.messaging.http.HttpClientHelper;

import vn.speedsms.client.enums.SpeedSMSType;
import vn.speedsms.client.utils.AuthorizationUtils;

public class SpeedSMSClient extends BaseLoggable implements Closeable {

	public static final String SEND_SMS_URL = "http://api.speedsms.vn/index.php/sms/send";

	protected static final String POST = "POST";
	protected static final String AUTHORIZATION = "Authorization";

	private static final String TO = "to";
	private static final String CONTENT = "content";
	private static final String SMS_TYPE = "sms_type";
	private static final String BRAND_NAME = "brandname";

	private String basicAuth = null;
	private final String accessToken;
	private final HttpClientHelper httpClientHelper;

	public SpeedSMSClient(String accessToken) {
		this.accessToken = accessToken;
		this.httpClientHelper = new HttpClientHelper();
		this.httpClientHelper.setUsingMultipath(false);
	}

	private String getBasicAuth() {
		if (this.basicAuth == null && this.accessToken != null) {
			synchronized (this) {
				if (this.basicAuth == null) {
					this.basicAuth = AuthorizationUtils.generateBasicAuth(this.accessToken);
				}
			}
		}
		return this.basicAuth;
	}

	protected RequestBuilder createRequestBuilder() {
		RequestBuilder builder = RequestBuilder.create(POST);
		builder.setUri(SEND_SMS_URL);
		builder.addHeader(AUTHORIZATION, this.getBasicAuth());
		return builder;
	}

	public SpeedSMSSendingFuture send(String content, List<String> repicients, SpeedSMSType type, String brandName) {
		assert content != null;
		assert repicients != null && repicients.size() > 0;
		assert type != null;
		if (type == SpeedSMSType.BRAND_NAME && (brandName == null || brandName.trim().length() == 0)) {
			throw new RuntimeException("Brand name must be set while SMS Type == BRAND_NAME (type == 3)");
		}
		PuObject params = new PuObject();
		params.set(TO, repicients);
		params.set(CONTENT, content);
		params.set(SMS_TYPE, type.getType());
		if (type == SpeedSMSType.BRAND_NAME) {
			params.set(BRAND_NAME, brandName);
		}

		return new SpeedSMSSendingFuture(this.httpClientHelper.executeAsync(createRequestBuilder(), params));
	}

	public SpeedSMSSendingFuture send(String content, String[] repicients, SpeedSMSType type, String brandName) {
		List<String> listRepicients = new ArrayList<>();
		for (String str : repicients) {
			listRepicients.add(str);
		}
		return this.send(content, listRepicients, type, brandName);
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	@Override
	public void close() throws IOException {
		this.httpClientHelper.close();
	}
}
