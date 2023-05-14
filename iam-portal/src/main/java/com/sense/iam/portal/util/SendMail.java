package com.sense.iam.portal.util;

import javax.mail.Authenticator;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;



import org.springframework.stereotype.Component;

import java.util.Properties;


@Component
public class SendMail {

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
    
    public static Message sendMail() throws Exception{
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host",HOST);
        properties.setProperty("mail.smtp.port", PORT);
      /*  properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");*/
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.socketFactory.port", PORT);
        properties.setProperty("mail.smtp.auth","true");
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
        return message;
    }
}
