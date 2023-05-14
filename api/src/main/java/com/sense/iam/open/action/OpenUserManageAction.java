package com.sense.iam.open.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import net.sf.json.JSONArray;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sense.core.model.BaseModel;
import com.sense.core.util.PageList;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.api.model.IdentityViewReq;
import com.sense.iam.api.model.LogReq;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.model.im.App;
import com.sense.iam.model.im.Org;
import com.sense.iam.model.im.Position;
import com.sense.iam.model.im.User;
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

@Controller
@RequestMapping("open/userManage")
public class OpenUserManageAction extends BaseAction{
	
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
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	/**
     * 用户身份视图页面
     * @return
     */
	@RequestMapping("/userInfo.html")
    public String userInfo(String sn){
		if(sn!=null&&sn.length()>0){
			request.setAttribute("sn", sn);
		}
		return "/userManage";
	}
		
	/**
	 * 用户信息查询
	 * @param sn
	 * @return
	 */
	@RequestMapping(value="identityView", method=RequestMethod.POST)
	@ResponseBody
	public IdentityViewReq identityView(String sn) {
		IdentityViewReq identityViewReq=new IdentityViewReq();
		try{
			User u = new User();
			u.setSn(sn);
			List<User> list=userService.findList(u);
			if(list.size()==0){
				 identityViewReq.setCode(Constants.OPERATION_FAIL);
				 return identityViewReq;
			}
			Long userId = list.get(0).getId();
			User user = userService.findById(userId);
			if(user.getOrgId()!=null && user.getOrgId() != 0){
				//设置兼职组织对象
				identityViewReq.setMultiorgs(userMultiOrgService.findOrgByUserId(userId));
				
				identityViewReq.setUser(user);
			
				/**====================获取用户的组织===============*/
				Org org=orgService.findById(user.getOrgId());
				org.setNamePath(org.getNamePath()+org.getName());
				identityViewReq.setOrg(org);
				/**====================获取用户的组织===============*/
				//查询用户岗位信息
				List<?> posData= jdbcService.findList("select name from im_position where id in(select position_id from im_user_position where user_id="+userId+")");
				if(posData!=null&&posData.size()>0){
					JSONArray data=JSONArray.fromObject(posData);
					List<Position> posList=new ArrayList<Position>();
					for(int i=0;i<data.size();i++){
						Position model=new Position();
						model.setName(data.getJSONObject(i).getString("name"));
						posList.add(model);
					}
					identityViewReq.setPositionList(posList);
				}
				//查询用户应用信息
				List<?> appData= jdbcService.findList("select name from im_app where id in(select app_id from im_account where user_id="+userId+" and status=1)");
				if(appData!=null&&appData.size()>0){
					JSONArray data=JSONArray.fromObject(appData);
					List<App> appList=new ArrayList<App>();
					for(int i=0;i<data.size();i++){
						App model=new App();
						model.setName(data.getJSONObject(i).getString("name"));
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
					for(int i=0;i<data.size();i++){
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
					for(int i=0;i<data.size();i++){
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
}
