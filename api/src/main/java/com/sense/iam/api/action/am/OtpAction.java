package com.sense.iam.api.action.am;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.OTPUtil;
import com.sense.iam.api.model.am.LoginPolicyReq;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;

@Api(tags = "动态口令")
@ApiSort(value=3)
@Controller
@RestController
@RequestMapping("am/otp")
public class OtpAction {

	@ApiOperation(value = "动态口令生成接口")
	@RequestMapping(value = "dynamicToken", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject dynamicToken(String username) {
		JSONObject json = new JSONObject();
		java.text.SimpleDateFormat format=new java.text.SimpleDateFormat("yyyyMMddHHmm");
        try {
			String token = OTPUtil.generateOTP(username.getBytes("UTF-8"),Long.parseLong(format.format(System.currentTimeMillis())),6);
			json.put("success", true);
			json.put("msg", token);
        } catch (Exception e) {
        	json.put("success", false);
			json.put("msg", e);
		} 
		return json;
	}
	
	
}
