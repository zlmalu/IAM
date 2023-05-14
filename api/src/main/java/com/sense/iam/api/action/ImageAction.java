package com.sense.iam.api.action;

import com.sense.iam.config.RedisCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Image;

@Api(tags = "图片上传")
@Controller
@RestController
@RequestMapping("image")
public class ImageAction extends BaseAction{

	@Resource
	protected ImageCache imageCache;

	@ApiOperation(value="显示图像")
	@RequestMapping(value="viewImage/{type}/{oid}", method=RequestMethod.GET)
	public void viewImage(@PathVariable Long oid,@PathVariable String type){
		try{
			imageCache.readAppImage(response.getOutputStream(), oid);
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

	@Resource
	private RedisCache redisCache;

	@ApiOperation(value="上传获取临时KEY")
	@RequestMapping(value="upLoadImage", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode uploadImage(@ApiParam(name="file",value="图片") @RequestParam(required=false,name="file",value="file") MultipartFile file){
		try {
			Image image=new Image();
			image.setContentType(file.getContentType());
			image.setContentByte(file.getBytes());
			String tempImageId="temp_"+UUID.randomUUID().toString().replaceAll("-", "");
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			try {
				ObjectOutputStream oos=new ObjectOutputStream(bos);
				oos.writeObject(image);
				oos.close();
				String value=new String(Base64.encode(bos.toByteArray()));
				//图片存放redis10分钟过期
				redisCache.setCacheObject(tempImageId, value, 10 * 60 * 1000,TimeUnit.MILLISECONDS);
				return new ResultCode(Constants.OPERATION_SUCCESS,tempImageId);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new ResultCode(Constants.OPERATION_FAIL);
		} catch (IOException e) {
			log.error("save error:",e);
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
}
