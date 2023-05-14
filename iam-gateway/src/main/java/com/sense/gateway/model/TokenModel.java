package com.sense.gateway.model;

public class TokenModel {

	String sessionId;
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	String host;
	String jwdtoken;
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getJwdtoken() {
		return jwdtoken;
	}
	public void setJwdtoken(String jwdtoken) {
		this.jwdtoken = jwdtoken;
	}
	@Override
	public String toString() {
		return "AuthModel [sessionId=" + sessionId + ", host=" + host
				+ ", jwdtoken=" + jwdtoken + "]";
	}
	
	
}
