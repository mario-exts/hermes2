package com.gaia.hermes2.statics;

public enum Status {
	SUCCESS(0,"Success"),
	UNKNOWN(1,"Unknown"),
	CHECK_TASK_UNSUCCESSFUL(2,"Check push task is unsuccessful"),
	PARAMS_MISSING(3,"Parameters is missing"),
	WRONG_PARAMS(4,"Wrong parameters"),
	DUPLICATE_AUTHENTICATOR_ID(5,"Duplicate authenticator ID"),
	DUPLICATE_BUNDLE_ID(6,"Duplicate bunde ID"),
	DUPLICATE_SMS_SERVICE(7,"Duplicate sms service"),
	DUPLICATE_TOKEN(8,"Duplicate token"),
	AUTHENTICATOR_NOT_FOUND(9,"Authenticator not found"),
	DUPLICATE_PRODUCT_ID(6,"Duplicate Product ID")
	;
	private int id;
	private String message;
	private Status(int id,String message){
		this.id=id;
		this.message=message;
	}
	
	public static Status fromId(int id){
		for(Status s:values()){
			if(s.getId()==id){
				return s;
			}
		}
		return Status.UNKNOWN;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
