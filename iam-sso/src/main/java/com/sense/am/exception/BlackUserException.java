package com.sense.am.exception;

/**
 * 
 * 黑名单用户
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@SuppressWarnings("serial")
public class BlackUserException extends RuntimeException{

	public BlackUserException() {
		super();
	}

	public BlackUserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BlackUserException(String message, Throwable cause) {
		super(message, cause);
	}

	public BlackUserException(String message) {
		super(message);
	}

	public BlackUserException(Throwable cause) {
		super(cause);
	}
	
	
}
