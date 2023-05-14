package org.iam.compoment.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.DefaultContent;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.JdbcService;

/**
 * 
 * 用户复职自动启用帐号
 * Description:  当用户从禁用状态恢复成启用状态，自动启用所有账户
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("用户复职自动启用帐号")
public class AutoEnabled implements SyncInteface{

	@Param("默认sql样例")
	private String exclusive="APP001";
	
	@DefaultContent
	private String defaultContent="${oid}";


	/**
	 * content 为当前用户禁用的id列表，  使用英文逗号进行分割
	 */
	@Override
	public ResultCode execute(String content) {
		String findSql="select ID from im_account where user_id="+content+(StringUtils.isTrimEmpty(content)?"":(" and APP_ID in (select ID from IM_APP where sn not in ('"+exclusive.replace("[,]", "','")+"'))"));
		List<Map> idLists=((JdbcService)ContextUtil.getBean("jdbcService")).findList(findSql);
		List<Long> ids=new ArrayList<Long>();
		for (Map idMap : idLists) {
			ids.add(Long.valueOf(idMap.get("ID").toString()));
		}
		((AccountService)ContextUtil.getBean("imAccountService")).enabled(ids.toArray(new Long[ids.size()]));
		return new ResultCode(SUCCESS);
	}

}