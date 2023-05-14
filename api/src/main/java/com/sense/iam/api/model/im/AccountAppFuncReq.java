package com.sense.iam.api.model.im;

import java.util.ArrayList;
import java.util.List;

import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.AppFunc;

import io.swagger.annotations.ApiModelProperty;

/**
 * 帐号应用权限模型
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
public class AccountAppFuncReq {

	
	private Long acctId;
	
	private List<Long> funcs;

	@ApiModelProperty(value="唯一标识")
	public Long getAcctId() {
		return acctId;
	}

	public void setAcctId(Long acctId) {
		this.acctId = acctId;
	}

	@ApiModelProperty(value="功能权限集合")
	public List<Long> getFuncs() {
		return funcs;
	}

	public void setFuncs(List<Long> funcs) {
		this.funcs = funcs;
	}
	
	public Account toAccount(){
		Account account=new Account();
		List<AppFunc> list=new ArrayList<AppFunc>();
		AppFunc appFunc;
		for (Long funcId : funcs) {
			appFunc=new AppFunc();
			appFunc.setId(funcId);
			list.add(appFunc);
		}
		account.setId(acctId);
		account.setAppFuncs(list);
		return account;
	}
	
}
