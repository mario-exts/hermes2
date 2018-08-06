package vn.speedsms.client.enums;

import lombok.Getter;

public enum SpeedSMSType {

	ADVERTISEMENT(1), CUSTOMER_CARE(2), BRAND_NAME(3);

	@Getter
	private int type;

	private SpeedSMSType(int type) {
		this.type = type;
	}

	public static SpeedSMSType fromType(int type) {
		for (SpeedSMSType smsType : values()) {
			if (smsType.getType() == type) {
				return smsType;
			}
		}
		return null;
	}
}
