package vn.speedsms.client;

import java.util.ArrayList;
import java.util.List;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuObject;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import vn.speedsms.client.enums.SpeedSMSError;

@Getter
@Setter
@ToString
public class SpeedSMSSendingResponse {

	private static final String TRANSACTION_ID = "tranId";
	private static final String TOTAL_SMS = "totalSMS";
	private static final String TOTAL_PRICE = "totalPrice";
	private static final String INVALID_PHONE = "invalidPhone";
	private static final String STATUS = "status";
	private static final String CODE = "code";
	private static final String DATA = "data";

	private String status;
	private SpeedSMSError error;

	private int transactionId;
	private int totalSMS;
	private long totalPrice;
	private final List<String> invalidPhone = new ArrayList<>();

	void readPuObject(PuObject data) {
		this.setStatus(data.getString(STATUS));
		if (data.variableExists(CODE)) {
			this.setError(SpeedSMSError.fromCode(data.getInteger(CODE)));
		}
		if (this.getError() == null && data.variableExists(DATA)) {
			PuObject puo = data.getPuObject(DATA);
			this.setTransactionId(puo.getInteger(TRANSACTION_ID, -1));
			this.setTotalSMS(puo.getInteger(TOTAL_SMS, 0));
			this.setTotalPrice(puo.getLong(TOTAL_PRICE, 0));

			PuArray arr = puo.getPuArray(INVALID_PHONE, null);
			if (arr != null && arr.size() > 0) {
				arr.forEach(value -> {
					invalidPhone.add(value.getString());
				});
			}
		}
	}
}
