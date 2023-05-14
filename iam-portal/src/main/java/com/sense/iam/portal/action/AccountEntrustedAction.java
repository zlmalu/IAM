package com.sense.iam.portal.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sense.core.util.CurrentAccount;
import com.sense.core.util.PageList;
import com.sense.iam.cache.ImageCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.im.AccountUser;
import com.sense.iam.model.im.User;
import com.sense.iam.portal.util.OrgTreeModel;
import com.sense.iam.portal.util.OrgUtil;
import com.sense.iam.service.AccountService;
import com.sense.iam.service.AppService;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.UserService;

/**
 * 账号委托
 * @author ygd
 *
 */
@Controller
public class AccountEntrustedAction extends BaseAction {
	protected Log log=LogFactory.getLog(getClass());
	
	@Resource
	AppService appService;
	
	@Resource
	JdbcService jdbcService;
	
	@Resource
	UserService userService;
	
	@Resource
	AccountService accountService;
	
	@Resource
	ImageCache imageCache;

	/**
	 * 账号委托查询
	 * @param loginName
	 * @param appName
	 * @param appSn
	 * @param userSn
	 * @param userName
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping("/accountUserPage.action")
	@ResponseBody
    public Object accountUserPage(String loginName,String appName,String appSn,String userSn,String userName,Integer page,Integer limit){
		log.info("appName:"+appName);
		log.info("appSn:"+appSn);
		log.info("userName:"+userName);
		log.info("userSn:"+userSn);
		log.info("loginName:"+loginName);
		log.info("page:"+page);
		log.info("limit"+limit);
		appName=StringEscapeUtils.escapeHtml4(appName);
		appSn=StringEscapeUtils.escapeHtml4(appSn);
		userName=StringEscapeUtils.escapeHtml4(userName);
		userSn=StringEscapeUtils.escapeHtml4(userSn);
		loginName=StringEscapeUtils.escapeHtml4(loginName);

		CurrentAccount account=CurrentAccount.getCurrentAccount();
		AccountUser au=new AccountUser();
		au.setAppName(appName);
		au.setAppSn(appSn);
		au.setAcctId(account.getId());
		au.setUserSn(userSn);
		au.setUserName(userName);
		au.setLoginName(loginName);
		au.setIsLikeQuery(true);
		au.setSort("[{\"property\":\"LOGIN_NAME\",\"direction\":\"DESC\"}]");
		PageList<AccountUser> pageList=accountService.findMyAccountUserLists(au, page, limit);
		return pageList;
	}
	
	/**
	 * 移除委托账号
	 * @param acctId
	 * @param userId
	 * @return
	 */
	@RequestMapping("/removeAccountUser.action")
	@ResponseBody
    public Object removeAccountUser(Long acctId,Long userId){
		if(acctId==null||acctId.longValue()==0){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
		if(userId==null||userId.longValue()==0){
			return new ResultCode(Constants.OPERATION_FAIL);
		}
		AccountUser au=new AccountUser();
		au.setAcctId(acctId);
		au.setUserId(userId);
		accountService.removeAccountUser(au);
		return new ResultCode(Constants.OPERATION_SUCCESS);

	}
	
	/**
	 * 加载代理人中--组织树
	 * @param name
	 * @return
	 */
	@RequestMapping("/orgTreeload.action")
	@ResponseBody
    public Object orgTreeload(String name){
		name=StringEscapeUtils.escapeHtml4(name);
		
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		String sql="SELECT o.ID,o.NAME,o.PARENT_ID,o.ORG_TYPE_ID,p.NAME AS PARENTNAME FROM IM_ORG o left join IM_ORG p on p.ID=o.PARENT_ID  WHERE o.STATUS=1";
		List<Map<String, Object>> maps;
		if(StringUtils.isNotEmpty(name)){
			sql+=" and o.NAME like concat('%',?,'%')";
			maps = jdbcService.findList(sql,name);
		}else {
			maps = jdbcService.findList(sql);
		}
		if(StringUtils.isNotEmpty(name)){
			for(Map<String, Object> map:maps){
				Long parentId2=Long.valueOf(map.get("PARENT_ID").toString());
				if(parentId2.longValue()!=-1){
					map.put("title", map.get("PARENTNAME")+">"+map.get("NAME"));
					map.put("id", map.get("ID"));
				}else{
					map.put("title", map.get("NAME"));
					map.put("id", map.get("ID"));
				}
			}
			return maps;
		}else{
			List<OrgTreeModel> menu=new ArrayList<OrgTreeModel>();;
			for(Map<String, Object> map:maps){
				OrgTreeModel model=new OrgTreeModel();
				model.setChildren(new ArrayList<OrgTreeModel>());
				model.setId(Long.valueOf(map.get("ID").toString()));
				model.setParentId(Long.valueOf(map.get("PARENT_ID").toString()));
				model.setTitle(map.get("NAME").toString());
				menu.add(model);
			}
			menu=OrgUtil.menuList(menu);
			return menu;
		}
		
	}
	
	/**
	 * 加载代理人中--用户账号信息
	 * @param sn
	 * @param mobile
	 * @param name
	 * @param status
	 * @param orgId
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping("/userAccountInfo.action")
	@ResponseBody
    public Object userAccountInfo(String sn,String mobile,String name,Integer status,Long orgId,Integer page,Integer limit){
		log.info("sn:"+sn);
		log.info("mobile:"+mobile);
		log.info("name:"+name);
		log.info("status:"+status);
		log.info("orgId"+orgId);
		log.info("page:"+page);
		log.info("limit"+limit);
		CurrentAccount account=CurrentAccount.getCurrentAccount();
		String sql="SELECT u.ID,u.SN,u.NAME,u.SEX,u.EMAIL,u.TELEPHONE,u.STATUS,o.NAME as ORG_NAME FROM IM_USER u left join IM_ORG_USER ou on ou.USER_ID=u.ID LEFT JOIN IM_ORG o on o.ID=ou.ORG_ID WHERE u.COMPANY_SN='"+account.getCompanySn()+"'";
		if(sn!=null&&sn.trim().length()>0){
			sql+=" and u.SN like '%"+sn+"%'";
		}
		if(name!=null&&name.trim().length()>0){
			sql+=" and u.NAME like '%"+name+"%'";
		}
		if(mobile!=null&&mobile.trim().length()>0){
			sql+=" and u.TELEPHONE like '%"+mobile+"%'";
		}
		if(orgId!=null&&orgId.longValue()!=0){
			sql+=" and o.ID="+orgId;
		}
		if(status!=null&&status.longValue()!=0){
			sql+=" and u.STATUS="+status;
		}else{
			sql+=" and u.STATUS=1";
		}
		User model=new User();
		log.info("sql:"+sql);
		model.setSort("[{\"property\":\"SN\",\"direction\":\"DESC\"}]");
		PageList pageList=jdbcService.findPage(sql,model, page, limit);
		return pageList;
		
	}
}
