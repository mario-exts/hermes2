//package com.gaia.hermes2.service.sms;
//
//import java.io.Closeable;
//import java.io.IOException;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.RequestBuilder;
//
//import com.nhb.common.BaseLoggable;
//import com.nhb.common.async.Callback;
//import com.nhb.common.data.PuElement;
//import com.nhb.common.data.PuObject;
//import com.nhb.messaging.http.HttpAsyncFuture;
//import com.nhb.messaging.http.HttpClientHelper;
//
//import vn.speedsms.client.SmsEnvelop;
//
//public class SmsAsyncSender  extends BaseLoggable implements Closeable {
//	public static final String API_URL = "http://api.speedsms.vn/index.php";
//	private String mAccessToken;
//	private HttpClientHelper httpClient;
//	
//	public SmsAsyncSender(String accessToken) {
//		this.mAccessToken = accessToken;
//		httpClient = new HttpClientHelper();
//		httpClient.setUsingMultipath(false);
//	}
//	
//	@Override
//	public void close() throws IOException {
//		this.httpClient.close();
//	}
//
//	public void getUserInfo(Callback<PuObject> callback){
//		String userCredentials = mAccessToken+":x";
//		String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), userCredentials.length()));
//		RequestBuilder builder = null;
//		
//		builder = RequestBuilder.get(API_URL + "/user/info").addHeader("Content-Type", "application/json")
//				.addHeader("Authorization", basicAuth);
//		
//		HttpAsyncFuture future = httpClient.executeAsync(builder,null);
//		future.setCallback(new Callback<HttpResponse>() {
//			@Override
//			public void apply(HttpResponse result) {
//				PuObject puo = new PuObject();
//				PuElement element = null;
//				try {
//					element = HttpClientHelper.handleResponse(result);
//					puo = (PuObject) element;
//				} catch (Exception e) {
//					puo.set("error", element.toString());
//				}
//				puo.set("status", result.getStatusLine());
//				callback.apply(puo);
//			}
//		});
//	}
//	
//	public void sendSMS(SmsEnvelop content,Callback<SendResult> successCallback, 
//						Callback<Throwable> failureCallback){
//		String userCredentials = mAccessToken+":x";
//		String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), userCredentials.length()));
//		RequestBuilder builder = null;
//		builder = RequestBuilder.post(API_URL + "/sms/send").addHeader("Content-Type", "application/json")
//				.addHeader("Authorization", basicAuth);
//		PuObject data= content.toPuObject();
//		
//		HttpAsyncFuture future = httpClient.executeAsync(builder, data);
//		future.setCallback(new Callback<HttpResponse>() {
//			@Override
//			public void apply(HttpResponse response) {
//				if (response == null) {
//					if (failureCallback != null) {
//						failureCallback.apply(future.getFailedCause());
//					} else {
//						getLogger().error("Sending SMS error", future.getFailedCause());
//					}
//				} else {
//					PuElement puElement = HttpClientHelper.handleResponse(response);
//					if (!(puElement instanceof PuObject) || response.getStatusLine().getStatusCode() != 200) {
//						Exception exception = new Exception("An error responsed by SMS --> " + puElement.toString());
//						if (failureCallback != null) {
//							failureCallback.apply(exception);
//						} else {
//							getLogger().error("Sending SMS error", exception);
//						}
//					}else{
//						SendResult result=SendResult.fromPuObject((PuObject) puElement);
//						getLogger().debug("Sending SMS result success: "+puElement);
//						successCallback.apply(result);
//					}
//				}
//				
//			}
//		});
//	}
//	
//	public void getStatus(int tranId,Callback<PuObject> callback){
//		String userCredentials = mAccessToken+":x";
//		String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), userCredentials.length()));
//		RequestBuilder builder = null;
//		
//		builder = RequestBuilder.get(API_URL + "/sms/status/" + tranId).addHeader("Content-Type", "application/json")
//				.addHeader("Authorization", basicAuth);
//		
//		HttpAsyncFuture future = httpClient.executeAsync(builder,null);
//		future.setCallback(new Callback<HttpResponse>() {
//			@Override
//			public void apply(HttpResponse result) {
//				PuObject puo = new PuObject();
//				PuElement element = null;
//				try {
//					element = HttpClientHelper.handleResponse(result);
//					puo = (PuObject) element;
//				} catch (Exception e) {
//					puo.set("error", element.toString());
//				}
//				puo.set("status", result.getStatusLine());
//				callback.apply(puo);
//			}
//		});
//	}
//}
