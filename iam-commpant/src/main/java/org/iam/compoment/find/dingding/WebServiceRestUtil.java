package org.iam.compoment.find.dingding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import net.sf.json.JSONObject;





public class WebServiceRestUtil{
	

  public static JSONObject post(String url,JSONObject jsonParam){
    StringBuffer sb = new StringBuffer();
    try {
    
      URL Url = new URL(url);
      URLConnection connection = Url.openConnection();
      HttpURLConnection httpConn = (HttpURLConnection)connection;
      httpConn.setConnectTimeout(2000);
      httpConn.setReadTimeout(5000);
      httpConn.setDoOutput(true);
      httpConn.setDoInput(true);
      httpConn.setRequestProperty("Content-Length", String.valueOf(jsonParam.toString().getBytes("utf-8").length));
      httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      httpConn.setRequestProperty("Accept", "application/json");
      httpConn.setRequestMethod("POST");
      httpConn.getOutputStream().write(jsonParam.toString().getBytes("utf-8"));
      httpConn.getOutputStream().flush();

      BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
      String line = "";
      while ((line = in.readLine()) != null) {
        sb.append(line);
      }
     // System.out.println(line);
      in.close();
    } catch (Exception e) {
    	e.printStackTrace();
    	sb = null;
    	// e.printStackTrace();
    }
	return JSONObject.fromObject(sb.toString());

  }
  
  
  public static JSONObject get(String url){
	    StringBuffer sb = new StringBuffer();
	    try {
	    
	      URL Url = new URL(url);
	      URLConnection connection = Url.openConnection();
	      HttpURLConnection httpConn = (HttpURLConnection)connection;
	      httpConn.setConnectTimeout(2000);
	      httpConn.setReadTimeout(5000);
	      httpConn.setDoOutput(true);
	      httpConn.setDoInput(true);

	      httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	      httpConn.setRequestMethod("GET");
	    

	      BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
	      String line = "";
	      while ((line = in.readLine()) != null) {
	        sb.append(line);
	      }
	     // System.out.println(line);
	      in.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	sb = null;
	    	// e.printStackTrace();
	    }
	    return JSONObject.fromObject(sb.toString());
	  }
}