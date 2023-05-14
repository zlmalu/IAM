package com.sense.am.exception;

/**
 * 
 * 资源授权访问用户异常
 * 
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class ResourceAuthUserException extends RuntimeException{

	public ResourceAuthUserException() {
		super();
	}

	public ResourceAuthUserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ResourceAuthUserException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceAuthUserException(String message) {
		super(message);
	}

	public ResourceAuthUserException(Throwable cause) {
		super(cause);
	}
}
