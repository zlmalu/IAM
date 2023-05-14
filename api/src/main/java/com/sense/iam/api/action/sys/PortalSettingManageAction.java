package com.sense.iam.api.action.sys;

import com.sense.core.util.StringUtils;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.sys.PortalSettingManageReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.config.RedisCache;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.sys.PortalSettingManage;
import com.sense.iam.service.PortalSettingManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiSort;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.*;
import java.util.List;


@Api(tags = "应用面板")
@Controller
@RestController
@RequestMapping("sys/PortalSettingManage")
@ApiSort(value = 23)
public class PortalSettingManageAction extends AbstractAction<PortalSettingManage, Long> {

	@Resource
	private PortalSettingManageService portalSettingManageService;
	@Resource
	private RedisCache redisCache;

	@ApiOperation(value = "查询全部")
	@RequestMapping(value = "findByAll", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public List<PortalSettingManage> findByAll() {
		return portalSettingManageService.findAll();
	}

	@ApiOperation(value="新增/编辑",code=0)
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody PortalSettingManageReq entity) {
		ResultCode code=null;
		try {
			PortalSettingManage model=entity.getPortalSettingManage();

			if(!StringUtils.isEmpty(entity.getTemploginSlideImage1())){
				model.setLoginSlide1Image(getRedisImage(entity.getTemploginSlideImage1()));
			}
			if(!StringUtils.isEmpty(entity.getTemploginSlideImage2())){
				model.setLoginSlide2Image(getRedisImage(entity.getTemploginSlideImage2()));
			}
			if(!StringUtils.isEmpty(entity.getTemploginSlideImage3())){
				model.setLoginSlide3Image(getRedisImage(entity.getTemploginSlideImage3()));
			}
			if(!StringUtils.isEmpty(entity.getTemplogoImage())){
				model.setLogo1Image(getRedisImage(entity.getTemplogoImage()));
			}
			List<PortalSettingManage> findAll = portalSettingManageService.findAll();

			if(findAll!=null&&findAll.size()>0){
				model.setId(findAll.get(0).getId());
				code = super.edit(model);
			}else{
				code = super.save(model);
			}
			if(code != null&&code.getSuccess()){
				findAll = portalSettingManageService.findAll();
				if(findAll!=null&&findAll.size()>0){
					putPortalSettingManage(findAll.get(0));
				}
			}
		} catch (Exception e) {
			return new ResultCode(Constants.OPERATION_FAIL);
		}
		return code;
	}


	/**
	 * 获取redis图片对象
	 * @param tempKey
	 * @return
	 */
	private Image getRedisImage(String tempKey){
		try {
			//获取图片信息
			String value=redisCache.getCacheObject(tempKey);
			if(value!=null){
				ObjectInputStream oos=new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value.getBytes())));
				Image image= (Image)oos.readObject();
				redisCache.deleteObject(tempKey);
				return image;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 添加redis
	 * @param portalSettingManage
	 * @return
	 */
	private void putPortalSettingManage(PortalSettingManage portalSettingManage){
		//存储redis
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(portalSettingManage);
			oos.close();
			String value = new String(Base64.encode(bos.toByteArray()));
			redisCache.setCacheObject(Constants.PORTAL_SETTING_MANAGE_KEY,value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
