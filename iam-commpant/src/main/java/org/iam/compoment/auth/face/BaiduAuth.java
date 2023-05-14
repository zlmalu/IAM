package org.iam.compoment.auth.face;

import java.util.Map;

import net.sf.json.JSONObject;

import com.sense.core.exception.UserAuthentionException;
import com.sense.core.security.Base64;
import com.sense.core.util.BaiduAiUtil;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.StringUtils;
import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.model.im.Image;
import com.sense.iam.service.ImageService;

/**
 * 基于百度AI的人脸认证
 * 
 * Description:  
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("人脸认证")
public class BaiduAuth implements AuthInterface{
	
	@Param("apiKey")
	private String apiKey="sOer2mgmTUCGQzyOswwHWx6u";
	@Param("apiSecuret")
	private String apiSecuret="8IpGbWD61zvj7NKFbHtlVonRliHRGPgr";
	@Param("认证阈值1-100")
	private String source="90";
	
	@Override
	public void authentication(String uid, String password, Map<String, Object> params) {
		ImageService imageService=(ImageService) ContextUtil.getBean("imageService");
		Image image=new Image();
		image.setOid(CurrentAccount.getCurrentAccount().getUserId());
		image=imageService.findByObject(image);
		if(image==null){
			throw new UserAuthentionException("验证失败,用户没有注册");
		}
		String srcImage=Base64.encodeToString(image.getContent());
		//获取当前用户头像信息
		if(StringUtils.isEmpty(BaiduAiUtil.AccessToken)){
			BaiduAiUtil.initAuth(apiKey, apiSecuret);
		}
		//进行头像信息对比
	    String retMsg=BaiduAiUtil.match(srcImage, password);
		JSONObject jsonObject =JSONObject.fromObject(retMsg);
		if(jsonObject.getInt("error_code")==110){//会话过期请重新初始化
			BaiduAiUtil.initAuth(apiKey, apiSecuret);
			retMsg=BaiduAiUtil.match(srcImage, password);
		}
		
		if(jsonObject.getInt("error_code")!=0){
			throw new RuntimeException("验证失败,"+retMsg);
		}else{
			if(((JSONObject)jsonObject.get("result")).getInt("score")<Integer.valueOf(source)){
				throw new RuntimeException("验证失败,对比阈值为:"+((JSONObject)jsonObject.get("result")).getInt("score"));
			}
		}
	}

	
	
}
