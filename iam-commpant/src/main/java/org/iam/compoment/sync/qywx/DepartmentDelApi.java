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

@Name("部门移除")
public class DepartmentDelApi extends SyncApi implements SyncInteface {

	private Log log = LogFactory.getLog(getClass());
	@Param("企业ID")
	private String corpid = "ww00a52dcb1e35a5c3";
	@Param("通讯录密钥")
	private String corpsecret = "n8XObnc-HpbY6M0WWDRuIKB6jticSWSHXPY44X4mhbs";
	@DefaultContent
	private String defaultContent="<@JDBC id=\"org\" sql=\"select * from im_org a a.id=${oid}) a\"/>${org[0].SN}";
	
	
	@Override
	public ResultCode execute(String content) {
		try{
			String result=HttpUtil.GET_API("https://qyapi.weixin.qq.com/cgi-bin/department/delete?access_token="+getAccessToken(corpid,corpsecret)+"&id="+content.trim(), new HashMap());
			JSONObject json=JSONObject.fromObject(result);
			if(json.getInt("errcode")==0){
				//反写本地组织机构对应的企业微信组织ID
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
