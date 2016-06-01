//package com.gaia.hermes2.service.sms;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import com.gaia.hermes2.statics.F;
//import com.nhb.common.data.PuArray;
//import com.nhb.common.data.PuObject;
//import com.nhb.common.data.PuValue;
//
//import lombok.Getter;
//import lombok.Setter;
//@Getter
//@Setter
//
//public class SendResult {
//	private String status;
//	private String code;
//	private String message;
//	private String transId;
//	private long totalSms;
//	private long totalPrice;
//	private Set<String> invalidPhones;
//	
//	public static SendResult fromPuObject(PuObject data){
//		SendResult result=new SendResult();
//		result.setStatus(data.getString(F.STATUS));
//		result.setCode(data.getString(F.CODE));
//		if(result.getCode().equals("00")){
//			PuObject puo=data.getPuObject(F.DATA);
//			result.setTransId(puo.getString(F.TRAN_ID));
//			result.setTotalSms(puo.getLong(F.TOTAL_SMS));
//			result.setTotalPrice(puo.getLong(F.TOTAL_PRICE));
//			PuArray arr=puo.getPuArray(F.INVALID_PHONE);
//			result.setInvalidPhones(new HashSet<>());
//			for(PuValue val:arr){
//				result.getInvalidPhones().add(val.getString());
//			}
//		}else{
//			result.setMessage(data.getString(F.MESSAGE));
//		}
//		return result;
//	}
//}
