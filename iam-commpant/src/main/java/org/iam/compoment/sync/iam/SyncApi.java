package org.iam.compoment.sync.iam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

import net.sf.json.JSONObject;

/**
 * IAM接口数据同步
 * 
 * Description: 
 * 
 * @author hyj
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class SyncApi implements SyncInteface{

	@Param("登录令牌地址")
	private String loginUrl="http://sso.sensesw.com:8882/api/sys/login";
	@Param("用户名")
	private String username="admin";
	@Param("密码")
	private String password="Sense@321";
	@Param("接口地址")
	private String addr="http://sso.sensesw.com:8882/api/sys/acct/save";
	@Param("请求头信息,采用<headername>value</headername>")
	private String headers="";
	@Param("验证正则表达式")
	private String valRegex="<![CDATA[.*true.*]]>";
	@Param("成功信息截取表达式")
	private String successRegex="<![CDATA[.*\"msg\":\"((.*)?)\".*]]>";
	@Param("失败信息截取表达式")
	private String failRegex="<![CDATA[.*\"msg\":\"((.*)?)\".*]]>";
	
	@Override
	public ResultCode execute(String content) {
		OutputStream os=null;
		BufferedReader bf=null;
		HttpURLConnection con=null;
		try {
			StringBuffer sb = new StringBuffer("<uim-login-user-id>"+getToken(username, password)+"</uim-login-user-id>");
			sb.append(headers);
			URL url=new URL(addr);
			con=(HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			Map<String,String> headMap=XMLUtil.simpleXml2Map("<headers>"+sb.toString()+"</headers>");
			for (Map.Entry<String, String> entry: headMap.entrySet()) {
				con.setRequestProperty(entry.getKey(),entry.getValue());
			}
			con.setRequestMethod("POST");
			con.setConnectTimeout(3000);
			os=con.getOutputStream();
			os.write(content.getBytes("UTF-8"));
			os.flush();
			if(con.getResponseCode()!=200){
				bf=new BufferedReader(new InputStreamReader(con.getErrorStream(),"UTF-8"));
			}else{
				bf=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			}
			StringBuffer resultBuf=new StringBuffer();
			String line;
			while((line=bf.readLine())!=null){
				resultBuf.append(line);
			}
			String resultStr=StringEscapeUtils.unescapeXml(resultBuf.toString());
			System.out.println(resultStr);
			if(resultStr.matches(valRegex)){
				return new ResultCode(SUCCESS,parseResult(resultStr,successRegex));
			}else{
				return new ResultCode(FAIL,parseResult(resultStr,failRegex));
			}
		}catch(Exception e){
			return new ResultCode(FAIL,e.getMessage());
		}
	}

	private static  String parseResult(String content,String regex){
		Matcher m=Pattern.compile(regex).matcher(content);
		while(m.find()){
			return m.group(1);
		}
		return "";
	}
	
	private String getToken(String username, String password) {
		Map<String, String> map = new HashMap<String, String>();
        map.put("username", username);
        map.put("password", password);
		String token="";
		try {
			String result = HttpUtil.POST_API(loginUrl, JSONObject.fromObject(map).toString());
			token = JSONObject.fromObject(result).getString("msg");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return token;
	}
	
//	public static void main(String[] args) {
//		SyncApi api=new SyncApi();
//		api.valRegex="*.\"code\":0*.";
//		api.headers="<Content-Type>application/json;charset=UTF-8</Content-Type>";
//		api.failRegex=".*\"msg\":\"((.*)?)\",.*";
//		api.successRegex=".*\"msg\":\"((.*)?)\",.*";
//		api.addr="http://10.255.56.127:8080/spring-boot-sso-ddc/ddc/users/insertSsuser?token=faa31bcb8156dd74ce27eea282781e88&account=test&pass=111&agentName=ORA";
//		api.execute("{\"employeeId\":\"1\",\"uid\":100044536,\"phone\":\"中\"}");
//		//		String s="{\"errcode\":0,\"access_token\":\"4fa30d4970243406804f74c6314ec4ad\",\"errmsg\":\"ok\",\"expires_in\":7200}";
////		System.out.println(s.matches(".*\"errcode\":0.*"));
////		System.out.println(parseResult(s,".*\"errmsg\":\"((.*)?)\",.*"));
////		System.out.println(StringEscapeUtils.unescapeXml(">&lt;?xml version='1.0' encoding='UTF-8'?&gt;&lt;data&gt;&lt;resulttype&gt;0&lt;/resulttype&gt;&lt;errormessage&gt;&#x7528;&#x6237;&#x4E0D;&#x5B58;&#x5728;&lt;/errormessage&gt;&lt;/data&gt;</UserStatusReturn></ns1:UserStatusResponse></soapenv:Body></soapenv:Envelope>"));
//	}
}
