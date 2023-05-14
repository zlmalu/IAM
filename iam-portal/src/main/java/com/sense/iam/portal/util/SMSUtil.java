package com.sense.iam.portal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.Clock;
import java.util.Base64;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

@Component
public class SMSUtil {
	protected static Log log = LogFactory.getLog(SMSUtil.class);
	// 产品名称:云通信短信API产品,开发者无需替换
    private static String product;
	
	@Value("${com.sms.product}")
	public void setProduct(String product1) {
		this.product = product1;
	}
	
    private static String domain;
	
	@Value("${com.sms.domain}")
	public void setdomain(String domain) {
		this.domain = domain;
	}
	
    
    // 此处需要替换成开发者自己的AK(在阿里云访问控制台寻找)
	
    private static String accessKeyId;
    
    @Value("${com.sms.accessKeyId}")
	public void setaccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}
    
    private static String accessKeySecret;
	
	@Value("${com.sms.accessKeySecret}")
	public void setaccessKeySecret(String accessKeySecret) {
		this.accessKeySecret = accessKeySecret;
	}
    
    private static String signName;
	@Value("${com.sms.signName}")
	public void setsignName(String signName) {
		this.signName = signName;
	}
	
	
    //发送密码找回模板
    private static String identifyingTempleteCode1;
	@Value("${com.sms.identifyingTempleteCode1}")
	public void setidentifyingTempleteCode1(String identifyingTempleteCode1) {
		this.identifyingTempleteCode1 = identifyingTempleteCode1;
	}
	
    //短信登录模板
    private static String identifyingTempleteCode;
	@Value("${com.sms.identifyingTempleteCode}")
	public void setidentifyingTempleteCode(String identifyingTempleteCode) {
		this.identifyingTempleteCode = identifyingTempleteCode;
	}
	
	
	private static String apikey;//即 API-Key
	private static String apisecret;
	private static String address;
	
	/*@Value("${com.sms.custom.address}")
	public void setAddress(String address) {
		this.address = address;
	}
	
  	@Value("${com.sms.custom.apikey}")
	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	@Value("${com.sms.custom.apisecret}")
	public void setApisecret(String apisecret) {
		this.apisecret = apisecret;
	}
*/


	public static SendSmsResponse sendSms(String mobile, String templateParam, String templateCode)
            throws ClientException {

        // 可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        // 初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        // 组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();

        // 必填:待发送手机号
        request.setPhoneNumbers(mobile);
        // 必填:短信签名-可在短信控制台中找到
        request.setSignName(signName);
        // 必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(templateCode);

        // 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        request.setTemplateParam(templateParam);

        // 选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        // request.setSmsUpExtendCode("90997");

        // 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");

        // hint 此处可能会抛出异常，注意catch
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

        return sendSmsResponse;
    }

    

    public static SendSmsResponse sendIdentifyingCode1(String mobile, String code) {
        try {
            return sendSms(mobile, "{\"code\":\"" + code + "\"}", identifyingTempleteCode1);
        } catch (ClientException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public static SendSmsResponse sendIdentifyingCode(String mobile, String code) {
        try {
            return sendSms(mobile, "{\"code\":\"" + code + "\"}", identifyingTempleteCode);
        } catch (ClientException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

//    public static void main(String[] args) {
//        // 发短信
//        SendSmsResponse response = SMSUtil.sendIdentifyingCode("13262527461", "888888");
//        log.info("短信接口返回的数据----------------");
//        log.info("Code=" + response.getCode());
//        log.info("Message=" + response.getMessage());
//        log.info("RequestId=" + response.getRequestId());
//        log.info("BizId=" + response.getBizId());
//
//    }

    
    
    
    
    
    
    
    
    
    
    
    
    
  	/**
  	 * 自定义短信认证接口
  	 * @param mobile
  	 * @param code
  	 * @return
  	 */
  	public static SendSmsResponse sendCustomMessage1(String mobile, String code) {
          try {
          	String data = "您正在使用申石软件统一身份短信认证服务，此次验证码是："+code+"，如非本人操作，请忽略本条短信";
          	String content = Base64.getEncoder().encodeToString(data.getBytes("GBK"));//需要用GBK编码，否则有问题
          	//base64编码结果：
          	String contentUrlEncoded = URLEncoder.encode(content, "gbk");
          	
          	data = "mobiles="+mobile+"&sms="+contentUrlEncoded;
          	long t = Clock.systemUTC().millis();   //即 timestamp
          	String data2sign = data + apikey + t;
          	String signature = HmacUtils.hmacSha1Hex(apisecret, data2sign);    // Signature
          	log.info("add head apikey:"+apikey);
          	log.info("add head signature:"+signature);
          	log.info("send param:"+data);
          	log.info("postaddress:"+address);
          	log.info("action http post...");
          	String resp=POST_API(address,signature,t,apikey,data);
          	log.info("result:"+resp);
          	SendSmsResponse sendSmsResponse=new SendSmsResponse();
          	if(resp.indexOf("<Result>0</Result>")!=-1){
          		sendSmsResponse.setCode("OK");
          		sendSmsResponse.setMessage("发送成功");
          	}else{
          		sendSmsResponse.setCode("FAIL");
          		sendSmsResponse.setMessage("接口异常");
          	}
          	return  sendSmsResponse;
             
          } catch (Exception e) {
              throw new RuntimeException(e.getMessage());
          }
     }
  	/**
  	 * 自定义短信密码找回接口
  	 * @param mobile
  	 * @param code
  	 * @return
  	 */
  	public static SendSmsResponse sendCustomMessage2(String mobile, String code) {
  		try {
          	String data = "您正在使用申石软件统一身份密码找回服务，此次验证码是："+code+"，如非本人操作，请忽略本条短信";
          	String content = Base64.getEncoder().encodeToString(data.getBytes("GBK"));//需要用GBK编码，否则有问题
          	//base64编码结果：
          	String contentUrlEncoded = URLEncoder.encode(content, "gbk");
          	
          	data = "mobiles="+mobile+"&sms="+contentUrlEncoded;
          	long t = Clock.systemUTC().millis();   //即 timestamp
          	String data2sign = data + apikey + t;
          	String signature = HmacUtils.hmacSha1Hex(apisecret, data2sign);    // Signature
        	log.info("add head apikey:"+apikey);
          	log.info("add head signature:"+signature);
          	log.info("send param:"+data);
          	log.info("postaddress:"+address);
          	log.info("action http post...");
          	String resp=POST_API(address,signature,t,apikey,data);
          	log.info("result:"+resp);
          	SendSmsResponse sendSmsResponse=new SendSmsResponse();
          	if(resp.indexOf("<Result>0</Result>")!=-1){
          		sendSmsResponse.setCode("OK");
          		sendSmsResponse.setMessage("发送成功");
          	}else{
          		sendSmsResponse.setCode("FAIL");
          		sendSmsResponse.setMessage("接口异常");
          	}
          	return  sendSmsResponse;
             
          } catch (Exception e) {
              throw new RuntimeException(e.getMessage());
          }
      }

    /**
     * 
     * @param 要请求的接口地址
     * @param post参数
     * @return 接口返回的数据
     * @throws IOException
     */
    public static String POST_API(String url,String signature,long t,String APIKey,String parameters) throws IOException{
        HttpClient httpclient =  HttpClients.createDefault();
        //新建Http  post请求  
        HttpPost httppost = new HttpPost(url);    //登录链接
        httppost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));   
        httppost.addHeader("Content-Type","application/x-www-form-urlencoded");
        httppost.setHeader("API-Key",APIKey);
        httppost.setHeader("Signature",signature);
        httppost.setHeader("timestamp",t+"");
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
    
}
