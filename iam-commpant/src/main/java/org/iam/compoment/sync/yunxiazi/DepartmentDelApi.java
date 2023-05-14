package org.iam.compoment.sync.yunxiazi;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.DefaultContent;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

@Name("部门移除")
public class DepartmentDelApi extends SyncApi implements SyncInteface {

	private Log log = LogFactory.getLog(getClass());
	@Param("接口地址")
	private String addrPrefix = "https://192.168.0.226:9443";
	@Param("登录用户")
	private String username = "admin";
	@Param("登录密码")
	private String password = "Password@5";
	@DefaultContent
	private String defaultContent="<@JDBC id=\"org\" sql=\"select a.ID,a.SN,a.NAME,a.PARENT_ID,b.VALUE as YXZ_ORG_ID,(select VALUE from im_org_attr where ORG_ID=a.PARENT_ID) as YXZ_PARENT_ID from im_org a left join im_org_attr b on a.id=b.org_id where b.NAME='YXZ_ORG_ID' and a.id=${oid}\"/>{\"yxzOrgId\": \"${org[0].YXZ_ORG_ID}\",\"yxzParentId\": \"${org[0].YXZ_PARENT_ID}\"}";
	
	
	@Override
	public ResultCode execute(String content) {
		System.out.println("sync org delete content===="+content);
		try{
			JSONObject contObj=JSONObject.fromObject(content);
			
			JSONObject json = new JSONObject();
			json.put("c", "{}");
			json.put("b", "{\"ids\":["+contObj.getInt("yxzOrgId")+"]}");
			String token =getLoginToken(addrPrefix, username, password);
			String body=getEncryBody(token.substring(8,24),json.toString());
			Map<String, String> headers=new HashMap<String, String>();
			headers.put("Referer", addrPrefix);
			headers.put("Origin", addrPrefix);
			headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
			headers.put("nonce", getNonce());
			headers.put("Content-Type", "text/plain");
			headers.put("Cookie", "YAB_AUTH_TOKEN="+token);
			String result = HttpUtil.POST(addrPrefix+"/3.0/departmentService/deleteDepartments", body, headers);
			Integer code=Integer.parseInt(JSONObject.fromObject(result).get("code").toString());
			if(code==0){
				return new ResultCode(SUCCESS,JSONObject.fromObject(result).getString("msg"));
			}else{
				return new ResultCode(FAIL,JSONObject.fromObject(result).getString("msg"));
			}
		}catch(Exception e){
			log.error("sync data delete exception",e);
			return new ResultCode(FAIL,"sync error "+e.getMessage());
		}
	}
}
