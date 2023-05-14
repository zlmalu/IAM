package org.iam.compoment.sync.yunxiazi;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.DefaultContent;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
/**
 * 1：启用 4：禁用
 * @author heyijun
 *
 */
@Name("用户启用/禁用")
public class UserDisableApi extends SyncApi implements SyncInteface {

	private Log log = LogFactory.getLog(getClass());
	@Param("接口地址")
	private String addrPrefix = "https://192.168.0.226:9443";
	@Param("登录用户")
	private String username = "admin";
	@Param("登录密码")
	private String password = "Password@5";
	@DefaultContent
	private String defaultContent = "<@JDBC id=\"accts\" sql=\"select a.LOGIN_NAME from im_account a where a.ID =${oid}\"/>{\"loginName\": \"${accts[0].LOGIN_NAME}\",\"status\": 4";
	
	@Override
	public ResultCode execute(String content) {
		System.out.println("sync user enable content===="+content);
		try{
			JSONObject contObj=JSONObject.fromObject(content);
			
			String token =getLoginToken(addrPrefix, username, password);
			Map<String, String> headers=new HashMap<String, String>();
			headers.put("Referer", addrPrefix);
			headers.put("Origin", addrPrefix);
			headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
			headers.put("nonce", getNonce());
			headers.put("Content-Type", "text/plain");
			headers.put("Cookie", "YAB_AUTH_TOKEN="+token);
			
			JSONObject searchJson = new JSONObject();
			searchJson.put("c", "{}");
			searchJson.put("b", "{\"page\":1,\"pageSize\":1,\"sKey\":\"LOGIN_NAME\",\"sValue\":\""+contObj.getString("loginName")+"\"}");
			String searchBody=getEncryBody(token.substring(8,24),searchJson.toString());
			
			String userList = HttpUtil.POST(addrPrefix+"/3.0/userService/userList", searchBody, headers);
			JSONArray users= JSONArray.fromObject(JSONObject.fromObject(JSONObject.fromObject(userList).get("data")).get("rows"));
			Integer userId=JSONObject.fromObject(users.get(0)).getInt("id");
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("ids", new Integer[]{userId});
			jsonObj.put("status", contObj.getInt("status"));
			JSONObject json = new JSONObject();
			json.put("c", "{}");
			json.put("b", jsonObj);
			String addBody=getEncryBody(token.substring(8,24),json.toString());
			String result=HttpUtil.POST(addrPrefix+"/3.0/userService/operStatus", addBody, headers);
			Integer code=Integer.parseInt(JSONObject.fromObject(result).get("code").toString());
			if(code==0){
				return new ResultCode(SUCCESS, JSONObject.fromObject(result).getString("msg"));
			}else{
				return new ResultCode(FAIL, JSONObject.fromObject(result).getString("msg"));
			}
		}catch(Exception e){
			log.error("sync data edit exception",e);
			return new ResultCode(FAIL,"sync error "+e.getMessage());
		}
	}
	
}
