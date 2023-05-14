package org.iam.compoment.sync.qywx;

import java.util.HashMap;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.HttpUtil;

public class SyncApi {
	protected Log log = LogFactory.getLog(getClass());
	
	public String getAccessToken(String corpid, String corpsecret) {
		try {
			String result = HttpUtil.GET_API("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret,new HashMap());
			log.debug(result);
			Integer code=Integer.parseInt(JSONObject.fromObject(result).get("errcode").toString());
			if(code!=0){
				log.error("获取Token异常");
			}
			return JSONObject.fromObject(result).get("access_token").toString();
		} catch (Exception e) {
			log.error("获取访问token失败",e);
		}
		return "";
	}
  
}
