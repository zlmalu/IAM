package com.sense.iam.auth;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SendMail {

	private static Log log=LogFactory.getLog(SendMail.class);
	/**
	 * 邮件SMTP服务器
	 */
	
    private static String HOST;
	
    @Value("${mail.smtp.host}")
	public void setHOST(String HOST) {
		this.HOST = HOST;
	}
    /**
	 * 端口
	 */
	
    private static String PORT;
    @Value("${mail.smtp.port}")
	public void setPORT(String PORT) {
		this.PORT = PORT;
	}
    /**
	 * 发件人帐号
	 */

    private static String USER;
	@Value("${mail.smtp.auth.user}")
	public void setUSER(String USER) {
		this.USER = USER;
	}
    /**
	 * 临时密钥
	 */
	
    private static String TEMPWD;
    @Value("${mail.smtp.auth.pwd}")
	public void setTEMPWD(String TEMPWD) {
		this.TEMPWD = TEMPWD;
	}
    
    public static void sendMail(String toMail,String content){
    	try{
    		log.info("发送告警邮件到:::"+toMail);
	        Properties properties = new Properties();
	        properties.setProperty("mail.transport.protocol", "SMTPS");
	        properties.setProperty("mail.smtp.host",HOST);
	        properties.setProperty("mail.smtp.port", "465");
	        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
	        properties.setProperty("mail.smtp.socketFactory.port", "465");
	        properties.setProperty("mail.smtp.auth","true");
	        log.info(properties);
	        Authenticator authenticator = new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication(){
	                //此处密码填写非邮箱登录密码，应该为邮箱授权码
	                return new PasswordAuthentication(USER,TEMPWD);
	            }
	        };
	        Session session = Session.getInstance(properties,authenticator);
	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress(USER));
			message.setRecipient(Message.RecipientType.TO,new InternetAddress(toMail));
			message.setSubject("风险告警");
	    	message.setContent(
					"<div>尊敬的管理员 "
							+"，您好，<p style='margin: 0;margin-left: 26px;'>服务器存在异常用户认证</p> "
							+ "<p style='margin: 0;margin-left: 26px;'>"+content+"</p>"
							+ "<br/>认证异常"
							+"</div>",
							
					"text/html;charset=UTF-8");
	        Transport.send(message);
    	}catch(Exception ex){
    		log.error("mail send error",ex);
    	}
    }
    
  /*  public static void main(String[] args) {
    	Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "SMTPS");
        properties.setProperty("mail.smtp.host","smtp.163.com");
        properties.setProperty("mail.smtp.port", "465");
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.socketFactory.port", "465");
        properties.setProperty("mail.smtp.auth","true");
        System.out.println(properties);
        
	}*/
}
