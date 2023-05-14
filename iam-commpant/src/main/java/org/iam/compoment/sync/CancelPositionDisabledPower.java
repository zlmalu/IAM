package org.iam.compoment.sync;

import java.util.ArrayList;
import java.util.List;

import com.sense.core.util.ContextUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.DefaultContent;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.AccountService;

/**
 * 用户岗位变动清除多余应用
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("岗位变动禁用帐号")
public class CancelPositionDisabledPower implements SyncInteface{
	@Param("默认sql样例")
	private String findSql="select * from im_account where open_type=2 and app_id not in (select app_ID from im_position_app where position_id in (select position_id from im_user_position where user_id=100000150)";
	@DefaultContent
	private String defaultContent="<@JDBC id=\"accts\" sql=\"select * from im_account where open_type=2 and app_id not in (select app_ID from im_position_app where position_id in (select position_id from im_user_position where user_id=${oid}))\" />\n"+
"<#list accts as acct>${acct.ID?c},</#list>";
	@Override
	public ResultCode execute(String content) {
		String[] idStrs=content.trim().split(",");
		List<Long> ids=new ArrayList<Long>();
		for (int i = 0; i < idStrs.length; i++) {
			if(idStrs[i].trim().length()>0){
				ids.add(Long.valueOf(idStrs[i]));
			}
		}
		((AccountService)ContextUtil.getBean("imAccountService")).disabled(ids.toArray(new Long[ids.size()]));
		return new ResultCode(SUCCESS);
	}

}
