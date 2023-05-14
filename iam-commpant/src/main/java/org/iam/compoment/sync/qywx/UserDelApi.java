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

@Name("用户移除")
public class UserDelApi extends SyncApi implements SyncInteface {
	
	private Log log = LogFactory.getLog(getClass());
	@Param("企业ID")
	private String corpid = "ww9410bb0b70e44ce1";
	@Param("通讯录密钥")
	private String corpsecret = "DEyoLxGRyAG1dgby_tuSEPg2Mc9-gLW24lCWEBaUaZs";
	@DefaultContent
	private String defaultContent="<@JDBC id=\"acct\" sql=\"select * from im_account a a.id=${oid}) a\"/>${acct[0].LOGIN_NAME}";
	
	@Override
	public ResultCode execute(String content) {
		try{
			String result=HttpUtil.GET_API("https://qyapi.weixin.qq.com/cgi-bin/user/delete?access_token="+getAccessToken(corpid,corpsecret)+"&userid="+content.trim(), new HashMap());
			JSONObject json=JSONObject.fromObject(result);
			if(json.getInt("errcode")==0){
				return new ResultCode(SUCCESS,json.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,json.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qywx del exception",e);
			return new ResultCode(FAIL,"sync error "+e.getMessage());
		}
	}
	

}
