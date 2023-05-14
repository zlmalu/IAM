package org.iam.compoment.sync.iam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;


public class HttpUtil {
	
	
    /**
     * 
     * @param 要请求的接口地址
     * @param post参数
     * @return 接口返回的数据
     * @throws IOException
     */
    public static String POST_API(String url,String parameters) throws IOException{
        HttpClient httpclient =  HttpClients.createDefault();
        //新建Http  post请求  
        HttpPost httppost = new HttpPost(url);    //登录链接
        httppost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));   
        httppost.addHeader("Content-type","application/json; charset=utf-8");
        httppost.setHeader("Accept", "application/json");
        //处理请求，得到响应  
        HttpResponse response = httpclient.execute(httppost);   
        //打印返回的结果  
        HttpEntity entity = response.getEntity();  
       // Header[] map =  response.getAllHeaders();

        StringBuilder result = new StringBuilder();  
        if (entity != null) {  
            InputStream instream = entity.getContent();  
            BufferedReader br = new BufferedReader(new InputStreamReader(instream));  
            String temp = "";  
            while ((temp = br.readLine()) != null) {  
                String str = new String(temp.getBytes(), "utf-8");  
                result.append(str).append("\r\n");  
            }  
        }
        return result.toString();
    }
    
    public static String GET_API(String url) {
        HttpClient httpclient = HttpClients.createDefault();
        //新建Http  post请求  
        HttpGet httpGet = new HttpGet(url);    //登录链接
//        httppost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));   
//        httppost.addHeader("Content-type","application/json; charset=utf-8");
//        httppost.setHeader("Accept", "application/json");
        //处理请求，得到响应  
        try{
        HttpResponse response = httpclient.execute(httpGet);   
        //打印返回的结果  
        HttpEntity entity = response.getEntity();  
       // Header[] map =  response.getAllHeaders();

        StringBuilder result = new StringBuilder();  
        if (entity != null) {  
            InputStream instream = entity.getContent();  
            BufferedReader br = new BufferedReader(new InputStreamReader(instream));  
            String temp = "";  
            while ((temp = br.readLine()) != null) {  
                String str = new String(temp.getBytes(), "utf-8");  
                result.append(str).append("\r\n");  
            }  
        }
        	return result.toString();
        }catch(IOException e){
        	e.printStackTrace();
        }
        return null;
    }
    
    
    public static String PUT_API(String url, String parameters) throws IOException{
        HttpClient httpclient = HttpClients.createDefault();
        //新建Http  put请求  
        HttpPut httpPut = new HttpPut(url);    //登录链接
        httpPut.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));   
        httpPut.addHeader("Content-type","application/json; charset=utf-8");
        httpPut.setHeader("Accept", "application/json");
        //处理请求，得到响应  
        HttpResponse response = httpclient.execute(httpPut);   
        //打印返回的结果  
        HttpEntity entity = response.getEntity();  
       // Header[] map =  response.getAllHeaders();

        StringBuilder result = new StringBuilder();  
        if (entity != null) {  
            InputStream instream = entity.getContent();  
            BufferedReader br = new BufferedReader(new InputStreamReader(instream));  
            String temp = "";  
            while ((temp = br.readLine()) != null) {  
                String str = new String(temp.getBytes(), "utf-8");  
                result.append(str).append("\r\n");  
            }  
        }
        return result.toString();
    }
    
//    public static void main(String[] args) throws IOException {
//    	String url = "http://10.3.158.170/seeyon/rest/orgMember/?loginName=10021&token=af84ec32-d914-437f-9485-9d2674746d64";
//        Map<String, String> map = new HashMap<String, String>();  //构造参数
//        map.put("loginName", "10021");
//        map.put("token", "121312312");
//        POST_API(url, JSONObject.toJSONString(map));
//	}
    
//    public static void main(String[] args) {
//    	BufferedReader br=null;
//    	OutputStream os=null;
//    	try {
//    		URL url1=new URL("http://www.btjson.com/sms");
//    		HttpURLConnection con=(HttpURLConnection)url1.openConnection();
//    		con.setDoOutput(true);
//    		con.setDoInput(true);
//    		con.setRequestMethod("POST");
//    		os=con.getOutputStream();
//    		String info="m=13262527461&c=abcdefg";
//    		os.write(info.getBytes());
//    		
//    		br=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
//    		String line;
//    		StringBuffer resultMsgBuf=new StringBuffer();
//    		while((line=br.readLine())!=null){
//    			resultMsgBuf.append(line);
//    		}
//    		System.out.println(resultMsgBuf);
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    	} finally{
//    		if(os!=null)try{os.close();}catch(Exception e){}
//    		if(br!=null)try{br.close();}catch(Exception e){}
//    	}
//	}
    
//    public static void main(String[] args) {
//		String s = HttpUtil.GET_API("http://139.196.252.217:8060/IAM/exAuthenticate.action?username=1010&password=Password@1&uim-login-user-id="+BaseLocalService.getUID());
//		System.out.println(BaseLocalService.getUID());
//		System.out.println(s);
//    }
}