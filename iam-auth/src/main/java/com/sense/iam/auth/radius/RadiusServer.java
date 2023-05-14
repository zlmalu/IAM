package com.sense.iam.auth.radius;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.Radius;


@Component
public class RadiusServer implements Runnable {

	private static HashMap<String, String[]> clientList = new HashMap<String,String[]>();// 客户端配置参数列表

	@Value("${auth.address}")
	private String authAddress;
	
	private boolean isRun = false;

	private DatagramSocket server = null;

	public RadiusServer() {

	}
	
	public void addclient(Radius radius){
		String[] temp = new String[5];
		temp[0] = Tool.ByteToHex(radius.getSharedSecret().getBytes());
		temp[1] = radius.getManager();
		temp[2] = radius.getPassword();
		temp[3] = radius.getAppId()+"";
		temp[4] = "";
		RadiusServer.clientList.put(radius.getIp(), temp);
	}

	public void finalize() {
		stopRadius();
	}

	// 运行
	public void run() {
		
		try {
			server = new DatagramSocket(Integer.valueOf(SysConfigCache.RADIUS_SERVER_PORT));
			isRun = true;
			System.out.println("radius listener run");
			while (isRun) {
				try {
					byte[] buf = new byte[4096];
					DatagramPacket in = new DatagramPacket(buf, 4096);
					server.receive(in);
					new ReceiveThread(this, in);
				} catch (Exception e1) {
					Tool.writeLog("error", e1.toString());
				}
			}
			
		} catch (Exception e) {
		}
	}

	public void startRadius(){
		System.out.println("changeList==="+RadiusServer.clientList);
		if(!isRun){
			new Thread(this).start();
		}
	}

	// 停止
	public void stopRadius() {
		isRun = false;
		try {
			if (server != null) {
				server.close();
				server = null;
			}
		} catch (Exception e) {
		}
		try {
			if (RadiusServer.clientList != null) {
				RadiusServer.clientList.clear();
			}
		} catch (Exception e) {
		}
	}

	// 接收数据
	public void receive(DatagramPacket in) {
		try {
			String ip = in.getAddress().getHostAddress();
			int port = in.getPort();
			// byte[] inData = new byte[in.getLength()];
			// System.arraycopy(in.getData(), 0, inData, 0, inData.length);
			byte[] inData = in.getData();
			// 包类型
			int code = inData[0];
			// 包标识符：用于匹配请求包和响应包
			int identifier = inData[1];
			// 包长度：code、identifier、length、authenticator、attributes的长度总和，有效范围是20~4096
			int length = in.getLength();
			System.out.println("总长度：" + length);
			// 验证字：
			// (1)请求验证字(Request Authenticator)，用在请求报文中，必须为全局唯一的随机值
			// (2)响应验证字(Response Authenticator)，用在响应报文中，用于鉴别响应报文的合法性，
			// 响应验证字=MD5(Code+ID+Length+请求验证字+Attributes+SharedSecret)
			String authenticator = Tool.ByteToHex(inData, 4, 20);
			// 属性域
			String attributes = Tool.ByteToHex(inData, 20, length);
			Tool.writeLog("接收", "ip=" + ip + ",port=" + port + ",code=" + code + ",identifier=" + identifier + ",length=" + length + ",authenticator=" + authenticator + ",attributes=" + attributes);
			String[][] attributesList = null;
			if ((attributes != null) && (attributes.length() > 0)) {
				attributesList = Tool.getAttributes(attributes);
			}
			byte[] outData = optionData(code, ip, port, identifier, authenticator, attributesList);
			if (outData != null) {
				DatagramPacket out = new DatagramPacket(outData, outData.length, in.getSocketAddress());
				server.send(out);
				Tool.writeLog(ip, "发送完成！");
			}
		} catch (Exception e) {
			Tool.writeLog("error", e.toString());
		}
	}

