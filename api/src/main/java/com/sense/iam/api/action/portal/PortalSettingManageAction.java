/*
package com.sense.iam.api.action.portal;

import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.sys.SettingManage;
import com.sense.iam.service.ImageService;
import com.sense.iam.service.PortalSettingManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Api(tags = "门户配置")
@Controller
@RestController
@RequestMapping("portal")
public class PortalSettingManageAction extends AbstractAction<SettingManage,Long> {
	


	@Resource
	protected PortalSettingManageService portalSettingManageService;
	
	//定义内存变量
	public static Map<String, Image> TEMP_IMAGES=new HashMap<String, Image>();

	@Resource
	private ImageService imageService;

	@ApiOperation(value="显示图像")
	@RequestMapping(value="viewImage/{type}/{oid}", method=RequestMethod.GET)
	public void viewImage(@PathVariable Long oid,@PathVariable String type){
		//从内存中读取图片
		Image image=imageCache.imageMap.get(oid);
		if(image==null && oid!=null){//从image表中读取图片
			Image queryImage=new Image();
			queryImage.setOid(oid);
			image=imageService.findByObject(queryImage);
			if(image!=null){
				imageCache.imageMap.put(oid, image);
			}
		}
		//判断图片是否存在，如果不存在显示临时图片
		if(image==null){
			try {
				if(type!=null){
					String redirectUrl=GatewayHttpUtil.getKey("RemoteServer", request)+"/api/app.svg?type="+type+"&r="+StringUtils.getSecureRandomnNumber();
					redirectUrl = redirectUrl.replaceAll("\r", "%0D");//Encode \r to url encoded value
					redirectUrl = redirectUrl.replaceAll("\n", "%0A");//Encode \n to url encoded value
					response.sendRedirect(redirectUrl);
					return;
				}else{
					String redirectUrl=GatewayHttpUtil.getKey("RemoteServer", request)+"/api/app.svg?r="+StringUtils.getSecureRandomnNumber();
					redirectUrl = redirectUrl.replaceAll("\r", "%0D");//Encode \r to url encoded value
					redirectUrl = redirectUrl.replaceAll("\n", "%0A");//Encode \n to url encoded value
					response.sendRedirect(redirectUrl);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			try{
				if(image.getContent()!=null && image.getContentType()!=null){
					response.setContentType(image.getContentType());
					response.getOutputStream().write(image.getContent());
					response.getOutputStream().flush();
				}
				return;
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try {
					response.getOutputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	

	
	@ApiOperation(value="保存配置")
	@RequestMapping(value="setting/save", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode save(@RequestBody SettingManage entity) {
		return super.save(entity);
	}

	@ApiOperation(value="上传获取临时KEY")
	@RequestMapping(value="upLoadImage", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode uploadImage(@ApiParam(name="file",value="图片",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file){
		try {
			Image image=new Image();
			image.setContentType(file.getContentType());
			image.setContentByte(file.getBytes());
			String tempImageId="temp_"+UUID.randomUUID().toString().replaceAll("-", "");
			//放入内存保存,记录临时图片个数不能超过五，
			if(TEMP_IMAGES.size() >= 5){
				TEMP_IMAGES.clear();
			}
			TEMP_IMAGES.put(tempImageId, image);
			return new ResultCode(Constants.OPERATION_SUCCESS,tempImageId);
		} catch (IOException e) {
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	
}
*/
