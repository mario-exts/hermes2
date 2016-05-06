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
package com.google.android.gcm.server;

import static com.google.android.gcm.server.Constants.GCM_SEND_ENDPOINT;
import static com.google.android.gcm.server.Constants.JSON_CANONICAL_IDS;
import static com.google.android.gcm.server.Constants.JSON_ERROR;
import static com.google.android.gcm.server.Constants.JSON_FAILURE;
import static com.google.android.gcm.server.Constants.JSON_MESSAGE_ID;
import static com.google.android.gcm.server.Constants.JSON_MULTICAST_ID;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_BADGE;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_BODY;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_BODY_LOC_ARGS;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_BODY_LOC_KEY;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_CLICK_ACTION;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_COLOR;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_ICON;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_SOUND;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_TAG;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_TITLE;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_TITLE_LOC_ARGS;
import static com.google.android.gcm.server.Constants.JSON_NOTIFICATION_TITLE_LOC_KEY;
import static com.google.android.gcm.server.Constants.JSON_PAYLOAD;
import static com.google.android.gcm.server.Constants.JSON_REGISTRATION_IDS;
import static com.google.android.gcm.server.Constants.JSON_RESULTS;
import static com.google.android.gcm.server.Constants.JSON_SUCCESS;
import static com.google.android.gcm.server.Constants.JSON_TO;
import static com.google.android.gcm.server.Constants.PARAM_COLLAPSE_KEY;
import static com.google.android.gcm.server.Constants.PARAM_CONTENT_AVAILABLE;
import static com.google.android.gcm.server.Constants.PARAM_DELAY_WHILE_IDLE;
import static com.google.android.gcm.server.Constants.PARAM_DRY_RUN;
import static com.google.android.gcm.server.Constants.PARAM_PRIORITY;
import static com.google.android.gcm.server.Constants.PARAM_RESTRICTED_PACKAGE_NAME;
import static com.google.android.gcm.server.Constants.PARAM_TIME_TO_LIVE;
import static com.google.android.gcm.server.Constants.TOKEN_CANONICAL_REG_ID;
import static com.google.android.gcm.server.Constants.TOPIC_PREFIX;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;

import com.nhb.common.async.Callback;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.messaging.http.HttpAsyncFuture;
import com.nhb.messaging.http.HttpClientHelper;

/**
 * Helper class to send messages to the GCM service using an API Key.
 */
public class AsyncSender {

	protected static final String UTF8 = "UTF-8";

	/**
	 * Initial delay before first retry, without jitter.
	 */
	protected static final int BACKOFF_INITIAL_DELAY = 1000;
	/**
	 * Maximum delay before a retry.
	 */
	protected static final int MAX_BACKOFF_DELAY = 1024000;

	protected final Random random = new Random();
	protected static final Logger logger = Logger.getLogger(AsyncSender.class.getName());

	private final String key;
	private HttpClientHelper http;
	private ExecutorService callbackExecutor;

	/**
	 * Default constructor.
	 *
	 * @param key
	 *            API key obtained through the Google API Console.
	 */
	public AsyncSender(String key) {
		this.key = nonNull(key);
		this.callbackExecutor = Executors.newCachedThreadPool();
	}

