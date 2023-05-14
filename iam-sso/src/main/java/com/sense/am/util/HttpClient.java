package com.sense.am.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpClient {

	private String charset="UTF-8";
	
	private String cookie;
	
	public String login(String urlStr,String params) throws Exception{
		URL url=new URL(urlStr);
		OutputStream os=null;
		BufferedReader br=null;
		try{
			HttpURLConnection con=(HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			if(cookie!=null){
				con.addRequestProperty("Cookie", cookie);
			}
			os=con.getOutputStream();
			os.write(params.getBytes(charset));
			os.flush();
			br=new BufferedReader(new InputStreamReader(con.getInputStream(),charset));
			
			cookie=con.getHeaderField("Set-Cookie");
			String line=null;
			StringBuffer strBuf=new StringBuffer();
			while((line=br.readLine())!=null){
				strBuf.append(line.trim());
			}
			return strBuf.toString();
		}finally{
			if(os!=null)try{os.close();}catch(Exception e){}
			if(br!=null)try{br.close();}catch(Exception e){}
		}
		
	}
	
	public String post(String urlStr,String params) throws Exception{
		URL url=new URL(urlStr);
		OutputStream os=null;
		BufferedReader br=null;
		try{
			HttpURLConnection con=(HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			if(cookie!=null){
				con.addRequestProperty("Cookie", cookie);
			}
			os=con.getOutputStream();
			os.write(params.getBytes(charset));
			os.flush();
			br=new BufferedReader(new InputStreamReader(con.getInputStream(),charset));
			String line=null;
			StringBuffer strBuf=new StringBuffer();
			while((line=br.readLine())!=null){
				strBuf.append(line.trim());
			}
			return strBuf.toString();
		}finally{
			if(os!=null)try{os.close();}catch(Exception e){}
			if(br!=null)try{br.close();}catch(Exception e){}
		}
	}
	
	/*public static void main(String[] args) {
		try {
			HttpClient client=new HttpClient();
			String result=client.login("http://139.196.252.217:8060/IAM/login.action","username=admin&password=Password@123");
			
			System.out.println(net.sf.json.JSONObject.fromObject(result));
			//加载所有访问控制策略
			String resourceJsonStr=(client.post("http://139.196.252.217:8060/IAM/amResource.action", "operate=findList&pageSize=10000"));
			System.out.println(resourceJsonStr);
			//加载所有IP限制策略
			//加载所有用户限制策略
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
