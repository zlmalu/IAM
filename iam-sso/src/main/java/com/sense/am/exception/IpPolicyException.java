package com.sense.am.exception;

/**
 * 
 * IP限制异常
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class IpPolicyException extends RuntimeException{

	public IpPolicyException() {
		super();
	}

	public IpPolicyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IpPolicyException(String message, Throwable cause) {
		super(message, cause);
	}

	public IpPolicyException(String message) {
		super(message);
	}

	public IpPolicyException(Throwable cause) {
		super(cause);
	}

}