	/**
	 * Sends a message to one device, retrying in case of unavailability.
	 *
	 * <p>
	 * <strong>Note: </strong> this method uses exponential back-off to retry in
	 * case of service unavailability and hence could block the calling thread
	 * for many seconds.
	 *
	 * @param message
	 *            message to be sent, including the device's registration id.
	 * @param to
	 *            registration token, notification key, or topic where the
	 *            message will be sent.
	 * @param retries
	 *            number of retries in case of service unavailability errors.
	 *
	 * @return result of the request (see its javadoc for more details).
	 *
	 * @throws IllegalArgumentException
	 *             if to is {@literal null}.
	 * @throws InvalidRequestException
	 *             if GCM didn't returned a 200 or 5xx status.
	 * @throws IOException
	 *             if message could not be sent.
	 */
	public void send(Message message, String to, int retries, Callback<Result> callback) throws IOException {
		int attempt = 0;
		int backoff = BACKOFF_INITIAL_DELAY;
		AtomicInteger counter = new AtomicInteger(0);

		Callback<Result> sendCallBack = new Callback<Result>() {

			@Override
			public void apply(Result result) {
				Callback<Result> thisCallback = this;
				callbackExecutor.execute(new Runnable() {

					@Override
					public void run() {
						if (null == result) {
							if (counter.incrementAndGet() <= retries) {
								if (logger.isLoggable(Level.FINE)) {
									logger.fine(
											"Attempt #" + attempt + " to send message " + message + " to regIds " + to);
								}
								try {
									sendNoRetry(message, to, thisCallback);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								int sleepTime = backoff / 2 + random.nextInt(backoff);
								sleep(sleepTime);
								// if (2 * backoff < MAX_BACKOFF_DELAY) {
								// backoff *= 2;
								// }
							}
						} else {
							callback.apply(result);
						}
					}
				});

			}

		};
		sendNoRetry(message, to, sendCallBack);

	}

	/**
	 * Sends a message without retrying in case of service unavailability. See
	 * {@link #send(Message, String, int)} for more info.
	 *
	 * @return result of the post, or {@literal null} if the GCM service was
	 *         unavailable or any network exception caused the request to fail,
	 *         or if the response contains more than one result.
	 *
	 * @throws InvalidRequestException
	 *             if GCM didn't returned a 200 status.
	 * @throws IllegalArgumentException
	 *             if to is {@literal null}.
	 */
	public void sendNoRetry(Message message, String to, Callback<Result> callback) throws IOException {
		nonNull(to);
		PuObject jsonRequest = new PuObject();
		messageToPuObject(message, jsonRequest);
		jsonRequest.set(JSON_TO, to);
		Callback<PuObject> postCallback = new Callback<PuObject>() {

			@Override
			public void apply(PuObject data) {
				// TODO Auto-generated method stub
				callbackExecutor.execute(new Runnable() {
					@Override
					public void run() {
						Result.Builder resultBuilder = new Result.Builder();

						if (data.variableExists("results")) {
							PuArray arr = data.getPuArray("results");
							if (arr.size() >= 1) {
								PuObject puo = arr.remove(0).getPuObject();
								String messageId = puo.getString(JSON_MESSAGE_ID, "");
								String canonicalRegId = puo.getString(TOKEN_CANONICAL_REG_ID, "");
								String error = puo.getString(JSON_ERROR, "");
								if (messageId.length() > 0) {
									resultBuilder.messageId(messageId);
								}
								if (canonicalRegId.length() > 0) {
									resultBuilder.canonicalRegistrationId(canonicalRegId);
								}
								if (error.length() > 0) {
									resultBuilder.errorCode(error);
								}
								callback.apply(resultBuilder.build());
							} else {
								logger.log(Level.WARNING, "Found null or " + arr.size() + " results, expected one");
								callback.apply(null);
								return;
							}
						} else if (to.startsWith(TOPIC_PREFIX)) {
							if (data.variableExists(JSON_MESSAGE_ID)) {
								// message_id is expected when this is the
								// response from a
								// topic message.
								String messageId = data.getString(JSON_MESSAGE_ID, "");
								resultBuilder.messageId(messageId);
								callback.apply(resultBuilder.build());
							} else if (data.variableExists(JSON_ERROR)) {
								String error = data.get(JSON_ERROR);
								resultBuilder.errorCode(error);
								callback.apply(resultBuilder.build());
							} else {
								logger.log(Level.WARNING, "Expected " + JSON_MESSAGE_ID + " or " + JSON_ERROR
										+ " found: " + data.toJSON());
								callback.apply(null);
								return;
							}
						} else if (data.variableExists(JSON_SUCCESS) && data.variableExists(JSON_FAILURE)) {
							// success and failure are expected when response is
							// from group
							// message.
							int success = data.getInteger(JSON_SUCCESS);
							int failure = data.getInteger(JSON_FAILURE);
							List<String> failedIds = null;
							if (data.variableExists("failed_registration_ids")) {
								PuArray jFailedIds = data.getPuArray("failed_registration_ids");
								failedIds = new ArrayList<String>();
								while (jFailedIds.size() > 0) {
									failedIds.add(jFailedIds.remove(0).getString());

								}
							}
							resultBuilder.success(success).failure(failure).failedRegistrationIds(failedIds);
							callback.apply(resultBuilder.build());
						} else {
							logger.warning("Unrecognized response: " + data.toJSON());
							// throw new IOException(data.toJSON());
							callback.apply(null);

						}
					}
				});

			}
		};
		try {
			post(GCM_SEND_ENDPOINT, "application/json", jsonRequest.toJSON(), postCallback);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends a message to many devices, retrying in case of unavailability.
	 *
	 * <p>
	 * <strong>Note: </strong> this method uses exponential back-off to retry in
	 * case of service unavailability and hence could block the calling thread
	 * for many seconds.
	 *
	 * @param message
	 *            message to be sent.
	 * @param regIds
	 *            registration id of the devices that will receive the message.
	 * @param retries
	 *            number of retries in case of service unavailability errors.
	 *
	 * @return combined result of all requests made.
	 *
	 * @throws IllegalArgumentException
	 *             if registrationIds is {@literal null} or empty.
	 * @throws InvalidRequestException
	 *             if GCM didn't returned a 200 or 503 status.
	 * @throws IOException
	 *             if message could not be sent.
	 */
	public void send(Message message, List<String> regIds, int retries, Callback<MulticastResult> callback)
			throws IOException {
		int backoff = BACKOFF_INITIAL_DELAY;
		// Map of results by registration id, it will be updated after each
		// attempt
		// to send the messages
		Map<String, Result> results = new HashMap<String, Result>();
		AtomicInteger counter = new AtomicInteger(0);

		Callback<MulticastResult> sendCallBack = new Callback<MulticastResult>() {

			@Override
			public void apply(MulticastResult multicastResult) {
				Callback<MulticastResult> thisCallback = this;
				callbackExecutor.execute(new Runnable() {

					@Override
					public void run() {
						boolean isRetry = false;
						List<String> unsentRegIds = new ArrayList<String>(regIds);
						long multicastId = 0;
						List<Long> multicastIds = new ArrayList<Long>();
						if (null == multicastResult) {
							if (counter.incrementAndGet() <= retries) {
								isRetry = true;
							}
						} else {
							multicastId = multicastResult.getMulticastId();
							logger.fine("multicast_id on attempt # " + counter.get() + ": " + multicastId);
							multicastIds.add(multicastId);
							unsentRegIds = updateStatus(unsentRegIds, results, multicastResult);
							isRetry = !unsentRegIds.isEmpty() && counter.get() <= retries;
						}
						if (isRetry) {
							if (logger.isLoggable(Level.FINE)) {
								logger.fine("Attempt #" + counter.get() + " to send message " + message + " to regIds "
										+ regIds.size());
							}
							try {
								sendNoRetry(message, regIds, thisCallback);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							int sleepTime = backoff / 2 + random.nextInt(backoff);
							sleep(sleepTime);
							// if (2 * backoff < MAX_BACKOFF_DELAY) {
							// backoff *= 2;
							// }
						} else {
							if (multicastIds.isEmpty()) {
								// all JSON posts failed due to GCM
								// unavailability
								callback.apply(null);
							}
							// calculate summary
							int success = 0, failure = 0, canonicalIds = 0;
							for (Result re : results.values()) {
								if (re.getMessageId() != null) {
									success++;
									if (re.getCanonicalRegistrationId() != null) {
										canonicalIds++;
									}
								} else {
									failure++;
								}
							}
							// build a new object with the overall result
							multicastId = multicastIds.remove(0);
							MulticastResult.Builder builder = new MulticastResult.Builder(success, failure,
									canonicalIds, multicastId).retryMulticastIds(multicastIds);
							// add results, in the same order as the input
							for (String regId : regIds) {
								Result re = results.get(regId);
								builder.addResult(re);
							}
							callback.apply(builder.build());
						}
					}
				});

			}

		};
		sendNoRetry(message, regIds, sendCallBack);

	}

	/**
	 * Updates the status of the messages sent to devices and the list of
	 * devices that should be retried.
	 *
	 * @param unsentRegIds
	 *            list of devices that are still pending an update.
	 * @param allResults
	 *            map of status that will be updated.
	 * @param multicastResult
	 *            result of the last multicast sent.
	 *
	 * @return updated version of devices that should be retried.
	 */
	private List<String> updateStatus(List<String> unsentRegIds, Map<String, Result> allResults,
			MulticastResult multicastResult) {
		List<Result> results = multicastResult.getResults();
		if (results.size() != unsentRegIds.size()) {
			// should never happen, unless there is a flaw in the algorithm
			throw new RuntimeException("Internal error: sizes do not match. " + "currentResults: " + results
					+ "; unsentRegIds: " + unsentRegIds);
		}
		List<String> newUnsentRegIds = new ArrayList<String>();
		for (int i = 0; i < unsentRegIds.size(); i++) {
			String regId = unsentRegIds.get(i);
			Result result = results.get(i);
			allResults.put(regId, result);
			String error = result.getErrorCodeName();
			if (error != null && (error.equals(Constants.ERROR_UNAVAILABLE)
					|| error.equals(Constants.ERROR_INTERNAL_SERVER_ERROR))) {
				newUnsentRegIds.add(regId);
			}
		}
		return newUnsentRegIds;
	}

	/**
	 * Sends a message without retrying in case of service unavailability. See
	 * {@link #send(Message, List, int)} for more info.
	 *
	 * @return multicast results if the message was sent successfully,
	 *         {@literal null} if it failed but could be retried.
	 *
	 * @throws IOException
	 *             if there was a JSON parsing error
	 */
	public void sendNoRetry(Message message, List<String> registrationIds, Callback<MulticastResult> sendCallback)
			throws IOException {
		if (nonNull(registrationIds).isEmpty()) {
			throw new IllegalArgumentException("registrationIds cannot be empty");
		}
		PuObject jsonRequest = new PuObject();
		messageToPuObject(message, jsonRequest);
		jsonRequest.set(JSON_REGISTRATION_IDS, registrationIds);
		Callback<PuObject> callback = new Callback<PuObject>() {

			@Override
			public void apply(PuObject result) {
				callbackExecutor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							int success = result.getInteger(JSON_SUCCESS);
							int failure = result.getInteger(JSON_FAILURE);
							int canonicalIds = result.getInteger(JSON_CANONICAL_IDS);
							long multicastId = result.getLong(JSON_MULTICAST_ID);
							MulticastResult.Builder builder = new MulticastResult.Builder(success, failure,
									canonicalIds, multicastId);
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
							sendCallback.apply(builder.build());
						} catch (Exception e) {
							sendCallback.apply(null);
						}
					}
				});

			}
		};
		try {
			post(GCM_SEND_ENDPOINT, "application/json", jsonRequest.toJSON(), callback);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Populate Map with message.
	 *
	 * @param message
	 *            Message used to populate Map.
	 * @param mapRequest
	 *            Map populated by Message.
	 */
	private void messageToPuObject(Message message, PuObject request) {
		if (message == null || request == null) {
			return;
		}
		setJsonField(request, PARAM_PRIORITY, message.getPriority());
		setJsonField(request, PARAM_CONTENT_AVAILABLE, message.getContentAvailable());
		setJsonField(request, PARAM_TIME_TO_LIVE, message.getTimeToLive());
		setJsonField(request, PARAM_COLLAPSE_KEY, message.getCollapseKey());
		setJsonField(request, PARAM_RESTRICTED_PACKAGE_NAME, message.getRestrictedPackageName());
		setJsonField(request, PARAM_DELAY_WHILE_IDLE, message.isDelayWhileIdle());
		setJsonField(request, PARAM_DRY_RUN, message.isDryRun());
		Map<String, String> payload = message.getData();
		if (!payload.isEmpty()) {
			request.set(JSON_PAYLOAD, payload);
		}
		if (message.getNotification() != null) {
			Notification notification = message.getNotification();
			PuObject nMap = new PuObject();
			if (notification.getBadge() != null) {
				setJsonField(nMap, JSON_NOTIFICATION_BADGE, notification.getBadge().toString());
			}
			setJsonField(nMap, JSON_NOTIFICATION_BODY, notification.getBody());
			setJsonField(nMap, JSON_NOTIFICATION_BODY_LOC_ARGS, notification.getBodyLocArgs());
			setJsonField(nMap, JSON_NOTIFICATION_BODY_LOC_KEY, notification.getBodyLocKey());
			setJsonField(nMap, JSON_NOTIFICATION_CLICK_ACTION, notification.getClickAction());
			setJsonField(nMap, JSON_NOTIFICATION_COLOR, notification.getColor());
			setJsonField(nMap, JSON_NOTIFICATION_ICON, notification.getIcon());
			setJsonField(nMap, JSON_NOTIFICATION_SOUND, notification.getSound());
			setJsonField(nMap, JSON_NOTIFICATION_TAG, notification.getTag());
			setJsonField(nMap, JSON_NOTIFICATION_TITLE, notification.getTitle());
			setJsonField(nMap, JSON_NOTIFICATION_TITLE_LOC_ARGS, notification.getTitleLocArgs());
			setJsonField(nMap, JSON_NOTIFICATION_TITLE_LOC_KEY, notification.getTitleLocKey());
			request.set(JSON_NOTIFICATION, nMap);
		}
	}

	/**
	 * Sets a JSON field, but only if the value is not {@literal null}.
	 */
	private void setJsonField(PuObject json, String field, Object value) {
		if (value != null) {
			json.set(field, value);
		}
	}

	/**
	 * Makes an HTTP POST request to a given endpoint.
	 *
	 * <p> <strong>Note: </strong> the returned connected should not be
	 * disconnected, otherwise it would kill persistent connections made using
	 * Keep-Alive.
	 *
	 * @param url endpoint to post the request. @param contentType type of
	 * request. @param body body of the request.
	 *
	 * @return the underlying connection.
	 *
	 * @throws IOException propagated from underlying methods. @throws
	 */
	public void post(String url, String contentType, String body, Callback<PuObject> callback) throws IOException {
		if (url == null || contentType == null || body == null) {
			throw new IllegalArgumentException("arguments cannot be null");
		}
		if (!url.startsWith("https://")) {
			logger.warning("URL does not use https: " + url);
		}
		PuObject data = PuObject.fromJSON(body);
		http = new HttpClientHelper();
		http.setUsingMultipath(false);
		RequestBuilder builder = null;
		builder = RequestBuilder.post(GCM_SEND_ENDPOINT).addHeader("Content-Type", contentType)
				.addHeader("Authorization", "key=" + key).setCharset(Charset.forName("utf8"));

		HttpAsyncFuture future = http.executeAsync(builder, data);
		future.setCallback(new Callback<HttpResponse>() {
			@Override
			public void apply(HttpResponse result) {
				PuObject puo = new PuObject();
				PuElement element = null;
				try {
					element = HttpClientHelper.handleResponse(result);
					puo = (PuObject) element;
				} catch (Exception e) {
					puo.set("error", element.toString());
				}
				puo.set("status", result.getStatusLine());
				callback.apply(puo);
			}
		});
		return;
	}

	static <T> T nonNull(T argument) {
		if (argument == null) {
			throw new IllegalArgumentException("argument cannot be null");
		}
		return argument;
	}

	void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
