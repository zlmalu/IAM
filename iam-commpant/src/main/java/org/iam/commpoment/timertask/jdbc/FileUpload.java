package org.iam.commpoment.timertask.jdbc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sense.core.util.ExcelUtils;

/**
 * Java原生的API可用于发送HTTP请求，即java.net.URL、java.net.URLConnection，这些API很好用、很常用，
 * 但不够简便；
 * 
 * 1.通过统一资源定位器（java.net.URL）获取连接器（java.net.URLConnection） 2.设置请求的参数 3.发送请求
 * 4.以输入流的形式获取返回内容 5.关闭输入流
 * 
 * @author H__D
 ***/
public class FileUpload {
	
	private static String cookie;
	
	private static  String login(String urlStr,String params) throws Exception{
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
			os.write(params.getBytes("utf-8"));
			os.flush();
			br=new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
			
			System.out.println(con.getHeaderFields());
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
	
	    /**
	     * 多文件上传的方法
	     * 
	     * @param actionUrl：上传的路径
	     * @param uploadFilePaths：需要上传的文件路径，数组
	     * @return
	     */
	    @SuppressWarnings("finally")
	    public static String uploadFile(String actionUrl,String serverUser,List models,List<Map<String,String>> contents,String type) {
	    	try {
				login("http://localhost:9082/IAM/login.action?username="+serverUser+"&password=Password@123","");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
	    	String end = "\r\n";
	        String twoHyphens = "--";
	        String boundary = "---------7d4a6d158c0";
	        DataOutputStream ds = null;
	        InputStream inputStream = null;
	        InputStreamReader inputStreamReader = null;
	        BufferedReader reader = null;
	        StringBuffer resultBuffer = new StringBuffer();
	        String tempLine = null;

	        try {
	            // 统一资源
	            URL url = new URL(actionUrl);
	            // 连接类的父类，抽象类
	            URLConnection urlConnection = url.openConnection();
	            // http的连接类
	            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

	            // 设置是否从httpUrlConnection读入，默认情况下是true;
	            httpURLConnection.setDoInput(true);
	            // 设置是否向httpUrlConnection输出
	            httpURLConnection.setDoOutput(true);
	            // 设定请求的方法，默认是GET
	            httpURLConnection.setRequestMethod("POST");
	            // 设置请求内容类型
	            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
	            httpURLConnection.addRequestProperty("Cookie", cookie);
	            // 设置DataOutputStream
	            ds = new DataOutputStream(httpURLConnection.getOutputStream());
//	            ds.writeBytes(end);
	            ///写文件
                ds.writeBytes(twoHyphens + boundary + end);
                ds.writeBytes("Content-Disposition: form-data; " + "name=\"file\";filename=\"file.xlsx\"" + end);
                ds.writeBytes("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" + end);
                ds.writeBytes(end);
                //输出文件到附件
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                ExcelUtils.export(bos, contents, models, type);
                ds.write(bos.toByteArray());
                bos.close();
	            ////////////////////文件写入完成
                ds.flush();
                ds.writeBytes(end);
                System.out.println(twoHyphens + boundary + twoHyphens + end);
	            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
	            /* close streams */
	            ds.flush();
	            
	            
	            
	            if (httpURLConnection.getResponseCode() >= 300) {
	                throw new Exception(
	                        "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
	            }

	            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
	                inputStream = httpURLConnection.getInputStream();
	                inputStreamReader = new InputStreamReader(inputStream);
	                reader = new BufferedReader(inputStreamReader);
	                tempLine = null;
	                resultBuffer = new StringBuffer();
	                while ((tempLine = reader.readLine()) != null) {
	                    resultBuffer.append(tempLine);
	                    resultBuffer.append("\n");
	                }
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (ds != null) {
	                try {
	                    ds.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	            if (inputStreamReader != null) {
	                try {
	                    inputStreamReader.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	            if (inputStream != null) {
	                try {
	                    inputStream.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }

	            return resultBuffer.toString();
	        }
	    }


	

}

