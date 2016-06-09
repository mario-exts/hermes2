/*
 * Copyright Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gaia.hermes2.service.gcm;

import static com.google.android.gcm.server.Constants.GCM_SEND_ENDPOINT;
import static com.google.android.gcm.server.Constants.JSON_CANONICAL_IDS;
import static com.google.android.gcm.server.Constants.JSON_ERROR;
import static com.google.android.gcm.server.Constants.JSON_FAILURE;
import static com.google.android.gcm.server.Constants.JSON_MESSAGE_ID;
import static com.google.android.gcm.server.Constants.JSON_MULTICAST_ID;
import static com.google.android.gcm.server.Constants.JSON_REGISTRATION_IDS;
import static com.google.android.gcm.server.Constants.JSON_RESULTS;
import static com.google.android.gcm.server.Constants.JSON_SUCCESS;
import static com.google.android.gcm.server.Constants.TOKEN_CANONICAL_REG_ID;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.nhb.common.BaseLoggable;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.messaging.http.HttpAsyncFuture;
import com.nhb.messaging.http.HttpClientHelper;

public class GCMAsyncSender extends BaseLoggable implements Closeable {

	private static final String DEFAULT_CONTENT_TYPE = "application/json";

	private final String secretKey;
	private final HttpClientHelper httpClient;

	public GCMAsyncSender(String secretKey) {
		assert secretKey != null;
		this.secretKey = secretKey;
		this.httpClient = new HttpClientHelper();
		this.httpClient.setUsingMultipath(false);
	}

	public void send(Message message, List<String> regIds, Callback<MulticastResult> successCallback,
			Callback<Throwable> failureCallback) {

		assert message != null;
		assert regIds != null && regIds.size() > 0;
		assert successCallback != null;

		PuObject params = GCMHelper.convertMessageToPuObject(message);
		params.set(JSON_REGISTRATION_IDS, regIds);

		RequestBuilder builder = RequestBuilder.post(GCM_SEND_ENDPOINT).addHeader("Content-Type", DEFAULT_CONTENT_TYPE)
				.addHeader("Authorization", "key=" + this.secretKey).setCharset(Charset.forName("UTF-8"));

//		getLogger().debug("Sending message to " + regIds.size() + " device(s)");
		final HttpAsyncFuture future = this.httpClient.executeAsync(builder, params);

		future.setCallback(new Callback<HttpResponse>() {

			@Override
			public void apply(HttpResponse response) {
//				getLogger().debug("Sending complete with response: " + response);
				if (response == null) {
					if (failureCallback != null) {
						failureCallback.apply(future.getFailedCause());
					} else {
						getLogger().error("Sending message error", future.getFailedCause());
					}
				} else {
					PuElement puElement = HttpClientHelper.handleResponse(response);
					if (!(puElement instanceof PuObject) || response.getStatusLine().getStatusCode() != 200) {
						Exception exception = new Exception("An error responsed by GCM --> " + puElement.toString());
						if (failureCallback != null) {
							failureCallback.apply(exception);
						} else {
							getLogger().error("Sending message error", exception);
						}
					} else {
						PuObject result = (PuObject) puElement;
						int success = result.getInteger(JSON_SUCCESS);
						int failure = result.getInteger(JSON_FAILURE);
						int canonicalIds = result.getInteger(JSON_CANONICAL_IDS);
						long multicastId = result.getLong(JSON_MULTICAST_ID);

						MulticastResult.Builder builder = new MulticastResult.Builder(success, failure, canonicalIds,
								multicastId);

						PuArray results = result.getPuArray(JSON_RESULTS);
						if (results != null) {
							while (results.size() > 0) {
								Result.Builder resultObj = new Result.Builder();
								PuObject puo = results.remove(0).getPuObject();
								String messageId = puo.getString(JSON_MESSAGE_ID, "");
								String canonicalRegId = puo.getString(TOKEN_CANONICAL_REG_ID, "");
								String error = puo.getString(JSON_ERROR, "");
								if (messageId.length() > 0) {
									resultObj.messageId(messageId);
								}
								if (canonicalRegId.length() > 0) {
									resultObj.canonicalRegistrationId(canonicalRegId);
								}
								if (error.length() > 0) {
									resultObj.errorCode(error);
								}
								builder.addResult(resultObj.build());
							}
						}
						successCallback.apply(builder.build());
					}
				}
			}
		});
	}

	@Override
	public void close() throws IOException {
		this.httpClient.close();
	}
}
