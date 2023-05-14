package org.iam.compoment.sync.yunxiazi;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.DefaultContent;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.JdbcService;

@Name("用户新增")
public class UserAddApi extends SyncApi implements SyncInteface {

	private Log log = LogFactory.getLog(getClass());
	@Param("接口地址")
	private String addrPrefix = "https://192.168.0.226:9443";
	@Param("登录用户")
	private String username = "admin";
	@Param("登录密码")
	private String password = "Password@5";
	@DefaultContent
	private String defaultContent = "<@JDBC id=\"accts\" sql=\"select a.LOGIN_NAME,a.LOGIN_PWD,u.NAME,u.SEX,u.TELEPHONE,u.SN as USER_SN,u.EMAIL,(select VALUE from im_org_attr where ORG_ID=ou.ORG_ID) as YXZ_ORG_ID from IM_ACCOUNT a left join IM_USER u on u.ID = a.USER_ID left join IM_ORG_USER ou on u.ID=ou.USER_ID left join IM_ORG o on ou.ORG_ID=o.ID where a.ID =${oid}\"/>{\"loginName\": \"${accts[0].LOGIN_NAME}\",\"loginPwd\": \"Password@1\",\"name\": \"${accts[0].NAME}\",\"departmentId\": ${accts[0].YXZ_ORG_ID},\"email\":\"${accts[0].EMAIL!}\",\"phone\": \"${accts[0].TELEPHONE!}\",\"roleId\":13,\"verifyType\":1}";
	
	@Override
	public ResultCode execute(String content) {
		System.out.println("sync user add content===="+content);
		try{
			JSONObject contObj=JSONObject.fromObject(content);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("loginName", contObj.getString("loginName"));
			jsonObj.put("pwd", getEncryBody("tmppasswordver1.", contObj.getString("loginPwd")));
			jsonObj.put("name", contObj.getString("name"));
			jsonObj.put("phone", contObj.getString("phone"));
			jsonObj.put("email", contObj.getString("email"));
			jsonObj.put("roleId", contObj.getInt("roleId"));
			jsonObj.put("departmentId", contObj.getInt("departmentId"));
			jsonObj.put("verifyType", contObj.getInt("verifyType"));
			JSONObject json = new JSONObject();
			json.put("c", "{}");
			json.put("b", jsonObj);
			String token =getLoginToken(addrPrefix, username, password);
			String body=getEncryBody(token.substring(8,24),json.toString());
			Map<String, String> headers=new HashMap<String, String>();
			headers.put("Referer", addrPrefix);
			headers.put("Origin", addrPrefix);
			headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
			headers.put("nonce", getNonce());
			headers.put("Content-Type", "text/plain");
			headers.put("Cookie", "YAB_AUTH_TOKEN="+token);
			String result=HttpUtil.POST(addrPrefix+"/3.0/userService/addUser", body, headers);
			Integer code=Integer.parseInt(JSONObject.fromObject(result).get("code").toString());
			if(code==0){
				return new ResultCode(SUCCESS, JSONObject.fromObject(result).getString("msg"));
			}else{
				return new ResultCode(FAIL, JSONObject.fromObject(result).getString("msg"));
			}
		}catch(Exception e){
			log.error("sync data add exception",e);
			return new ResultCode(FAIL,"sync error "+e.getMessage());
		}
	}
	
}