	private byte[] optionData(int code, String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			switch (code) {
			case 1:// Access-Request(认证请求数据包)
			{
				Tool.writeLog(ip, ">>Access-Request");
				ret = accessRequest(ip, port, identifier, authenticator, attributesList);
			}
				break;
			case 2:// Access-Accept(认证响应数据包)
			{
				Tool.writeLog(ip, ">>Access-Accept");
				ret = accessAccept(ip, port, identifier, authenticator, attributesList);
			}
				break;
			case 3:// Access-Reject(认证拒绝数据包)
			{
				Tool.writeLog(ip, ">>Access-Reject");
				ret = accessReject(ip, port, identifier, authenticator, attributesList);
			}
				break;
			case 4:// Accounting-Request(计费请求数据包)
			{
				Tool.writeLog(ip, ">>Accounting-Request");
				ret = accountingRequest(ip, port, identifier, authenticator, attributesList);
			}
				break;
			case 5:// Accounting-Response(计费响应数据包)
			{
				Tool.writeLog(ip, ">>Accounting-Response");
				ret = accountingResponse(ip, port, identifier, authenticator, attributesList);
			}
				break;
			case 11:// Access-Challenge()
			{
				Tool.writeLog(ip, ">>Access-Challenge");
				ret = accessChallenge(ip, port, identifier, authenticator, attributesList);
			}
				break;
			case 12:// Status-Server(试验阶段)
			{
				Tool.writeLog(ip, ">>Status-Server");
				ret = statusServer(ip, port, identifier, authenticator, attributesList);
			}
				break;
			case 13:// Status-Client(试验阶段)
			{
				Tool.writeLog(ip, ">>Status-client");
				ret = statusClient(ip, port, identifier, authenticator, attributesList);
			}
				break;
			case 255:// Reserved(保留)
			{
				Tool.writeLog(ip, ">>Reserved");
				ret = reserved(ip, port, identifier, authenticator, attributesList);
			}
				break;
			default: {
				Tool.writeLog(ip, "code错误！(" + code + ")");
			}
				break;
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Access-Request(认证请求数据包)
	private byte[] accessRequest(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			String name = null;
			String password = null;
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					String value = Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
					switch (type) {
					case 1:// User-Name(用户账户ID)
					{
						name = value;
					}
						break;
					case 2:// User-Password(用户密码)
					{
						password = value;
					}
						break;
					case 3:// Chap-Password()
					{
						password = value;
					}
						break;
					case 4:// Nas-IP-Address(Nas的ip地址)
					{
						// ip = value;
					}
						break;
					case 5:// Nas-Port(用户接入端口号)
					{
						// port = Integer.parseInt(value);
					}
						break;
					default:
						break;
					}
				} catch (Exception e1) {
				}
			}
			if ((name != null) && (password != null) && (ip != null)) {
				String[] client = RadiusServer.clientList.get(ip);// 取密钥
				if (client != null) {
					String sharedSecret = client[0];
					if (sharedSecret != null) {
						Tool.writeLog("Access-Request", "ip=" + ip + ",port=" + port + ",name=" + name + ",password=" + password + ",sharedSecret=" + sharedSecret);
						password = Tool.decodeMD5(sharedSecret, authenticator, password);
						System.out.println("username="+name+"password================="+password);
						String result = null;

						LinkedMultiValueMap<String, Object> paramEntity = new LinkedMultiValueMap<String, Object>();  
						paramEntity.add("username", name);
						paramEntity.add("password", password);
						paramEntity.add("reamId", "1002");
						
						ResponseEntity<ResultCode> responseEntity=new RestTemplate().postForEntity(authAddress, paramEntity, ResultCode.class);
						if(responseEntity.getBody().getSuccess()){
							result="0";
						}else{
							result="1";
						}

						String attributes = null;
						int code = 0;
						if (result != null) {
							if (result.equals("0")) {
								name = "Access-Accept";
								attributes = "";
								code = 2;
								Tool.writeLog(ip, "验证成功！");
							} else {
								name = "Access-Reject";
								attributes = "";// Tool.getAttributeString(18,
								// "验证错误！");
								code = 3;
								Tool.writeLog(ip, "验证错误！");
							}
						} else {
							name = "Access-Reject";
							attributes = "";// Tool.getAttributeString(18,
							// "验证失败！");
							code = 3;
							Tool.writeLog(ip, "验证失败！");
						}
						ret = Tool.getOutData(name, sharedSecret, ip, port, code, identifier, authenticator, attributes);
					} else {
						Tool.writeLog(ip, "密钥为空！");
					}
				} else {
					Tool.writeLog(ip, "取密钥失败！");
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Access-Accept(认证响应数据包)
	private byte[] accessAccept(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
				} catch (Exception e1) {
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Access-Reject(认证拒绝数据包)
	private byte[] accessReject(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
				} catch (Exception e1) {
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Accounting-Request(计费请求数据包)
	private byte[] accountingRequest(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
				} catch (Exception e1) {
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Accounting-Response(计费响应数据包)
	private byte[] accountingResponse(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
				} catch (Exception e1) {
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Access-Challenge()
	private byte[] accessChallenge(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
				} catch (Exception e1) {
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Status-Server(试验阶段)
	private byte[] statusServer(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
				} catch (Exception e1) {
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Status-Client(试验阶段)
	private byte[] statusClient(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
				} catch (Exception e1) {
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// Reserved(保留)
	private byte[] reserved(String ip, int port, int identifier, String authenticator, String[][] attributesList) {
		byte[] ret = null;
		try {
			for (int i = 0; i < attributesList.length; i++) {
				try {
					int type = Integer.parseInt(attributesList[i][0], 16);
					Tool.getAttributeValue(ip, type, attributesList[i][1]).trim();
				} catch (Exception e1) {
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	public boolean isRun() {
		return isRun;
	}
	
	
}
