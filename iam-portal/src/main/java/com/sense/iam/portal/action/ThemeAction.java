package com.sense.iam.portal.action;

import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.PortalSettingManage;
import com.sense.iam.portal.LoginInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ThemeResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 
 * 主题
 * 
 * Description:
 * 
 * @author w_jfwen
 * 
 *         Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@RestController
public class ThemeAction extends BaseAction {
	protected Log log=LogFactory.getLog(getClass());

	@Autowired
	private ThemeResolver themeResolver;



	@RequestMapping("/changeTheme")
	public ResultCode changeTheme(HttpServletRequest request,
							  HttpServletResponse response, String themeName) {
		if(themeName == null){
			themeName = "default";
		}
		//只有允许用户个性化开启之后才能更新主题
		if(getPortalSettingManage() != null && getPortalSettingManage().getUserEnable().intValue() == 1) {
			log.info("current theme is " + themeResolver.resolveThemeName(request));
			themeResolver.setThemeName(request, response, themeName);
			log.info("current theme change to " + themeResolver.resolveThemeName(request));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}else{
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}


	
}
