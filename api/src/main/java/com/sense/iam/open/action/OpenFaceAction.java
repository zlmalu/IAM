package com.sense.iam.open.action;

import javax.annotation.Resource;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.BaiduAiUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.api.model.FaceRep;
import com.sense.iam.cache.SysConfigCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.im.User;
import com.sense.iam.service.ImageService;
import com.sense.iam.service.UserService;

@Api(value = "API - 人脸识别接口", tags = "人脸识别接口")
@Controller
@RestController
@RequestMapping("/face")
public class OpenFaceAction extends BaseAction{
	
	@Resource
	private ImageService imageService;
	
	@Resource
	private UserService userService;
	
	/**
	 * 保存人脸数据
	 * @return
	 * description :  
	 * ygd
	 */
	@ApiOperation(value="保存人脸识别图片", notes="保存人脸识别图片")
	@RequestMapping(value = "/save", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode save(@RequestBody FaceRep faceRep){
		String data = faceRep.getData();
		String sn = faceRep.getSn();
		if(StringUtils.isEmpty(sn) || StringUtils.isEmpty(data)){
			return new ResultCode(Constants.OPERATION_FAIL,"缺失注册信息!");
		}
		if(BaiduAiUtil.AccessToken.equals("")){
			BaiduAiUtil.initAuth(SysConfigCache.FACE_KEY,SysConfigCache.FACE_SECURET);
    	}
		JSONObject result=JSONObject.fromObject(BaiduAiUtil.detect(data));
		if(result.getInt("error_code")==0){
			//获取当前用户ID
			User user=new User();
			user.setSn(sn);
			user=userService.findByObject(user);
			if(user!=null){
				Image image=new Image();
				image.setContentByte(Base64.decode(data));
				image.setContentType("image/jpeg");
				image.setId(user.getId());
				image.setOid(user.getId());
				imageService.save(image);
			}
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}else{
			return new ResultCode(Constants.OPERATION_FAIL);
		}
		
	}


}
