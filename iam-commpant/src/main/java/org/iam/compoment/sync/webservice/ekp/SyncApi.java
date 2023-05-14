package org.iam.compoment.sync.webservice.ekp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

/**
 * 采用rest接口进行数据同步
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
public class SyncApi implements SyncInteface{

	protected Log log=LogFactory.getLog(getClass());
	
	@Param("服务器地址")
	private String addr="http://127.0.0.1:8081/sys/webservice/sysSynchroSetOrgWebService";
	@Param("验证正则表达式")
	private String valRegex=".*<returnState>2</returnState>.*";
	@Param("成功信息截取表达式")
	private String successRegex=".*<returnState>((.*)?)</returnState>.*";
	@Param("失败信息截取表达式")
	private String failRegex=".*<returnState>((.*)?)</returnState>.*";
	
	@Param("请求超过时间(毫秒)")
	private String connectTimeout="3000";
	
	@Override
	public ResultCode execute(String content) {
		OutputStream os=null;
		BufferedReader bf=null;
		HttpURLConnection con=null;
		try {
			log.info("请求地址："+addr);
			log.info("请求参数："+content);
			URL url=new URL(addr);
			con=(HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
			con.setRequestProperty("Cache-Control","no-cache");
			con.setRequestProperty("Pragma","no-cache");
			con.setRequestMethod("POST");
			con.setConnectTimeout(Integer.valueOf(connectTimeout));
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
			log.info("响应结果："+resultStr);
			if(resultStr.matches(valRegex)){
				return new ResultCode(SUCCESS,parseResult(resultStr,successRegex));
			}else{
				return new ResultCode(FAIL,parseResult(resultStr,failRegex));
			}
		}catch(Exception e){
			return new ResultCode(FAIL,e.getMessage());
		}finally{
			if(con!=null){con.disconnect();}
			if(os!=null){try {os.close();os.flush();} catch (IOException e) {e.printStackTrace();}
			}
		}
	}

	private static  String parseResult(String content,String regex){
		Matcher m=Pattern.compile(regex).matcher(content);
		while(m.find()){
			return m.group(1);
		}
		return "";
	}
	
	public static void main(String[] args) {
		System.out.println(new SyncApi().execute("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:in=\"http://in.webservice.organization.sys.kmss.landray.com/\">"+
   "<soapenv:Header/>"+
   "<soapenv:Body>"+
      "<in:syncOrgElementsBaseInfo>"+
       "<arg0>"+
       "<appName></appName>"+
      "<orgJsonData>"+
       "[{\"id\":\"50000910\",\"lunid\":\"50000910\",\"name\":\"南京研发中心\",\"type\":\"dept\",\"no\":\"1002\",\"keyword\":\"1002\",\"order\":\"1\"}]"+
       "</orgJsonData>"+
       "<orgSyncConfig></orgSyncConfig>"+
       "</arg0>"+
      "</in:syncOrgElementsBaseInfo>"+
   "</soapenv:Body>"+
"</soapenv:Envelope>").getCode());
	}
}
