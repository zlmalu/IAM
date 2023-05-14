package com.sense.iam.sso;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.sense.core.util.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class HttpUtil {

	private static Log log=LogFactory.getLog(HttpUtil.class);
	
	/**
	 * 采用POST方式进行
	 * @param url 
	 * @param paramStr
	 * @return
	 */
	public static String POST_API(String urlStr,String paramStr,Map<String,String> headers) {
		log.debug(urlStr+","+paramStr+","+headers);
		BufferedReader br=null;
		OutputStream os=null;
		StringBuffer strBuf=new StringBuffer();
		try {
			URL url=new URL(urlStr);
			HttpURLConnection con=(HttpURLConnection) url.openConnection();
			con.setReadTimeout(5000);//5秒过�?
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			for (Map.Entry<String,String> entry : headers.entrySet()) {
				con.setRequestProperty(entry.getKey(), entry.getValue());
			}
			os=con.getOutputStream();
			
			os.write(paramStr.getBytes("UTF-8"));
			if(con.getResponseCode()==200){
				br=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
				String line;
				while((line=br.readLine())!=null){
					strBuf.append(line);
				}
			}
		} catch (Exception e) {
			log.error("post_api error",e);
		} finally{
			if(os!=null){
				try{os.close();}catch(Exception e){}
			}
			if(br!=null){
				try{br.close();}catch(Exception e){}
			}
		}
		return strBuf.toString();
	}
	
    public static String GET_API(String urlStr,Map<String,String> headers) {
    	log.debug(urlStr+","+headers);
		BufferedReader br=null;
		OutputStream os=null;
		StringBuffer strBuf=new StringBuffer();
		try {
			URL url=new URL(urlStr);
			HttpURLConnection con=(HttpURLConnection) url.openConnection();
			con.setReadTimeout(5000);//5秒过�?
			for (Map.Entry<String,String> entry : headers.entrySet()) {
				con.setRequestProperty(entry.getKey(), entry.getValue());
			}
			br=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			String line;
			while((line=br.readLine())!=null){
				strBuf.append(line);
			}
		} catch (Exception e) {
			log.error("get_api error",e);
		} finally{
			if(os!=null){
				try{os.close();}catch(Exception e){}
			}
			if(br!=null){
				try{br.close();}catch(Exception e){}
			}
		}
		return strBuf.toString();
    }
    
    public static String post(String requestUrl, String accessToken, String params)
            throws Exception {
        String contentType = "application/x-www-form-urlencoded";
        return HttpUtil.post(requestUrl, accessToken, contentType, params);
    }

    public static String post(String requestUrl, String accessToken, String contentType, String params)
            throws Exception {
        String encoding = "UTF-8";
        if (requestUrl.contains("nlp")) {
            encoding = "GBK";
        }
        return HttpUtil.post(requestUrl, accessToken, contentType, params, encoding);
    }

    public static String post(String requestUrl, String accessToken, String contentType, String params, String encoding)
            throws Exception {
        String url = requestUrl + "?access_token=" + accessToken;
        return HttpUtil.postGeneralUrl(url, contentType, params, encoding);
    }

    public static String postGeneralUrl(String generalUrl, String contentType, String params, String encoding)
            throws Exception {
        URL url = new URL(generalUrl);
        // 打开和URL之间的连�?
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // 设置通用的请求属�?
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // 得到请求的输出流对象
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(params.getBytes(encoding));
        out.flush();
        out.close();

        // 建立实际的连�?
        connection.connect();
        // 获取�?有响应头字段
        Map<String, List<String>> headers = connection.getHeaderFields();
        // 遍历�?有的响应头字�?
        for (String key : headers.keySet()) {
            System.err.println(key + "--->" + headers.get(key));
        }
        // 定义 BufferedReader输入流来读取URL的响�?
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), encoding));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        return result;
    }
    
    public static String doPost(String url, JSONObject param) {
        HttpPost httpPost = null;
        String result = null;
        try {
            HttpClient client = new DefaultHttpClient();
            httpPost = new HttpPost(url);
            if (param != null) {
                StringEntity se = new StringEntity(param.toString(), "utf-8");
                httpPost.setEntity(se); // post方法中，加入json数据
                httpPost.setHeader("Content-Type", "application/json");
            }
            
            HttpResponse response = client.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "utf-8");
                }
            }
            
        } catch (Exception ex) {
            System.err.println("发送到接口出错");
        }
        return result;
    }
    
    public static String POSTS(String url, JSONArray param, Map<String,String> headers) {
        HttpPost httpPost = null;
        String result = null;
        try {
            HttpClient client = new DefaultHttpClient();
            httpPost = new HttpPost(url);
            if (param != null) {
                StringEntity se = new StringEntity(param.toString(), "utf-8");
                httpPost.setEntity(se); // post方法中，加入json数据
                httpPost.addHeader("Content-Type", "application/json");
                for (Map.Entry<String,String> entry : headers.entrySet()) {
                	httpPost.addHeader(entry.getKey(), entry.getValue());
    			}
            }
            
            HttpResponse response = client.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "utf-8");
                }
            }
            
        } catch (Exception ex) {
            System.err.println("发送到接口出错");
        }
        return result;
    }
    
    public static String DELETES(String url, Map<String,String> headers) {
    	 HttpDelete httpDelete = null;
         String result = null;
         try {
             HttpClient client = new DefaultHttpClient();
             httpDelete = new HttpDelete(url);
             httpDelete.addHeader("Content-Type", "application/json");
             for (Map.Entry<String,String> entry : headers.entrySet()) {
            	 httpDelete.addHeader(entry.getKey(), entry.getValue());
     		 }
             HttpResponse response = client.execute(httpDelete);
             if (response != null) {
                 HttpEntity resEntity = response.getEntity();
                 if (resEntity != null) {
                     result = EntityUtils.toString(resEntity, "utf-8");
                 }
             }
             
         } catch (Exception ex) {
             System.err.println("发送到接口出错");
         }
         return result;
    }
    
