package com.sense.am.exception;

/**
 * 
 * 强认证异常
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@SuppressWarnings("serial")
public class StrongAuthenticationException extends RuntimeException{

	public StrongAuthenticationException() {
		super();
	}

	public StrongAuthenticationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StrongAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public StrongAuthenticationException(String message) {
		super(message);
	}

	public StrongAuthenticationException(Throwable cause) {
		super(cause);
	}

	
	
}
