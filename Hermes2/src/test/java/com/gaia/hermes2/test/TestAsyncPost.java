package com.gaia.hermes2.test;


import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gcm.server.AsyncSender;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Priority;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Notification;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuObject;

public class TestAsyncPost {
	public static void main(String[] args){
		AsyncSender client=new AsyncSender("AIzaSyDRMEo9WE7N_ZLEUHWwqBmJX6lLzTZL_nA");
		Sender sender=new Sender("AIzaSyDRMEo9WE7N_ZLEUHWwqBmJX6lLzTZL_nA");
		
		Callback<Result> callback=new Callback<Result>() {

			@Override
			public void apply(Result result) {
				// TODO Auto-generated method stub
				System.out.println("result 1:  "+result.toString());
			}
		};
		
		Callback<MulticastResult> callback2=new Callback<MulticastResult>() {

			@Override
			public void apply(MulticastResult result) {
				// TODO Auto-generated method stub
				System.out.println(result.toString());
			}
		};
		
		Message.Builder builder=new Message.Builder();
		Notification.Builder b=new Notification.Builder("");
		Notification notify=b.badge(1).body("hello").build();
		Message msg=builder.notification(notify)
			.addData("msg", "xin chao")
			.priority(Priority.HIGH)
			.build();
		List<String> list=new ArrayList<>();
		list.add("123");
		list.add("1234444");
		try {
			client.send(msg,list,5, callback2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try {
//			Result re=sender.send(msg, "123",5);
//			System.out.println("re: "+re.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
}
