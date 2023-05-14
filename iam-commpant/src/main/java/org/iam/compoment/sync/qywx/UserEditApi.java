package org.iam.compoment.sync.qywx;

import java.util.HashMap;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.HttpUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.DefaultContent;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

@Name("用户更新")
public class UserEditApi extends SyncApi implements SyncInteface {

	private Log log = LogFactory.getLog(getClass());
	@Param("企业ID")
	private String corpid = "ww9410bb0b70e44ce1";
	@Param("通讯录密钥")
	private String corpsecret = "DEyoLxGRyAG1dgby_tuSEPg2Mc9-gLW24lCWEBaUaZs";
	@DefaultContent
	private String defaultContent = "<@JDBC id=\"accts\" sql=\"select a.LOGIN_NAME,a.LOGIN_PWD,u.NAME,u.SEX,u.TELEPHONE,u.SN as USER_SN,o.sn as ORG_SN,u.EMAIL,p.NAME as POSITION_NAME from IM_ACCOUNT a left join IM_USER u on u.ID = a.USER_ID left join IM_ORG_USER ou on u.ID=ou.USER_ID left join IM_ORG o on ou.ORG_ID=o.ID left join im_user_position up on u.ID=up.USER_ID and up.TYPE=1  left join IM_POSITION p on up.POSITION_ID=p.ID where a.ID =${oid}\"/>{\"userid\": \"${accts[0].LOGIN_NAME}\",\"name\": \"${accts[0].NAME}\",\"department\": [${accts[0].ORG_SN}],\"position\": \"${accts[0].POSITION_NAME!}\",\"gender\":\"${accts[0].SEX}\",\"email\":\"${accts[0].EMAIL!}\",\"mobile\": \"${accts[0].TELEPHONE}\",\"enable\":1}";
	
	
	@Override
	public ResultCode execute(String content) {
		try{
			String result=HttpUtil.POST_API("https://qyapi.weixin.qq.com/cgi-bin/user/update?access_token="+getAccessToken(corpid,corpsecret), content, new HashMap());
			JSONObject json=JSONObject.fromObject(result);
			if(json.getInt("errcode")==0){
				return new ResultCode(SUCCESS,json.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,json.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qywx edit exception",e);
			return new ResultCode(FAIL,"sync error "+e.getMessage());
		}
	}
}
