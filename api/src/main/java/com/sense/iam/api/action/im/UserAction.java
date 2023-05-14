package com.sense.iam.api.action.im;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import com.sense.iam.config.RedisCache;
import org.apache.xmlbeans.impl.util.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sense.core.model.BaseModel;
import com.sense.core.util.ArrayUtils;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.ExcelUtils;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.core.util.PageList;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.SessionManager;
import com.sense.iam.api.action.AbstractAction;
import com.sense.iam.api.model.EnterAuthModel;
import com.sense.iam.api.model.EnterAuthOrgModel;
import com.sense.iam.api.model.IdentityViewReq;
import com.sense.iam.api.model.LogReq;
import com.sense.iam.api.model.UserMoveOrg;
import com.sense.iam.api.model.keysModel;
import com.sense.iam.api.model.im.UserAccountReq;
import com.sense.iam.api.model.im.UserAuthAccountReq;
import com.sense.iam.api.model.im.UserReq;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.Params;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.Account;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.Image;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.Position;
import com.sense.iam.model.im.User;
import com.sense.iam.model.im.UserPosition;
import com.sense.iam.model.im.UserType;
import com.sense.iam.model.sys.Field;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.MutexService;
import com.sense.iam.service.OrgService;
import com.sense.iam.service.PositionService;
import com.sense.iam.service.SysFieldService;
import com.sense.iam.service.UserMultiOrgService;
import com.sense.iam.service.UserPositionService;
import com.sense.iam.service.UserService;
import com.sense.iam.service.UserTypeService;

import cn.hutool.core.date.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;
import net.sf.json.JSONArray;


@Api(tags = "用户管理")
@Controller
@RestController
@RequestMapping("im/user")
@ApiSort(value = 4)
public class UserAction extends  AbstractAction<User,Long>{

	@Resource
	private PositionService positionService;

	@Resource
	private UserPositionService userPositionService;

	@Resource
	private UserService userService;

	@Resource
	private UserTypeService userTypeService;

	@Resource
	private SysFieldService sysFieldService;

	@Resource
	private OrgService orgService;

