package org.iam.compoment.sync.ekp;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;



import com.alibaba.fastjson.JSONObject;
import com.sense.core.util.StringUtils;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
@Name("EKP同步")
public class SyncApi implements SyncInteface
{
  protected Log log = LogFactory.getLog(getClass());
  @Param("url")
  private String url = "http://172.16.67.55:8001/sys/webservice/sysSynchroSetOrgWebService";
  @Param("接口用户名")
  private String userName = "test1";
  @Param("接口密码")
  private String password = "123456";
  
  public ResultCode execute(String content)
  {
    this.log.info("EKP下推内容：" + content);
    JSONArray jsonArray = JSONArray.fromObject(content);
    if(jsonArray!=null&&jsonArray.size()>0){
    	for(int i=0;i<jsonArray.size();i++){
		    if (jsonArray.getJSONObject(i).containsKey("password")){
		    	String password1 = jsonArray.getJSONObject(i).getString("password");
			    if (!StringUtils.isEmpty(password1)) {
			    	jsonArray.getJSONObject(i).remove("password");
			    	jsonArray.getJSONObject(i).put("password", Base64.encode(password1));
			    }
		    }
    	}
    }
    String params = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"http://sys.webservice.client\" xmlns:in=\"http://in.webservice.organization.sys.kmss.landray.com/\">";
    params = params + " <soapenv:Header>";
    params = params + " <tns:RequestSOAPHeader>";
    params = params + " <tns:user>" + this.userName + "</tns:user>";
    params = params + " <tns:password>" + this.password + "</tns:password>";
    params = params + " </tns:RequestSOAPHeader>";
    params = params + " </soapenv:Header>";
    params = params + "<soapenv:Body>";
    params = params + "<in:syncOrgElementsBaseInfo>";
    params = params + "<arg0>";
    params = params + "<appName>IAM</appName>";
    params = params + "<orgJsonData><![CDATA[" + jsonArray.toString() + "]]></orgJsonData>";
    params = params + "<orgSyncConfig></orgSyncConfig>";
    params = params + "</arg0>";
    params = params + "</in:syncOrgElementsBaseInfo>";
    params = params + "</soapenv:Body>";
    params = params + "</soapenv:Envelope>";
    this.log.info("EKP下推参数：" + params);
    HttpRequest post = HttpUtil.createPost(this.url);
    post.body(params, "text/xml;charset=UTF-8");
    post.timeout(30000);
    HttpResponse response = post.execute();
    String result = response.body();
    response.close();
    this.log.info("返回结果：" + result);
    if (!StrUtil.isAllBlank(new CharSequence[] { result }))
    {
      String pattern = "<returnState>2</returnState>";
      Pattern r = Pattern.compile(pattern);
      Matcher m = r.matcher(result);
      
      String msgPatternStr = "<return>((.*))</return>";
      Pattern msgPattern = Pattern.compile(msgPatternStr);
      Matcher msgMatcher = msgPattern.matcher(result);
      if (m.find())
      {
        if (msgMatcher.find()) {
        	    params="";
        	    params = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"http://sys.webservice.client\" xmlns:in=\"http://in.webservice.organization.sys.kmss.landray.com/\">";
        	    params = params + " <soapenv:Header>";
        	    params = params + " <tns:RequestSOAPHeader>";
        	    params = params + " <tns:user>" + this.userName + "</tns:user>";
        	    params = params + " <tns:password>" + this.password + "</tns:password>";
        	    params = params + " </tns:RequestSOAPHeader>";
        	    params = params + " </soapenv:Header>";
        	    params = params + "<soapenv:Body>";
        	    params = params + "<in:syncOrgElements>";
        	    params = params + "<arg0>";
        	    params = params + "<appName>IAM</appName>";
        	    params = params + "<orgJsonData><![CDATA[" + jsonArray.toString() + "]]></orgJsonData>";
        	    params = params + "<orgSyncConfig>{\"persion\":[\"password\",\"name\",\"loginName\",\"email\",\"mobileNo\",\"no\",\"isAvailable\",\"order\",\"posts\",\"parent\"]}</orgSyncConfig>";
        	    params = params + "</arg0>";
        	    params = params + "</in:syncOrgElements>";
        	    params = params + "</soapenv:Body>";
        	    params = params + "</soapenv:Envelope>";
        	    post = HttpUtil.createPost(this.url);
        	    post.body(params, "text/xml;charset=UTF-8");
        	    post.timeout(30000);
        	    response = post.execute();
          return new ResultCode(1, "推送EKP成功");
        }
      }
      else
      {
        if (msgMatcher.find())
        {
          String ret = msgMatcher.group(0);
          return new ResultCode(2, ret);
        }
        String ret = result.substring(result.indexOf("<faultstring>") + 13, result.indexOf("</faultstring>"));
        return new ResultCode(2, ret);
      }
    }
    return new ResultCode(2, result);
  }
  
  public static void main(String[] args)
  {
    String str = "</faultcode><faultstring>组织架构人员登陆名重复：caoweiping</faultstring><detail>";
    str = str.substring(str.indexOf("<faultstring>") + 13, str.indexOf("</faultstring>"));
    System.out.println(str);
  }
}
