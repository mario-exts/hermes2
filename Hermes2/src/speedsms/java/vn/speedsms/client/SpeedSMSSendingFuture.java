package vn.speedsms.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;

import com.nhb.common.Loggable;
import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.messaging.http.HttpAsyncFuture;
import com.nhb.messaging.http.HttpClientHelper;

import lombok.Getter;
import lombok.Setter;

public class SpeedSMSSendingFuture implements RPCFuture<SpeedSMSSendingResponse>, Loggable{

	@Getter
	private Throwable failedCause;

	@Getter
	@Setter
	private Callback<SpeedSMSSendingResponse> callback;

	private final HttpAsyncFuture future;

	private volatile SpeedSMSSendingResponse response;

	public SpeedSMSSendingFuture(HttpAsyncFuture future) {
		assert future != null;
		this.future = future;
		this.future.setCallback(new Callback<HttpResponse>() {

			@Override
			public void apply(HttpResponse result) {
				if (getCallback() != null) {
					if (result != null) {
						try {
							getCallback().apply(parseAndSaveResponse(result));
						} catch (Exception ex) {
							failedCause = ex;
							getCallback().apply(null);
						}
					} else {
						failedCause = future.getFailedCause();
						getCallback().apply(null);
					}
				}

			}
		});
	}

	private SpeedSMSSendingResponse parseAndSaveResponse(HttpResponse result) {
		if (this.response == null) {
			synchronized (this) {
				if (this.response == null) {
					PuElement puElement = HttpClientHelper.handleResponse(result);
					getLogger().debug("response: " + puElement);
					if (puElement instanceof PuObject) {
						SpeedSMSSendingResponse response = new SpeedSMSSendingResponse();
						response.readPuObject((PuObject) puElement);
						this.response = response;
					} else {
						throw new RuntimeException(
								"Response from server is not in json format, cannot parse as PuObject: " + puElement);
					}
				}
			}
		}
		return this.response;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return this.future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.future.isDone();
	}

	@Override
	public SpeedSMSSendingResponse get() throws InterruptedException, ExecutionException {
		return this.parseAndSaveResponse(this.future.get());
	}

	@Override
	public SpeedSMSSendingResponse get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return this.parseAndSaveResponse(this.future.get(timeout, unit));
	}

	@Override
	public void setTimeout(long arg0, TimeUnit arg1) {
		
	}

}
