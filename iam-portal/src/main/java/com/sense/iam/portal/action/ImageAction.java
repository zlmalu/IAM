package com.sense.iam.portal.action;

import com.sense.core.util.ByteArrayJarClassLoader;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.sys.PortalSettingManage;
import com.sense.iam.service.ImageService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import java.io.*;


@Controller
@RequestMapping("/image")
public class ImageAction extends BaseAction {

	protected Log log=LogFactory.getLog(getClass());

	@Resource
	private ImageCache imageCache;

	@Resource
	private ImageService imageService;


	@ResponseBody
	@RequestMapping(value="/showImage/{type}/{number}", method= RequestMethod.GET)
	public void showImage(@PathVariable String type, @PathVariable Long number){
		try{
			String imType = "image/png";
			byte[] bytes=null;
			if(type.equals("user")){
				Image image = new Image();
				image.setOid(number);
				image = imageService.findByObject(image);
				//当未获取到图片时加载默认图片
				if(null==image){
					bytes=ByteArrayJarClassLoader.loadClassImage("static/images/userDefault.png");
					response.setContentType(imType);
					assert bytes != null;
					response.getOutputStream().write(bytes);
					response.getOutputStream().flush();
				}
				else{
					imageCache.readAppImage(response.getOutputStream(), number);
				}
				return;
			}
			PortalSettingManage portalSettingManage=getPortalSettingManage();
			//判断门户设置是否存在，否不存在则显示本地静态缓存
			if(portalSettingManage==null){
				//判断是否幻灯片
				if(type.equals("loginSlide")){
					//加载本地资源文件
					bytes=ByteArrayJarClassLoader.loadClassImage("static/images/bg/loginSlide"+number+".png");
				}
				//判断是否logo
				else if(type.equals("logo")){
					//加载本地资源文件
					bytes=ByteArrayJarClassLoader.loadClassImage("static/images/logo/logo.png");
				}
				response.setContentType(imType);
				assert bytes != null;
				response.getOutputStream().write(bytes);
				response.getOutputStream().flush();
			}else {
				//判断是否幻灯片
				if (type.equals("loginSlide")) {
					Long loginSlideImage = 0L;
					if (number == 1) {
						loginSlideImage = portalSettingManage.getLoginSlideImage1();
					} else if (number == 2) {
						loginSlideImage = portalSettingManage.getLoginSlideImage2();
					} else if (number == 3) {
						loginSlideImage = portalSettingManage.getLoginSlideImage3();
					}
					Image image = new Image();
					image.setOid(loginSlideImage);
					image = imageService.findByObject(image);
					//当未获取到图片时加载默认图片
					if(null==image){
						bytes=ByteArrayJarClassLoader.loadClassImage("static/images/bg/loginSlide"+number+".png");
						response.setContentType(imType);
						assert bytes != null;
						response.getOutputStream().write(bytes);
						response.getOutputStream().flush();
					}
					else{
						imageCache.readAppImage(response.getOutputStream(), portalSettingManage.getLoginSlideImage1());
					}
				}
				//判断是否LOGO
				else if (type.equals("logo")) {
					Long logoImage = portalSettingManage.getLogoImage();
					Image image = new Image();
					image.setOid(logoImage);
					image = imageService.findByObject(image);
					//当未获取到图片时加载默认图片
					if(null==image){
						bytes=ByteArrayJarClassLoader.loadClassImage("static/images/logo/logo.png");
						response.setContentType(imType);
						assert bytes != null;
						response.getOutputStream().write(bytes);
						response.getOutputStream().flush();
					}
					else{
						imageCache.readAppImage(response.getOutputStream(), portalSettingManage.getLogoImage());
					}
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try {
				response.getOutputStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	@RequestMapping("/viewImage")
	@ResponseBody
	public void viewImage(Long oid) throws IOException {
		Image image = new Image();
		image.setOid(oid);
		image = imageService.findByObject(image);
		//当未获取到图片时加载默认图片
		if(null==image){
			byte[] bytes=ByteArrayJarClassLoader.loadClassImage("static/images/appDefault.png");
			response.setContentType("image/png");
			assert bytes != null;
			response.getOutputStream().write(bytes);
			response.getOutputStream().flush();
		}
		else{
			imageCache.readAppImage(response.getOutputStream(),oid);
		}
	}
}
