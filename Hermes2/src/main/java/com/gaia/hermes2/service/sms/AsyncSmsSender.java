package com.gaia.hermes2.service.sms;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;

import com.gaia.hermes2.statics.F;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.messaging.http.HttpAsyncFuture;
import com.nhb.messaging.http.HttpClientHelper;

public class AsyncSmsSender {
	public static final String API_URL = "http://api.speedsms.vn/index.php";
	protected String mAccessToken;
	private HttpClientHelper http;
//	private ExecutorService callbackExecutor;
	
	public AsyncSmsSender(String accessToken) {
		this.mAccessToken = accessToken;
//		this.callbackExecutor = Executors.newCachedThreadPool();
		http = new HttpClientHelper();
		http.setUsingMultipath(false);
	}
	
	/**
	 * Get user information
	 * @param: none
	 * @return: json string
	* */
	public void getUserInfo(Callback<PuObject> callback){
		String userCredentials = mAccessToken+":x";
		String basicAuth = "Basic " + Base64.encode(userCredentials.getBytes(), userCredentials.length());
		RequestBuilder builder = null;
		
		builder = RequestBuilder.get(API_URL + "/user/info").addHeader("Content-Type", "application/json")
				.addHeader("Authorization", basicAuth).setCharset(Charset.forName("utf8"));
		
		HttpAsyncFuture future = http.executeAsync(builder,null);
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
	}
	
	public void sendSMS(String message,List<String> recipients, int smsType,Callback<PuObject> callback){
		String userCredentials = mAccessToken+":x";
		String basicAuth = "Basic " + Base64.encode(userCredentials.getBytes(), userCredentials.length());
		RequestBuilder builder = null;
		
		builder = RequestBuilder.post(API_URL + "/sms/send").addHeader("Content-Type", "application/json")
				.addHeader("Authorization", basicAuth).setCharset(Charset.forName("utf8"));
		PuObject data=new PuObject();
		data.set(F.CONTENT, message);
		PuArray arr=new PuArrayList();
		for(String s:recipients){
			arr.addFrom(s);
		}
		data.set(F.TO, arr);
		data.set(F.SMS_TYPE, smsType);
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
	}
	
	public void getStatus(int tranId,Callback<PuObject> callback){
		String userCredentials = mAccessToken+":x";
		String basicAuth = "Basic " + Base64.encode(userCredentials.getBytes(), userCredentials.length());
		RequestBuilder builder = null;
		
		builder = RequestBuilder.get(API_URL + "/sms/status/" + tranId).addHeader("Content-Type", "application/json")
				.addHeader("Authorization", basicAuth).setCharset(Charset.forName("utf8"));
		
		HttpAsyncFuture future = http.executeAsync(builder,null);
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
	}
}
