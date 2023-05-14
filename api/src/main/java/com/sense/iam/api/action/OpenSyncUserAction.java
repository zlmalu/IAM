package com.sense.iam.api.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.model.BaseModel;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.im.ResponseData;
import com.sense.iam.api.model.im.UParameter;
import com.sense.iam.api.model.im.UParameterInfo;
import com.sense.iam.api.model.im.UserResp;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.dao.AccountDao;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.User;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.SysAcctService;
import com.sense.iam.service.UserMultiOrgService;
import com.sense.iam.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;



@Api(value = "API - 数据同步", tags = "数据同步")
@Controller
@RestController
@RequestMapping("im/data/sync")
@ApiSort(value = 200)
public class OpenSyncUserAction extends  AbstractAction<User,Long>{
	
	@Resource
	private SysAcctService sysAcctService;
	
	@Resource
	private ImageCache imageCache;
	
	@Resource
	private AppService appService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private UserMultiOrgService userMultiOrgService;
	
	@Resource
	private JdbcService jdbcService;
	
	@Resource
	private AccountService accountService;
	
	@Resource
	private AccountDao accountDao;
	
	@ApiOperation(value="根据应用编码获取应用帐号")
	@RequestMapping(value="/userList", method=RequestMethod.POST)
	@ResponseBody
	public PageList<User> userList(@RequestBody UParameter parm){
		log.info("userList:"+parm.toString());
		App entity=new App();
		entity.setSn(parm.getAppSn());
		entity.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
		if(parm.getStarttime()==null||parm.getEndtime()==null){
			entity.setFilterEndTime(null);
			entity.setFilterStartTime(null);
		}else{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//如果格式化时间有问题，则不进行数据格式化，默认没有时间限制
			try{
				df.parse(parm.getStarttime());
				df.parse(parm.getEndtime());
				entity.setFilterStartTime(parm.getStarttime());
				entity.setFilterEndTime(parm.getEndtime());
			}catch(Exception e){
				e.printStackTrace();
				entity.setFilterEndTime(null);
				entity.setFilterStartTime(null);
			}
		}
		return userService.findSyncUserPage(entity, parm.getPage(), parm.getPageSize());
	}


	
	@ApiOperation(value="根据应用编码和用户ID获取用户信息和帐号信息和权限信息")
	@RequestMapping(value="/findUserAndAccAndFunc", method=RequestMethod.POST)
	@ResponseBody
	public ResponseData findUserAndAccAndFunc(@RequestBody UParameterInfo uParameterInfo){
		log.info("uParameterInfo:"+uParameterInfo.toString());
		if(uParameterInfo.getUserId()==null||uParameterInfo.getUserId().longValue()==0){
			return new ResponseData(-1);
		}
		
		User u=userService.findById(uParameterInfo.getUserId());
		if(u==null){
			return new ResponseData(-1);
		}
		//设置兼职组织对象
		List<Org> multiorgs=userMultiOrgService.findOrgByUserId(uParameterInfo.getUserId());
		u.setMultiOrgs(multiorgs);
		String multiOrgsString="";
		for(int j=0;j<multiorgs.size();j++){
			if(j+1==multiorgs.size()){
				multiOrgsString+=multiorgs.get(j).getName();
			}else{
				multiOrgsString+=multiorgs.get(j).getName()+";";
			}
		}
		u.setMultiOrgsString(multiOrgsString);
		//设置岗位对象对象
		List<Map<String, Object>> polistMap=jdbcService.findList("SELECT a.SN,a.NAME,b.TYPE FROM IM_POSITION a left join IM_USER_POSITION b on b.POSITION_ID=a.ID where b.USER_ID="+u.getId());
		u.setPositions(polistMap);
		List<Account> list=new ArrayList<Account>();
		if(uParameterInfo.getAppSn()!=null||uParameterInfo.getAppSn().length()>0){
			Account entity=new Account();
			entity.setAppSn(uParameterInfo.getAppSn());
			entity.setCompanySn(CurrentAccount.getCurrentAccount().getCompanySn());
			entity.setUserId(u.getId());
			entity.setIsControl(false);
			entity.setStatus(null);
			list=accountService.findList(entity);
			if(list!=null&&list.size()>0){
				for(int i=0;i<list.size();i++){
					//设置扩展字段
					list.get(i).setExtraAttrs(accountDao.findById(list.get(i).getId()).getExtraAttrs());
					list.get(i).setAppFuncs(accountDao.findAppFunc(list.get(i).getId()));
					//密码设置空值
					list.get(i).setLoginPwd("");
				}
			}
		}
		return new ResponseData(0, u, list);
	}
	

}