	@Resource
	private ImageCache imageCache;
	@Resource
	private AppService appService;
	@Resource
	private JdbcService jdbcService;
	@Resource
	private MutexService mutexService;
	@Resource
	private AccountService accountService;
	@Resource
	private UserMultiOrgService userMultiOrgService;
	@Resource
	private RedisCache redisCache;
	@Resource
	private CompanyCache companyCache;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	@ApiOperation(value="身份视图")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "userId", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="identityView/{userId}", method=RequestMethod.GET)
	@ResponseBody
	public IdentityViewReq identityView(@PathVariable Long userId) {
		IdentityViewReq identityViewReq=new IdentityViewReq();
		try{
			User u=super.findById(userId);
			if(u.getOrgId()!=null && u.getOrgId() != 0){
				//设置兼职组织对象
				identityViewReq.setMultiorgs(userMultiOrgService.findOrgByUserId(userId));

				identityViewReq.setUser(u);

				/**====================获取用户的组织===============*/
				Org org=orgService.findById(u.getOrgId());
				org.setNamePath(org.getNamePath()+org.getName());
				identityViewReq.setOrg(org);
				/**====================获取用户的组织===============*/
				//查询用户岗位信息
				List<Map<String, Object>> posData= jdbcService.findList("select name from im_position where id in(select position_id from im_user_position where user_id="+userId+")");
				if(posData!=null&&posData.size()>0){
					List<Position> posList=new ArrayList<Position>();
					for(Map<String, Object> map:posData){
						Position model=new Position();
						model.setName(map.get("name").toString());
						posList.add(model);
					}
					identityViewReq.setPositionList(posList);
				}
				//查询用户应用信息
				List<Map<String, Object>> appData= jdbcService.findList("select name from im_app where id in(select app_id from im_account where user_id="+userId+" and status=1)");
				if(appData!=null&&appData.size()>0){
					List<App> appList=new ArrayList<App>();
					for(Map<String, Object> map:appData){
						App model=new App();
						model.setName(map.get("name").toString());
						appList.add(model);
					}
					identityViewReq.setAppList(appList);
				}


				List<LogReq> logData=new ArrayList<LogReq>();
				//查询1000条系统日志
				BaseModel base=new BaseModel() {};
				PageList<?> syslogList = jdbcService.findPage("SELECT log.*,ev.NAME FROM sys_log log LEFT JOIN sys_event ev on ev.clazz=log.clazz and ev.method=log.method  where (log.method='logout' or log.method='authenticate') and user_name in(select login_name from im_account where user_id="+userId+" and app_id in(select id from im_app where sn='APP001')) ORDER BY create_time desc", base, 1, 1000);
				if(syslogList!=null && syslogList.getTotalcount()>0){
					JSONArray data=JSONArray.fromObject(syslogList.getDataList());
				    int maxTimes = 1000;
				    int len = data.size();
				    for(int i = 0; len < maxTimes && i < len; i++){
						String status=data.getJSONObject(i).getInt("STATUS")==1?"成功":"失败";
						LogReq log=new LogReq();
						log.setTime(data.getJSONObject(i).getJSONObject("CREATE_TIME").getLong("time"));
						log.setTimeFmtString(sdf.format(log.getTime()));
						log.setRemark(data.getJSONObject(i).getString("NAME"));
						log.setStatus(status);
						logData.add(log);
					}
				}

				//统计单点列表日志
				//查询1000条单点日志日志
				BaseModel base1=new BaseModel() {};
				PageList<?> ssoLogList= jdbcService.findPage("select log.*,app.NAME from sso_log log  LEFT JOIN im_account acct on log.account_id=acct.id left join im_app app on app.id=acct.app_id where log.account_id in(select id from im_account where user_id="+userId+") order by log.create_time desc", base1, 1, 1000);

				if(ssoLogList!=null && ssoLogList.getTotalcount()>0){
					JSONArray data=JSONArray.fromObject(ssoLogList.getDataList());
                    int maxTimes = 1000;
					int len = data.size();
					for(int i = 0; len < maxTimes && i < len; i++){
						String status="成功";
						LogReq log=new LogReq();
						log.setTime(data.getJSONObject(i).getJSONObject("CREATE_TIME").getLong("time"));
						log.setTimeFmtString(sdf.format(log.getTime()));
						log.setRemark("访问"+data.getJSONObject(i).getString("NAME"));
						log.setStatus(status);
						logData.add(log);
					}
				}

				//排序，根据时间大小
				Collections.sort(logData, new Comparator<LogReq>() {
		            @Override
		            public int compare(LogReq o1, LogReq o2) {
		            	//降序
		                int i=o2.getTime().intValue()-o1.getTime().intValue();
		                return i;
		            }
			    });
				identityViewReq.setLogList(logData);
				identityViewReq.setCode(Constants.OPERATION_SUCCESS);
			}else{
				identityViewReq.setCode(Constants.OPERATION_FAIL);
			}
		}catch(Exception e){
			e.printStackTrace();
			identityViewReq.setCode(Constants.OPERATION_UNKNOWN);
		}
		return identityViewReq;

	}


	@ApiOperation(value="指定唯一标识查询")
	@ApiImplicitParams({
		 @ApiImplicitParam(name = "id", value = "唯一标识", required = true, paramType="path", dataType = "Long")
	})
	@RequestMapping(value="findById/{id}", method=RequestMethod.GET)
	@ResponseBody
	public User findById(@PathVariable Long id) {
		User u=super.findById(id);
		//设置兼职组织对象
		List<Org> multiorgs=userMultiOrgService.findOrgByUserId(id);
		u.setMultiOrgs(multiorgs);

		String multiOrgsString="";

		List<Map<String, Object>> polistMap=jdbcService.findList("SELECT a.SN,a.NAME,b.TYPE FROM IM_POSITION a left join IM_USER_POSITION b on b.POSITION_ID=a.ID where b.USER_ID="+u.getId());
		u.setPositions(polistMap);

		int i=0;
		for(Org o:multiorgs){
			if(i==0){
				multiOrgsString=o.getName();
			}else{
				multiOrgsString+=";"+o.getName();
			}
			i++;
		}
		u.setMultiOrgsString(multiOrgsString);
		return u;
	}



	@ApiOperation(value="新增用户")
	@RequestMapping(value="save", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode save(@RequestBody UserReq entity) {
		User u=entity.getUser();
		if(entity.getTempImageId()!=null){
			try {
				//获取图片信息
				String value=redisCache.getCacheObject(entity.getTempImageId());
				if(value!=null){
					ObjectInputStream oos=new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value.getBytes())));
					u.setImage((Image)oos.readObject());
					redisCache.deleteObject(entity.getTempImageId());

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!StringUtils.isEmpty(entity.getOrgSn())){//父级节点编码不为空，则使用父级编码查询机构
			Org org=new Org();
			org.setSn(entity.getOrgSn());
			org.setIsControl(false);
			org=orgService.findByObject(org);
			if(org!=null){
				u.setOrgId(org.getId());
			}
		}
		ResultCode code=super.save(u);
		if(code.getSuccess()){
			if(entity.getTempImageId()!=null) {
				Image image=u.getImage();
				image.setOid(u.getId());
				imageCache.loadImage(image);
			}
		}
		return code;
	}


	@ApiOperation(value="更新用户")
	@RequestMapping(value="edit", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	public ResultCode edit(@RequestBody UserReq entity) {
		User u=entity.getUser();
		if(entity.getTempImageId()!=null){
			try {
				//获取图片信息
				String value=redisCache.getCacheObject(entity.getTempImageId());
				if(value!=null){
					ObjectInputStream oos=new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value.getBytes())));
					u.setImage((Image)oos.readObject());
					redisCache.deleteObject(entity.getTempImageId());
					Image image=u.getImage();
					image.setOid(u.getId());
					imageCache.loadImage(image);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!StringUtils.isEmpty(entity.getOrgSn())){//父级节点编码不为空，则使用父级编码查询机构
			Org org=new Org();
			org.setSn(entity.getOrgSn());
			org.setIsControl(false);
			org=orgService.findByObject(org);
			if(org!=null){
				u.setOrgId(org.getId());
			}
		}
		return super.edit(u);
	}




	/**
	 * 根据SN查询用户对象
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="根据SN查询用户对象")
	@RequestMapping(value="findBySn", method=RequestMethod.POST)
	@ResponseBody
	protected User findBySn(@RequestBody UserReq entity){
		User u=new User();
		u.setSn(entity.getSn());
		try{
			u=getBaseService().findByObject(u);
			if(u!=null){
				u=getBaseService().findById(u.getId());
				//设置兼职组织对象
				List<Org> multiorgs=userMultiOrgService.findOrgByUserId(u.getId());
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

				List<Map<String, Object>> polistMap=jdbcService.findList("SELECT a.SN,a.NAME,b.TYPE FROM IM_POSITION a left join IM_USER_POSITION b on b.POSITION_ID=a.ID where b.USER_ID="+u.getId());
				u.setPositions(polistMap);
			}
			return u;
		}catch(Exception e){
			log.error("findBySn error:",e);
			return u;
		}
	}



	/**
	 * 分页查询用户列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页组织兼职用户列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="multiOrgsfindUserList", method=RequestMethod.POST)
	@ResponseBody
	protected PageList<User> multiOrgsfindUserList(@RequestBody UserReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			User p=entity.getUser();
			//判断组织ID为空
			if(p.getOrgId() != null && p.getOrgId().intValue() != 0){
				String sql="SELECT ID AS id,SN,NAME FROM IM_USER WHERE ID NOT IN(SELECT USER_ID FROM IM_USER_MULTI_ORG WHERE ORG_ID="+p.getOrgId()+") and COMPANY_SN='"+CurrentAccount.getCurrentAccount().getCompanySn()+"'";
				if(!StringUtils.isEmpty(p.getSn())){
					sql+=" and SN like '%"+p.getSn()+"%'";
				}
				User model=new User();
				model.setSort("[{\"property\":\"SN\",\"direction\":\"DESC\"}]");
				PageList pagdse=jdbcService.findPage(sql,model, page, limit);
				return pagdse;
			}
			return new PageList<User>();
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<User>();
		}
	}



	/**
	 * 分页查询用户列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询用户列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findList", method=RequestMethod.POST)
	@ResponseBody
	protected PageList<User> findList(@RequestBody UserReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			User p=entity.getUser();
			//如果组织类型不空分级授权下，默认组织类型ID关联的默认组织节点ID
			if(entity.getOrgTypeId()!=null&&entity.getOrgTypeId().longValue()!=0&&p.getOrgId()==null){
				log.info("not orgId:"+p.getOrgId());
				List<Map<String,Object>> deftOrg =jdbcService.findList("SELECT ID,PARENT_ID FROM IM_ORG WHERE ORG_TYPE_ID="+entity.getOrgTypeId()+" and ID in(select IM_ORG_ID from sys_acct_im_org where SYS_ACCT_ID="+CurrentAccount.getCurrentAccount().getId()+") order by create_time asc");
				if(deftOrg!=null&&deftOrg.size()>0){
					//判断是否为根节点，如果是根节点，则查询全部用户，不是根节点则查询当前节点用户，避免分级授权查询其他组织节点用户
					if(Long.valueOf(deftOrg.get(0).get("PARENT_ID").toString()).longValue()!=-1){
						p.setOrgId(Long.valueOf(deftOrg.get(0).get("ID").toString()));
						log.info("new orgId:"+p.getOrgId());
					}
				}
			}else{
				log.info("orgId:"+p.getOrgId());
			}


			p.setIsLikeQuery(true);
			String companySn=p.getCompanySn();
			if(StringUtils.isEmpty(companySn)) {
				companySn=companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request));
				p.setCompanySn(companySn);
			}
			PageList<User>  us=getBaseService().findPage(p,page,limit);
			if(us.getDataList().size()>0){
				for(int i=0;i<us.getDataList().size();i++){
					//设置兼职组织信息
					String multiOrgsString="";
					List<Org> orgs=	userMultiOrgService.findOrgByUserId(us.getDataList().get(i).getId());
					for(int j=0;j<orgs.size();j++){
						if(j+1==orgs.size()){
							multiOrgsString+=orgs.get(j).getName();
						}else{
							multiOrgsString+=orgs.get(j).getName()+";";
						}
					}

					us.getDataList().get(i).setMultiOrgsString(multiOrgsString);
				}
			}
			return us;
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<User>();
		}
	}


	@ApiOperation(value="移除用户")
    @RequestMapping(value="remove",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 3)
    public ResultCode remove(@RequestBody @ApiParam(name="唯一标识集合",value="多数据采用英文逗号分割",required=true)List<String> ids) {
		Params params=new Params();
		params.setIds(ids);
		log.info("ids:"+ids.toString());
		return super.remove(params);
    }


	/**
	 * 分页查询用户列表-根据岗位ID,可根据工号和姓名模糊查询
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value="分页查询用户列表-根据岗位ID,可根据工号和姓名模糊查询")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findUserByPostion", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 4)
	protected PageList<User> findUserByPostion(@RequestBody UserReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			//注 此处的user中的ID为岗位ID
			User p=entity.getUser();
			p.setIsLikeQuery(true);
			@SuppressWarnings("unchecked")
			PageList<User> list=positionService.findUserByPostion(p,page,limit);
			return list;
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<User>();
		}
	}


	/**
	 * 用户更改组织
	 * @return
	 */
	@ApiOperation(value="用户更改组织")
	@RequestMapping(value="moveOrg", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode moveOrg(@RequestBody UserMoveOrg userMoveOrg){
		try{
			userService.moveOrg(userService.findUserIds(userMoveOrg.getUserIds()),userMoveOrg.getOldOrgId(),userMoveOrg.getNewOrgId());
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}


	/**
	 * 用户离职
	 * @param params
	 * @return
	 */
	@ApiOperation(value="用户离职")
	@RequestMapping(value="suspend", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 6)
	public ResultCode suspend(@RequestBody Params params){
		try{
			userService.suspend(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	/**
	 * 用户激活/复职
	 * @param params
	 * @return
	 */
	@ApiOperation(value="用户激活/复职")
	@RequestMapping(value="active", method=RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 5)
	public ResultCode active(@RequestBody Params params){
		try{
			userService.active(ArrayUtils.stringArrayToLongArray(params.getIds().toArray(new String[params.getIds().size()])));
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}







	/**
	 * 分页查询岗位列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value="获取用户组织岗位分页查询，对象中sn和name,对应组织的SN和名称")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findOrgPositionPage", method=RequestMethod.POST)
	@ResponseBody
	protected PageList<Map<String, Object>> findOrgPositionPage(@RequestBody UserReq entity,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="20") Integer limit){
		try{
			User p=entity.getUser();
			//对象中sn和name,对应组织的SN和名称
			p.setIsLikeQuery(true);
			return userService.findOrgPositionPage(p, page, limit);
		}catch(Exception e){
			log.error("findList error:",e);
			return new PageList<Map<String,Object>>();
		}
	}


	/**
	 * 分页查询用户已授权岗位对象
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="分页查询用户已授权的岗位对象")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "userId", value = "唯一标识", required = true, paramType="path", dataType = "Long"),
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findAuthPositions/{userId}", method=RequestMethod.POST)
	@ResponseBody
	public PageList<?> findAuthPositions(@PathVariable Long userId, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		UserPosition entity = new UserPosition();
		entity.setUserId(userId);
		return userPositionService.findAuthPositions(entity, page, limit);
	}

	/**
	 * 分页查询用户岗位列表（已授权标标记）
	 * @param entity 查询对象
	 * @return
	 */
	@ApiOperation(value="分页查询用户岗位列表（已授权标标记）")
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认20页",example="20")
	})
	@RequestMapping(value="findAuthPositionsList", method=RequestMethod.POST)
	@ResponseBody
	public PageList<?> findAuthPositionsList(@RequestBody keysModel key, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer limit) {
		String sql="SELECT b.ID,b.NAME AS POSITION_NAME,b.SN,a.ORG_ID,c.name as ORGNAME,c.NAME_PATH,c.NAME FROM im_org_position a left join im_position b on a.position_id=b.id left join im_org c on a.org_id=c.ID where b.STATUS=1 and a.ORG_ID IN(SELECT ORG_ID FROM IM_ORG_USER WHERE USER_ID="+key.getUserId()+"  UNION SELECT ORG_ID FROM IM_USER_MULTI_ORG WHERE USER_ID ="+key.getUserId()+")";
		//目前用户授权岗位能授权所有组织下的岗位，，讨论后在觉得使用那种
		//加入模糊查询
		if(key.getKey()!=null&&key.getKey().length()>0){
			if(!"-ALL".equals(key.getKey())){
				sql=sql+" and b.NAME like '%"+key.getKey()+"%' or c.name like '%"+key.getKey()+"%' or b.sn like '%"+key.getKey()+"%' or c.name_path like '%"+key.getKey()+"%' ";
			}
		}
		Position model=new Position();
		model.setSort("[{\"property\":\"SN\",\"direction\":\"DESC\"}]");
		PageList pagdse=jdbcService.findPage(sql,model, page, limit);
		List<Map<String, Object>> map=pagdse.getDataList();
		UserPosition entity = new UserPosition();
		entity.setUserId(key.getUserId());
		PageList list= userPositionService.findAuthPositions(entity, 0, 1000);
		List<Map<String, Object>> list1s=list.getDataList();
		List<String> ara=new ArrayList<String>();
		List<String> ara2=new ArrayList<String>();
		//获取用户已勾选的所有集合
		for(Map<String, Object> ms:list1s){
			int type=Integer.valueOf(ms.get("TYPE").toString());
			if(type==1){
				String dmodel=ms.get("ORG_ID").toString()+"_"+ms.get("ID").toString();
				ara.add(dmodel);
			}else if(type==2){
				String dmode2=ms.get("ORG_ID").toString()+"_"+ms.get("ID").toString();
				ara2.add(dmode2);
			}
		}

		List<Map<String, Object>> newObject=new ArrayList<Map<String,Object>>();


		//构建选择标识
		for(Map<String, Object> ms:map){
			String key2=ms.get("ORG_ID").toString()+"_"+ms.get("ID").toString();

			//是否已勾选主岗
			if(ara.indexOf(key2)!=-1){
				ms.put("CHEACK", true);
			}else{
				ms.put("CHEACK", false);
			}
			//是否存在兼岗
			if(ara2.indexOf(key2)!=-1){
				ms.put("CHEACK2", true);
			}else{
				ms.put("CHEACK2", false);
			}
			String name=ms.remove("NAME").toString();
			String namePath=ms.get("NAME_PATH").toString();
			ms.put("NAME", namePath+""+name);
			newObject.add(ms);
		}
		pagdse.setCurrentPage(page);
		pagdse.setPageSize(limit);
		pagdse.setDataList(newObject);
		return pagdse;
	}

	/**
	 * 授权用户岗位信息
	 * @param userIds  用户标识
	 * @param positionIds   orgid_positionid组合
	 * @return
	 */
	@ApiOperation(value="授权用户岗位信息")
	@RequestMapping(value="authPosition", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode authPosition(@RequestBody EnterAuthModel entity){
		try{
			//获取所有互斥对象
			List<UserPosition> ups=new ArrayList<UserPosition>();//用户新增岗位
			List<UserPosition> cancelups=new ArrayList<UserPosition>();//用户取消岗位
			UserPosition up;
			//循环用户标识
			for (Long userId : entity.getUserIds()) {

				//循环岗位信息
				//Auth_ids[岗位ID_组织ID格式传参]
				for (String position : entity.getAuth_ids()) {
					up=new UserPosition();
					up.setUserId(userId);
					up.setType(entity.getType());
					up.setOrgId(Long.valueOf(position.split("_")[1]));
					up.setPositionId(Long.valueOf(position.split("_")[0]));
					ResultCode code=isMuite(userId, up.getPositionId());
					//判断当前授权岗位存不存在互斥，如果存在则返回授权失败信息
					if(!code.getSuccess()){
						return code;
					}
					ups.add(up);
				}

				//Auth_ids[岗位ID_组织ID格式传参]
				for (String position : entity.getCancel_auth_ids()) {
					up=new UserPosition();
					up.setUserId(userId);
					up.setType(entity.getType());
					up.setOrgId(Long.valueOf(position.split("_")[1]));
					up.setPositionId(Long.valueOf(position.split("_")[0]));
					cancelups.add(up);
				}
			}
			userService.authAndCancelPosition(ups, cancelups);
			//userService.authPosition(ups);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}
	/**
	 * 判断是否存在互斥岗位
	 * @param userId
	 * @param positionId
	 * @return
	 */
	public ResultCode isMuite(Long userId,Long positionId){
		boolean flag=false;
		String sql="SELECT B_ID FROM IM_MUTEX WHERE A_ID="+positionId;
		List<Map<String, Long>> maps=jdbcService.findList(sql);
		if(maps!=null&&maps.size()>0){
			UserPosition entity2 = new UserPosition();
			entity2.setUserId(userId);
			PageList list= userPositionService.findAuthPositions(entity2, 0, 1000);
			List<Map<String, Object>> list1s=list.getDataList();
			List<String> ara=new ArrayList<String>();
			//获取用户已勾选的所有集合
			for(Map<String, Object> ms:list1s){
				String dmodel=ms.get("ID").toString();
				ara.add(dmodel);
			}
			for(int i=0;i<maps.size();i++){
				for(String id:ara){
					if(maps.get(i).get("B_ID").longValue()==Long.valueOf(id).longValue()){
						//存在用户已勾选的互斥岗位，进行后端返回提示消息
						flag=true;
						break;
					}
				}
			}
		}
		if(flag){
			String name="";
			for(int i=0;i<maps.size();i++){
				Position p=positionService.findById(Long.valueOf(maps.get(i).get("B_ID")));
				if(p!=null){
					name+="["+p.getName()+"],";
				}
			}
			if(name==null||name.length()==0){
				return new ResultCode(Constants.OPERATION_FAIL,"授权失败，当前岗位与其他岗位存在互斥。");
			}else{
				name=name.substring(0, name.length()-1);
				return new ResultCode(Constants.OPERATION_FAIL,"授权失败，当前岗位与"+name+"存在互斥。");
			}
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	/**
	 * 取消用户授权岗位信息
	 * @param userIds  用户标识
	 * @param positionIds   orgid_positionid组合
	 * @return
	 */
	@ApiOperation(value="取消用户授权岗位信息")
	@RequestMapping(value="userCancelPosition", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode userCancelPosition(@RequestBody EnterAuthModel entity){
		try{
			List<UserPosition> ups=new ArrayList<UserPosition>();
			UserPosition up;
			//循环用户标识
			for (Long userId : entity.getUserIds()) {
				//循环岗位信息
				//Auth_ids[岗位ID_组织ID格式传参]
				for (String position : entity.getAuth_ids()) {
					up=new UserPosition();
					up.setUserId(userId);
					up.setType(entity.getType());
					up.setOrgId(Long.valueOf(position.split("_")[1]));
					up.setPositionId(Long.valueOf(position.split("_")[0]));
					ups.add(up);
				}
			}
			userService.userCancelPosition(ups);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}




	/**
	 * 授权用户应用信息
	 * @param userIds  用户标识
	 * @param auth_ids   orgid_positionid组合
	 * @return
	 */
	@ApiOperation(value="授权用户应用信息")
	@RequestMapping(value="authApp", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode authApp(@RequestBody EnterAuthModel entity){
		try{
			//循环用户标识
			Long[] userIds=new Long[entity.getUserIds().size()];
			int i=0;
			for (Long id:entity.getUserIds()) {
				userIds[i]=id;
				i++;
			}
			i=0;
			Long[] appIds=new Long[entity.getAuth_ids().size()];
			for (String id:entity.getAuth_ids()) {
				appIds[i]=Long.valueOf(id);
				i++;
			}
			i=0;
			userService.authApp(userIds, appIds, Constants.ACCOUNT_OPEN_TYPE_BASIC);
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}

	/**
	 * 用户导入
	 */

 	@ApiOperation(value="用户导入")
	@RequestMapping(value="importUser/{orgId}",method = RequestMethod.POST)
	@ResponseBody
	public ResultCode importUser(@PathVariable Long orgId, @ApiParam(name="file",value="导入内容",required=false) @RequestParam(required=false,name="file",value="file") MultipartFile file){
 		List<UserType> userTypes= userTypeService.findAll();
		Map<String,String> colM=new HashMap<String,String>();
		for (UserType userType : userTypes) {
			Field userField=new Field();
			userField.setObjId(userType.getId());
			List<Field> fields=sysFieldService.findList(userField);
			colM.clear();
			for (Field field : fields) {
				colM.put(field.getRemark(),field.getName());
			}
			try {
				List list=ExcelUtils.parseToMap(file.getInputStream(), userType.getRemark(), colM);
				if(list != null) {
					//获取组织机构类型ID,主要区分不同组织机构分区下编码相同问题
					return userService.importUser(list,orgService.findById(orgId).getOrgTypeId(),userType.getId());
				}
				else{
					return new ResultCode(Constants.OPERATION_UNKNOWN,"导入用户类型不匹配");
				}
			} catch (IOException e) {
				log.error("parse excel exception",e);
				return new ResultCode(Constants.OPERATION_FAIL);
			}
		}
		return new ResultCode(Constants.OPERATION_FAIL);
	}

 	@ApiOperation(value = "用户导出")
 	@RequestMapping(value = "export", method = RequestMethod.POST)
 	@ResponseBody
 	public void export(@RequestBody UserReq entity) {
 		try {
	 		User user = entity.getUser();
	 		user.setIsLikeQuery(true);
	 		PageList pageList = userService.findByOrgPage(user, 0, 50000);
	 		//循环生成打印模型
	 		List<ExcelUtils.ExcelModel> models = new ArrayList<ExcelUtils.ExcelModel>();
	 		models.add(new ExcelUtils.ExcelModel("工号", "SN",5000));
	 		models.add(new ExcelUtils.ExcelModel("姓名","NAME",5000));
			models.add(new ExcelUtils.ExcelModel("用户类型","USER_TYPE_ID",5000));
			models.add(new ExcelUtils.ExcelModel("性别","SEX",5000));
			models.add(new ExcelUtils.ExcelModel("联系电话","TELEPHONE",5000));
			models.add(new ExcelUtils.ExcelModel("电子邮件","EMAIL",5000));
			models.add(new ExcelUtils.ExcelModel("状态","STATUS",5000));
			models.add(new ExcelUtils.ExcelModel("组织编码","ORG_SN",5000));

			super.exportXlsx("user", pageList.getDataList(), models, "userlist");
	 	} catch (Exception e) {
	 		log.error("export user error", e);
	 	}
	}


 	@ApiOperation(value="同步用户更新事件")
	@RequestMapping(value="userUpdate", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode syncAll(@RequestBody Long id){
		CurrentAccount account=SessionManager.getSession(GatewayHttpUtil.getKey(Constants.CURRENT_SESSION_ID, request),request);
		List<Map<String, Object>> list=null;
		if(id==null || id.longValue()==0){
			 log.info("sync all user");
			 list = jdbcService.findList("select id from im_user where COMPANY_SN='"+account.getCompanySn()+"' ORDER BY CREATE_TIME asc");
		}else{
			log.info("sync user id :"+id.longValue());
			 list = jdbcService.findList("select id from im_user where id="+id.longValue()+" and COMPANY_SN='"+account.getCompanySn()+"'");
		}
		if(list!=null&&list.size()>0){
			Long[] ids=new Long[list.size()];
			for(int i=0;i<list.size();i++){
				ids[i]=Long.valueOf(list.get(i).get("id").toString());
				log.info(ids[i]);
			}
			//执行批量修改用户功能
			userService.updateSync(ids);
		}
		return new ResultCode(Constants.OPERATION_SUCCESS);
	}

 	/**
	 * 授权用户兼职组织
	 * @param EnterAuthOrgModel 授权对象
	 * @return
	 */
	@ApiOperation(value="授权用户兼职组织")
	@RequestMapping(value="authUserMultiOrg", method=RequestMethod.POST)
	@ResponseBody
	public ResultCode authUserMultiOrg(@RequestBody EnterAuthOrgModel entity){
		try{
			long userId=Long.valueOf(entity.getUserId());
			if(userId!=0){
				return userMultiOrgService.auth(userId, entity.getIds());
			}else{
				return new ResultCode(Constants.OPERATION_FAIL);
			}
		}catch(Exception e){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
	}





	@ApiOperation(value="获取用户被授权的帐号集合")
	@RequestMapping(value="findUserAuthAccount", method=RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name="page",required=true,dataType="Integer",value="页码,默认0",example="0"),
		@ApiImplicitParam(name="limit",required=true,dataType="Integer",value="页大小，默认10页",example="10")
	})
	@ResponseBody
	public PageList<UserAccountReq> findUserAuthAccount(@RequestBody UserAuthAccountReq entity,@RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="10") Integer limit) {
		PageList list=userService.findUserAuthAccount(entity.getUserId(), entity.getAppName(),page,limit);
		if(list!=null&&list.getDataList()!=null&&list.getDataList().size()>0){
			List<UserAccountReq> newList=new ArrayList<UserAccountReq>();
			List<Map<String, Object>> pagellist=list.getDataList();
			for(Map<String, Object> map:pagellist){
				try{
					UserAccountReq model=new UserAccountReq();
					model.setAccstatus(Integer.valueOf(map.get("accstatus").toString()));
					model.setAppname(map.get("appname").toString());
					model.setAppsn(map.get("appsn").toString());
					model.setLoginname(map.get("loginname").toString());
					model.setId(Long.valueOf(map.get("id").toString()));
					model.setUsersn(map.get("usersn").toString());
					model.setUsername(map.get("username").toString());
					newList.add(model);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			list.setDataList(newList);
		}
		return list;
	}
	/**
	 * 根据组织ID和应用ID批量开通账号
	 *
	 * @return
	 */
	@RequestMapping(value = "openAccts")
	@ResponseBody
	public synchronized Object openAccts(Long orgId, Long appId) {
		// 获取session中保存的时间，为空就设置当前时间，不为空就对比时间小于一分钟就返回
		Object openAcctsTime = request.getSession().getAttribute("openAcctsTime");
		if (openAcctsTime != null) {
			long openTime = Long.valueOf(openAcctsTime.toString());
			long currTime = DateUtil.offsetMinute(new Date(), -1).getTime();// 向前偏移一分钟
			if (currTime < openTime) {
				return new ResultCode(Constants.OPERATION_FAIL, "1分钟内不允许重复点击");
			}
		}
		long currTime = new Date().getTime();
		request.getSession().setAttribute("openAcctsTime", currTime);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orgId", orgId);
		params.put("appId", appId);
		List<Long> userIds = userService.findUserByOrgIdRecursion(params);
		for (Long userId : userIds) {
			Account account = new Account();
			account.setAppId(appId);
			account.setUserId(userId);
			// 判断用户是否已经授权过应用
			List<Account> accts = accountService.findList(account);
			if (accts == null || accts.size() == 0) {
				// 根据账号策略开通账号,如果没有策略则使用默认用户名和密码
				accountService.autoOpenAccount(userId, appId, 1);
			}
		}

		return new ResultCode(Constants.OPERATION_SUCCESS);
	}
	@ApiOperation(value = "用户权限导出")
 	@RequestMapping(value = "exportUserPower", method = RequestMethod.POST)
 	@ResponseBody
 	public void exportUserPower(@RequestBody UserReq entity) {
 		try {
 			User user=entity.getUser();
			//如果组织类型不空分级授权下，默认组织类型ID关联的默认组织节点ID
			if(entity.getOrgTypeId()!=null&&entity.getOrgTypeId().longValue()!=0&&user.getOrgId()==null){
				log.info("not orgId:"+user.getOrgId());
				List<Map<String,Object>> deftOrg =jdbcService.findList("SELECT ID,PARENT_ID FROM IM_ORG WHERE ORG_TYPE_ID="+entity.getOrgTypeId()+" and ID in(select IM_ORG_ID from sys_acct_im_org where SYS_ACCT_ID="+CurrentAccount.getCurrentAccount().getId()+") order by create_time asc");
				if(deftOrg!=null&&deftOrg.size()>0){
					//判断是否为根节点，如果是根节点，则查询全部用户，不是根节点则查询当前节点用户，避免分级授权查询其他组织节点用户
					if(Long.valueOf(deftOrg.get(0).get("PARENT_ID").toString()).longValue()!=-1){
						user.setOrgId(Long.valueOf(deftOrg.get(0).get("ID").toString()));
						log.info("new orgId:"+user.getOrgId());
					}
				}
			}
			user.setIsLikeQuery(true);
	 		List<User> findUserListPower = userService.findUserListPower(user);
	 		//循环生成打印模型
	 		List<ExcelUtils.ExcelModel> models = new ArrayList<ExcelUtils.ExcelModel>();
	 		models.add(new ExcelUtils.ExcelModel("工号", "sn",5000));
	 		models.add(new ExcelUtils.ExcelModel("姓名","name",5000));
			models.add(new ExcelUtils.ExcelModel("用户类型","userTypeName",5000));
			models.add(new ExcelUtils.ExcelModel("应用名称","appName",5000));
			models.add(new ExcelUtils.ExcelModel("所属组织","orgName",5000));
			models.add(new ExcelUtils.ExcelModel("账号状态","status",5000));
			super.exportXlsx("user", findUserListPower, models, "userPower");
	 	} catch (Exception e) {
	 		log.error("export user error", e);
	 	}
	}
}
