package com.sense.iam.auth.controller;



import com.sense.iam.cam.Constants;


public class ResultCodeReq  {
	

	public ResultCodeReq(int code,String msg,String sessionId){
		if(code==Constants.LOGIN_STATUS_SUCCESS){
			this.code=code;
			this.success=true;
			this.msg=msg;
			this.sessionId=sessionId;
		}
	}
	
	// Field descriptor #30 I
	private int code;
	
	java.lang.String sessionId;
	
	  
	// Field descriptor #32 Z
	private boolean success;
	  
	  
	java.lang.String msg;
	
	
	
	


	

	public java.lang.String getSessionId() {
		return sessionId;
	}

	public void setSessionId(java.lang.String sessionId) {
		this.sessionId = sessionId;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public java.lang.String getMsg() {
		return msg;
	}

	public void setMsg(java.lang.String msg) {
		this.msg = msg;
	}


	
	  
	 
}