//    public static void main(String[] args) {
//    	String result1=GET_API("http://openapi.italent.cn/swaggerfile/token?app_id=908&tenant_id=110605&secret=7c7eee75c0004819a09c203d724fc0bc&grant_type=client_credentials", new HashMap());
//		System.out.println("getToken:"+result1);		
//		String result2=GET_API("https://openapi.italent.cn/userframework/v1/110605/staffs?staffcode=035", new HashMap<String,String>(){
//			{
//				put("Authorization","Bearer "+JSONObject.fromObject(result1).getString("content"));
//			}
//		});
//		System.out.println("getUserID:"+JSONObject.fromObject(result2).getJSONArray("items").getJSONObject(0).getJSONObject("staffDto").getString("userId"));
//		String userId=JSONObject.fromObject(result2).getJSONArray("items").getJSONObject(0).getJSONObject("staffDto").getString("userId");
//    }
    /*public static void main(String[] args) {
    	//获取调用接口token
		String access_token=HttpUtil.GET_API("http://test.zhangin.com:8896/api/getToken.htm?client_id=4L2rmqXEv0&client_secret=8gCDtG5YRyrIypLX143Vm7hbEYuuAasA&grant_type=code&response_type=json", new HashMap());
		System.out.println(JSONObject.fromObject(access_token).getString("accessToken"));
		Map<String, String> headers=new HashMap<String, String>();
		headers.put("Authorization", "Bearer "+JSONObject.fromObject(access_token).getString("accessToken"));
		//获取单点登录token
//		String login_token = HttpUtil.GET_API("http://test.zhangin.com:8896/api/login/mobile.htm?mobile=15618956022", headers);
//		System.out.println(login_token);
		//获取部门信息
		String departmentInfo = HttpUtil.GET_API("http://test.zhangin.com:8896/api/department/list/1/10.htm", headers);
		System.out.println(departmentInfo);
		//获取用户信息
		String usetInfo = HttpUtil.GET_API("http://test.zhangin.com:8896/api/personSeal/list/1/10.htm", headers);
		System.out.println(usetInfo);
		//批量添加部门信息
//		JSONArray jsons = new JSONArray();
//		JSONObject json = new JSONObject();
//		json.put("name", "申石测试");
//		json.put("code", "test00201");
//		json.put("parent_id", "6f324179887f477ab6526338ccaee805");
//		json.put("department_id", StringUtils.getUuid());
//		json.put("is_enable", 1);
//		jsons.add(json);
//		String dep_result=HttpUtil.POSTS("http://test.zhangin.com:8896/api/department/batchCreate.htm", jsons, headers);
//		System.out.println(dep_result);
		//删除部门信息
//		String dep_result=HttpUtil.DELETES("http://test.zhangin.com:8896/api/department/batchDelete.htm?department_ids=ZGJ1002", headers);
//		System.out.println(dep_result);
		//批量添加人员信息
//		JSONArray jsons = new JSONArray();
//		JSONObject json = new JSONObject();
//		json.put("mobile", "13262527461");
//		json.put("name", "张杰1");
//		json.put("code", "zhangjie");
//		json.put("password", "88888888");
//		json.put("uid", "ZJ001");
//		json.put("department_id", "402881ec74c52a7b017545413714018e");
//		jsons.add(json);
//		String user_result=HttpUtil.POSTS("http://test.zhangin.com:8896/api/personSeal/batchCreate.htm", jsons, headers);
//		System.out.println(user_result);
		
		
	}*/
}
