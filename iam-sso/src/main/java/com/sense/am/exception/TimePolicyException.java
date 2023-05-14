package com.sense.am.exception;

public class TimePolicyException extends RuntimeException{

	public TimePolicyException() {
		super();
	}

	public TimePolicyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TimePolicyException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimePolicyException(String message) {
		super(message);
	}

	public TimePolicyException(Throwable cause) {
		super(cause);
	}

}
