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

@Name("部门更新")
public class DepartmentEditApi extends SyncApi implements SyncInteface {

	private Log log = LogFactory.getLog(getClass());
	@Param("企业ID")
	private String corpid = "ww00a52dcb1e35a5c3";
	@Param("通讯录密钥")
	private String corpsecret = "n8XObnc-HpbY6M0WWDRuIKB6jticSWSHXPY44X4mhbs";
	@DefaultContent
	private String defaultContent="<@JDBC id=\"org\" sql=\"select * from (select a.ID,a.SN,a.NAME,a.PARENT_ID,a.SORT_NUM,b.SN as PSN from im_org a left join im_org b on a.parent_id=b.ID where a.id=${oid}) a\"/><#if org[0].PARENT_ID!=-1>{\"id\": \"${org[0].SN}\",\"name\": \"${org[0].NAME}\",\"parentid\": \"${org[0].PSN}\",\"order\": \"${org[0].SORT_NUM}\"}</#if>";
	
	@Override
	public ResultCode execute(String content) {
		try{
			String result=HttpUtil.POST_API("https://qyapi.weixin.qq.com/cgi-bin/department/update?access_token="+getAccessToken(corpid,corpsecret), content, new HashMap());
			JSONObject json=JSONObject.fromObject(result);
			if(json.getInt("errcode")==0){
				//反写本地组织机构对应的企业微信组织ID
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
