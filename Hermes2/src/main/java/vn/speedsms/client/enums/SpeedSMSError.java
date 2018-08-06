package vn.speedsms.client.enums;

import lombok.Getter;

public enum SpeedSMSError {
	IP_LOCKED(7, "IP locked"),
	ACCOUNT_BLOCKED(8, "Account blocked"),
	ACCOUNT_NOT_ALLOWED(9, "Account not allow to call the API"),
	INVALID_PARAMETER(101, "Invalid or missing parameters"),
	INVALID_PHONE_NUMBER(105, "Phone number invalid"),
	ENCODING_NOT_SUPPORTED(110, "Not support sms content encoding"),
	CONTENT_TOO_LONG(113, "Sms content too long"),
	BALANCE_NOT_ENOUGH(300, "Your account balance not enough to send sms"),
	SERVICE_UNAVALABLE(500, "Internal error, please try again");

	@Getter
	private int code;
	@Getter
	private String message;

	private SpeedSMSError(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public static SpeedSMSError fromCode(int code) {
		for (SpeedSMSError error : values()) {
			if (error.getCode() == code) {
				return error;
			}
		}
		return null;
	}

	public static SpeedSMSError fromCode(String stringCode) {
		int code = Integer.valueOf(stringCode);
		return fromCode(code);
	}
}
