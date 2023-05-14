package com.sense.iam.portal.action;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import com.sense.core.security.Md5;
import com.sense.core.util.CurrentAccount;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.Config;
import com.sense.iam.portal.util.DESUtil;
import com.sense.iam.service.SysConfigService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class SSOloginAction extends BaseAction {

    @Resource
    private SysConfigService sysConfigService;

    public String getSysConfig(String name){
        Config secrectConfig = new Config();
        secrectConfig.setIsControl(false);
        secrectConfig.setName(name);
        secrectConfig = sysConfigService.findByObject(secrectConfig);
        if(secrectConfig != null){
            return secrectConfig.getValue();
        }
        return null;
    }

    //红海hr
    @ResponseBody
    @RequestMapping("hrLogin.action")
    public ResultCode gethrLogin(@RequestParam String name){
        try {
            CurrentAccount currentAccount =  CurrentAccount.getCurrentAccount();
            if(currentAccount.getUserId() == null){
                return new ResultCode(Constants.OPERATION_FAIL,"您还未登录，请先登录!");
            }

            String appSecrect = getSysConfig("HR_APP_SECRECT");
            String token_url = getSysConfig("HR_TOKEN_URL");
            String login_url = getSysConfig("HR_LOGIN_URL");
            log.info("HR appSecrect : " + appSecrect);
            log.info("HR token_url : " + token_url);
            log.info("HR login_url : " + login_url);

            long timestamp = new Date().getTime();
            Map tokenParam = new HashMap();
            tokenParam.put("loginId",name);
            tokenParam.put("loginIdType","BASECODE");
            tokenParam.put("timestamp",""+timestamp);
            tokenParam.put("sign", Md5.encodeStr(appSecrect+"&"+name+"&"+timestamp));
            String tokenRes = HttpUtil.get(token_url, tokenParam);
            log.info("HR tokenRes : " + tokenRes);

            JSONObject tokenJ = JSONObject.fromObject(tokenRes);
            String state = tokenJ.getString("state");

            if(!"1".equals(state)) {
                return new ResultCode(Constants.OPERATION_FAIL,"获取token失败:"+tokenJ.getString("meg"));
            }
            String token = tokenJ.getString("result");

            return new ResultCode(Constants.OPERATION_SUCCESS, login_url + token);
        } catch (Exception e){
            e.printStackTrace();
            return new ResultCode(Constants.OPERATION_FAIL,"请求错误!");
        }
    }


    //云学堂
    @ResponseBody
    @RequestMapping("yxtLogin.action")
    public ResultCode yxtLogin(@RequestParam String name){
        try {
            CurrentAccount currentAccount =  CurrentAccount.getCurrentAccount();
            if(currentAccount.getUserId() == null){
                return new ResultCode(Constants.OPERATION_FAIL,"您还未登录，请先登录!");
            }

            String apiKey = getSysConfig("YXT_API_KEY");
            String appSecrect = getSysConfig("YXT_APP_SECRECT");
            String token_url = getSysConfig("YXT_TOKEN_URL");
            String salt = getSysConfig("YXT_SALT");
            log.info("YXT apiKey : " + apiKey);
            log.info("YXT appSecrect : " + appSecrect);
            log.info("YXT token_url : " + token_url);
            log.info("YXT salt : " + salt);


            Map param = new HashMap();
            param.put("apikey",apiKey);
            param.put("salt", salt);
            //sha256加密
            param.put("signature", DigestUtil.sha256Hex(appSecrect + salt));
            param.put("uname", name);
            String res = HttpUtil.post(token_url, param);
            log.info("yxt res : " + res);

            JSONObject json = JSONObject.fromObject(res);
            String code = json.getString("code");

            if(!"0".equals(code)) {
                return new ResultCode(Constants.OPERATION_FAIL,"登录失败："+json.getString("message"));
            }

            return new ResultCode(Constants.OPERATION_SUCCESS, json.getString("data"));
        } catch (Exception e){
            e.printStackTrace();
            return new ResultCode(Constants.OPERATION_FAIL,"请求错误!");
        }
    }

    //合同系统
    @ResponseBody
    @RequestMapping("sghtLogin.action")
    public ResultCode getHtLogin(@RequestParam String name){
        try {
            CurrentAccount currentAccount =  CurrentAccount.getCurrentAccount();
            if(currentAccount.getUserId() == null){
                return new ResultCode(Constants.OPERATION_FAIL,"您还未登录，请先登录!");
            }

            String key = getSysConfig("SGHT_KEY");
            String clientId = getSysConfig("SGHT_CLIENT_ID");
            String loginUrl = getSysConfig("SGHT_LOGIN_URL");
            log.info("SGHT key : " + key);
            log.info("SGHT clientId : " + clientId);
            log.info("SGHT loginUrl : " + loginUrl);

            long timestamp = new Date().getTime();
            String code = "username="+name+"&ts="+timestamp;
            String token = DESUtil.encrypt(key,code);
            log.info("SGHT login token : " + token);

            return new ResultCode(Constants.OPERATION_SUCCESS, loginUrl +"?clientId="+clientId+"&token=" + token);
        } catch (Exception e){
            e.printStackTrace();
            return new ResultCode(Constants.OPERATION_FAIL,"请求错误!");
        }
    }

    //甄云供应链
    @ResponseBody
    @RequestMapping("srmLogin.action")
    public ResultCode srmLogin(@RequestParam String name){
        try {
            CurrentAccount currentAccount =  CurrentAccount.getCurrentAccount();
            if(currentAccount.getUserId() == null){
                return new ResultCode(Constants.OPERATION_FAIL,"您还未登录，请先登录!");
            }

            String key = getSysConfig("SRM_PRIVATE_KEY");
            String finalUrl = getSysConfig("SRM_FINAL_URL");
            String loginUrl = getSysConfig("SRM_LOGIN_URL");
            log.info("SRM key : " + key);
            log.info("SRM finalUrl : " + finalUrl);
            log.info("SRM loginUrl : " + loginUrl);

            long timestamp = new Date().getTime();
            String contentWithTime = name + "|" + String.valueOf(timestamp);
            Cipher cipher = Cipher.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key)));
            cipher.init(1, privateKey);
            String username = Base64.getUrlEncoder().encodeToString(cipher.doFinal(contentWithTime.getBytes(StandardCharsets.UTF_8)));
            log.info("SRM login username : " + username);

            return new ResultCode(Constants.OPERATION_SUCCESS, loginUrl +"?username="+username+"&redirectUri="+finalUrl);
        } catch (Exception e){
            e.printStackTrace();
            return new ResultCode(Constants.OPERATION_FAIL,"请求错误!");
        }
    }
}
