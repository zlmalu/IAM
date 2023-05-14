package com.sense.iam.api.action.sys;

import java.util.List;

import com.sense.iam.cam.Constants;
import com.sense.iam.model.sys.Acct;

public class ResultCodeReq  {
	

	public ResultCodeReq(int code,String msg,Acct userinfo,List<String> pfs){
		if(code==Constants.OPERATION_SUCCESS){
			this.code=code;
			this.success=true;
			this.msg=msg;
			this.userinfo=userinfo;
			this.pfs=pfs;
		}
	}
	
	// Field descriptor #30 I
	private int code;
	  
	// Field descriptor #32 Z
	private boolean success;
	  
	  
	java.lang.String msg;
	
	//权限集合
	List<String> pfs;
	Acct userinfo;
	
	
	public List<String> getPfs() {
		return pfs;
	}

	public void setPfs(List<String> pfs) {
		this.pfs = pfs;
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

	public Acct getUserinfo() {
		return userinfo;
	}

	public void setUserinfo(Acct userinfo) {
		this.userinfo = userinfo;
	}
	
	
	
	  
	 
}
