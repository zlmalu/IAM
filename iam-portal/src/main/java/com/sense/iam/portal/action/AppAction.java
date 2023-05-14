package com.sense.iam.portal.action;

import com.sense.core.util.CurrentAccount;
import com.sense.iam.model.im.User;
import com.sense.iam.portal.HomeDataUtil;
import com.sense.iam.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;


/**
 * 
 * 应用中心
 * 
 * Description:
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Controller
@RequestMapping("app")
public class AppAction extends BaseAction {
	protected Log log = LogFactory.getLog(getClass());

	@Resource
	JdbcService jdbcService;
	
	@Resource
	UserService userService;


	@RequestMapping("/main.html")
	public String appCenter(){
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		List orgList=jdbcService.findList("select p.NAME from im_position p left join im_user_position o on o.position_id=p.id where o.user_id=?",account.getUserId());
		JSONArray data=JSONArray.fromObject(orgList);
		String positionName="";
		for(int i=0;i<data.size();i++){
			if(i+1==data.size()){
				positionName+=data.getJSONObject(i).getString("NAME");
			}else{
				positionName+=data.getJSONObject(i).getString("NAME")+",";
			}
		}
		User u=userService.findById(account.getUserId());
		request.setAttribute("positionName", positionName);
		request.setAttribute("user", u);
		return "app/main";
	}



	@RequestMapping("/load.action")
	@ResponseBody
	public Object load(){
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		JSONObject apps=HomeDataUtil.getAppsByUserId(jdbcService, account);
		return apps.toString();
	}

}
