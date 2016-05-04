package com.gaia.hermes2.test;


import static com.google.android.gcm.server.Constants.GCM_SEND_ENDPOINT;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.RequestBuilder;

import com.google.android.gcm.server.AsyncSender;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Priority;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Notification;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuObject;
import com.nhb.messaging.http.HttpAsyncFuture;
import com.nhb.messaging.http.HttpClientHelper;
import com.nhb.messaging.http.HttpUtil;

public class TestAsyncPost {
	public static void main(String[] args){
		TestAsyncPost app=new TestAsyncPost();
		app.register();
		
	}
	
	public void register(){
		PuObject puo=new PuObject();
		puo.set("command", "registerToken");
		puo.set("appId", "4ec298c7-7b93-4d0a-b5c1-68ccea2307dc");
		puo.set("authenticatorId", "d51a8708-4c15-471d-9781-0d39a57ac575");
		puo.set("serviceType", "gcm");
		HttpClientHelper http = new HttpClientHelper();
		for(int i=0;i<2;i++){
			puo.set("token", UUID.randomUUID().toString());
			System.out.println("register: "+puo.get("token"));
			
			
			http.setUsingMultipath(false);
			RequestBuilder builder=null;
				builder = RequestBuilder.post("http://localhost:8801/hermes2/register")
						.addHeader("Content-Type", "multipart/form-data")
						.setCharset(Charset.forName("utf8"));
			
			HttpAsyncFuture future=http.executeAsync(builder, puo);
			future.setCallback(new Callback<HttpResponse>() {

				@Override
				public void apply(HttpResponse result) {
					
					
				}
				
			});
		}
		
	}
}
