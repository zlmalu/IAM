package org.iam.compoment.sync.rest.fy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;


public class HttpUtil {

	public static void main(String[] args) {
		String  userxml="";
		userxml+="<![CDATA[";
		userxml+="<?xml version='1.0' encoding='UTF-8'?>";
		userxml+="<root>";
		userxml+="<hrmlist>";
		userxml+="<hrm action=\"add\">";
		//固定参数-对象必须传，可留空
		userxml+="<jobtitle></jobtitle>";//岗位-职务，名称对象
		userxml+="<jobgroupid></jobgroupid>";//职务等级
		userxml+="<jobactivityid></jobactivityid>";//职务
		userxml+="<managerid></managerid>";//直接上级
		userxml+="<workroom></workroom>";//办公地点
		userxml+="<locationid></locationid>";//办公位置
		userxml+="<systemlanguage>简体中文</systemlanguage>";//系统语言
		userxml+="<birthday></birthday>";//出生日期
		userxml+="<maritalstatus>未婚</maritalstatus>";
		
		//动态参数-必填项不能留空
		userxml+="<workcode>100023</workcode>";//用户编号
		userxml+="<loginid>test23</loginid>";//用户登录名
		userxml+="<password>123456</password>";//密码
		userxml+="<subcompany>周文清</subcompany>";//分部
		userxml+="<department>周文清团队</department>";//部门
		userxml+="<sex>男</sex>";//性别
		userxml+="<lastname>测试</lastname>";//姓名
		userxml+="<telephone></telephone>";//手机号
		userxml+="<mobile></mobile>";//联系方式
		userxml+="<email></email>";//邮件地址
		userxml+="<statue>正式</statue>";//状态
		
		userxml+="</hrm>";
		userxml+="</hrmlist>";
		userxml+="</root>";
		userxml+="]]>";
		
		//创建人员--声明头部
		StringBuffer  xml= new StringBuffer("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hrm=\"http://localhost/services/HrmService\">");
		xml.append("<soapenv:Header/>");
		xml.append("<soapenv:Body>");
		xml.append("<hrm:SynHrmResource>");
		xml.append("<hrm:in0>192.168.0.1</hrm:in0>");
		xml.append("<hrm:in1>"+userxml.toString()+"</hrm:in1>");
		xml.append("</hrm:SynHrmResource>");
		xml.append("</soapenv:Body>");
		xml.append("</soapenv:Envelope>");
		System.out.println(userxml.toString());
		System.out.println(xml.toString());
		String resp=soapPostSendXml("http://106.15.232.78/services/HrmService", xml.toString());
		System.out.println("resp="+resp);
	}
	
	
  public static String GET_API(String url) {
        HttpClient httpclient = HttpClients.createDefault();
        //新建Http  post请求  
        HttpGet httpGet = new HttpGet(url);    //登录链接
//	        httppost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));   
//	        httppost.addHeader("Content-type","application/json; charset=utf-8");
//	        httppost.setHeader("Accept", "application/json");
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
    
    //请求soap接口用这个方法
    public static String soapPostSendXml(String SOAPUrl,String parXmlInfo){
          StringBuilder result = new StringBuilder();
          try {
              // Create the connection where we're going to send the file.
              URL url = new URL(SOAPUrl);
              URLConnection connection = url.openConnection();
              HttpURLConnection httpConn = (HttpURLConnection) connection;
              // how big it is so that we can set the HTTP Cotent-Length
              // property. (See complete e-mail below for more on this.)
              // byte[] b = bout.toByteArray();
              byte[] b = parXmlInfo.getBytes("UTF-8");
              // Set the appropriate HTTP parameters.
              httpConn.setRequestProperty( "Content-Length",String.valueOf( b.length ) );
              httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
              httpConn.setRequestMethod( "POST" );
              httpConn.setDoOutput(true);
              httpConn.setDoInput(true);
        
              // Everything's set up; send the XML that was read in to b.
              OutputStream out = httpConn.getOutputStream();
              out.write( b ); 
              out.close();
              // Read the response and write it to standard out.
              InputStreamReader isr =new InputStreamReader(httpConn.getInputStream());
              BufferedReader in = new BufferedReader(isr);
              String inputLine;
              while ((inputLine = in.readLine()) != null)
                      result.append(inputLine);
              in.close();
          
          } catch (MalformedURLException e) {
              e.printStackTrace();
          } catch (IOException e) {
              e.printStackTrace();
          }
          return result.toString();
      }
        
      
      public static String readTxtFile(String filePath){
          StringBuilder result = new StringBuilder();  
          try {

                  String encoding="GBK";

                  File file=new File(filePath);

                  if(file.isFile() && file.exists()){ //判断文件是否存在

                      InputStreamReader read = new InputStreamReader(

                      new FileInputStream(file),encoding);//考虑到编码格式

                      BufferedReader bufferedReader = new BufferedReader(read);

                      String lineTxt=null;

                      while((lineTxt = bufferedReader.readLine()) != null){
                          result.append(lineTxt); 
                          System.out.println(lineTxt);

                      }

                      read.close();

          }else{

              System.out.println("找不到指定的文件");

          }

          } catch (Exception e) {

              System.out.println("读取文件内容出错");

              e.printStackTrace();

          }

          return result.toString();
      }
    
    /**
     * http post请求
     * @param url                        地址
     * @param postContent                post内容格式为param1=value¶m2=value2¶m3=value3
     * @return
     * @throws IOException
     */
    public static String httpPostRequest(URL url, String postContent) throws Exception{
        OutputStream outputstream = null;
        BufferedReader in = null;
        try
        {
            URLConnection httpurlconnection = url.openConnection();
            httpurlconnection.setConnectTimeout(10 * 1000);
            httpurlconnection.setDoOutput(true);
            httpurlconnection.setUseCaches(false);
            OutputStreamWriter out = new OutputStreamWriter(httpurlconnection
                    .getOutputStream(), "UTF-8");
            out.write(postContent);
            out.flush();
            
            StringBuffer result = new StringBuffer();
            in = new BufferedReader(new InputStreamReader(httpurlconnection
                    .getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
            return result.toString();
        }
        catch(Exception ex){
          
            throw new Exception("post请求异常：" + ex.getMessage());
        }
        finally
        {
            if (outputstream != null)
            {
                try
                {
                    outputstream.close();
                }
                catch (IOException e)
                {
                    outputstream = null;
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    in = null;
                }
            }
        }    
    }    
    
   
    
    /**
     * 字符串处理,如果输入字符串为null则返回"",否则返回本字符串去前后空格。
     * @param inputStr            输入字符串
     * @return    string             输出字符串
     */
    public static String doString(String inputStr){
        //如果为null返回""
        if(inputStr == null || "".equals(inputStr) || "null".equals(inputStr)){
            return "";
        }    
        //否则返回本字符串把前后空格去掉
        return inputStr.trim();
    }

    /**
     * 对象处理，如果输入对象为null返回"",否则则返回本字符对象信息，去掉前后空格
     * @param object
     * @return
     */
    public static String doString(Object object){
        //如果为null返回""
        if(object == null || "null".equals(object) || "".equals(object)){
            return "";
        }    
        //否则返回本字符串把前后空格去掉
        return object.toString().trim();
    }
    
}