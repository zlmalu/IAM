package org.iam.compoment.sync.zhangin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sense.core.util.ContextUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.model.im.Org;
import com.sense.iam.service.JdbcService;

/**
 * zhangin 章管家数据同步
 * 
 * Description:  
 * 
 * @author hyj
 * 
 * Copyright 2005, 2020 Sense Software, Inc. All rights reserved.
 *
 */
@Name("章管家数据同步")
public class SyncApi implements SyncInteface{
	private Log log = LogFactory.getLog(getClass());

	@Param("接口地址")
	private String interface_address="http://test.zhangin.com:8896/api";
	@Param("企业ID")
	private String client_id="4L2rmqXEv0";
	@Param("安全密钥")
	private String client_secret="8gCDtG5YRyrIypLX143Vm7hbEYuuAasA";
	@Param("操作对象(1=组织,2=用户)")
	private String optObj="1";
	@Param("操作类型(1=新增,2=修改,3=删除)")
	private String optType="1";
	
	private String getAccessToken(){
		String access_token=HttpUtil.GET_API(interface_address+"/getToken.htm?client_id="+client_id+"&client_secret="+client_secret+"&grant_type=code&response_type=json", new HashMap());
		return JSONObject.fromObject(access_token).getString("accessToken");
	}
	
	private String getParentSn(Long parentId){
		String sn="";
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		List list =jdbcService.findList("select SN from im_org where id="+parentId);
		if(list!=null && list.size()>0){
			sn=((Map)list.get(0)).get("SN").toString();
		}
		return sn;
	}
	
	/**
	 * 添加/更新部门
	 * @param content
	 * @return
	 */
	public ResultCode addUpdateDepartment(String content){
		Map<String, String> headers=new HashMap<String, String>();
		headers.put("Authorization", "Bearer "+getAccessToken());
		log.info("add department："+content);
		try {
			JSONObject jsonObj=JSONObject.fromObject(content);
			JSONArray jsons = new JSONArray();
			JSONObject json = new JSONObject();
			json.put("name", jsonObj.getString("name"));
			json.put("code", jsonObj.getString("sn"));
			json.put("parent_id", getParentSn(jsonObj.getLong("parentId")));
			json.put("department_id", jsonObj.getString("sn"));
			json.put("is_enable", 1);
			jsons.add(json);
			String result =HttpUtil.POSTS(interface_address+"/department/batchCreate.htm", jsons, headers);
			if(JSONObject.fromObject(result).getInt("errcode")==0){
				return new ResultCode(SUCCESS, JSONObject.fromObject(result).getString("errmsg"));
			}else{
				return new ResultCode(FAIL, JSONObject.fromObject(result).getString("errmsg"));
			}
		} catch (Exception e) {
			log.error("add department faild exception",e);
			return new ResultCode(FAIL, e.getMessage());
		}
	}
	
	/**
	 * 删除部门
	 * @param content
	 * @return
	 */
	public ResultCode removeDepartment(String content){
		Map<String, String> headers=new HashMap<String, String>();
		headers.put("Authorization", "Bearer "+getAccessToken());
		log.info("delete department："+content);
		try {
			JSONObject jsonObj=JSONObject.fromObject(content);
			String department_ids=jsonObj.getString("sn");
			String result=HttpUtil.DELETES(interface_address+"/department/batchDelete.htm?department_ids="+department_ids, headers);
			if(JSONObject.fromObject(result).getInt("errcode")==0){
				return new ResultCode(SUCCESS, JSONObject.fromObject(result).getString("errmsg"));
			}else{
				return new ResultCode(FAIL, JSONObject.fromObject(result).getString("errmsg"));
			}
		} catch (Exception e) {
			log.error("delete department faild exception",e);
			return new ResultCode(FAIL, e.getMessage());
		}
		
	}
	
	/**
	 * 添加/更新用户
	 * @param content
	 * @return
	 */
	public ResultCode addUpdatePerson(String content){
		Map<String, String> headers=new HashMap<String, String>();
		headers.put("Authorization", "Bearer "+getAccessToken());
		log.info("add person："+content);
		try {
			JSONObject jsonObj=JSONObject.fromObject(content);
			JSONArray jsons = new JSONArray();
			JSONObject json = new JSONObject();
			json.put("mobile", jsonObj.getString("telephone"));
			json.put("name", jsonObj.getString("name"));
			json.put("code", jsonObj.getString("loginName"));
			json.put("password", jsonObj.getString("loginPwd"));
			json.put("uid", jsonObj.getString("loginName"));
			json.put("department_id", jsonObj.getString("orgSn"));
			json.put("is_enable", 1);
			jsons.add(json);
			String result=HttpUtil.POSTS(interface_address+"/personSeal/batchCreate.htm", jsons, headers);
			if(JSONObject.fromObject(result).getInt("errcode")==0){
				return new ResultCode(SUCCESS, JSONObject.fromObject(result).getString("errmsg"));
			}else{
				return new ResultCode(FAIL, JSONObject.fromObject(result).getString("errmsg"));
			}
		} catch (Exception e) {
			log.error("add person faild exception",e);
			return new ResultCode(FAIL, e.getMessage());
		}
	}
	
	/**
	 * 删除用户
	 * @param content
	 * @return
	 */
	public ResultCode removePerson(String content){
		Map<String, String> headers=new HashMap<String, String>();
		headers.put("Authorization", "Bearer "+getAccessToken());
		log.info("delete person："+content);
		try {
			JSONObject jsonObj=JSONObject.fromObject(content);
			String uids=jsonObj.getString("loginName");
			String result=HttpUtil.DELETES(interface_address+"/personSeal/batchDelete.htm?uids="+uids, headers);
			if(JSONObject.fromObject(result).getInt("errcode")==0){
				return new ResultCode(SUCCESS, JSONObject.fromObject(result).getString("errmsg"));
			}else{
				return new ResultCode(FAIL, JSONObject.fromObject(result).getString("errmsg"));
			}
		} catch (Exception e) {
			log.error("delete person faild exception",e);
			return new ResultCode(FAIL, e.getMessage());
		}
		
	}
	
	@Override
	public ResultCode execute(String content) {
		if("1".equals(optObj)){
			if("1".equals(optType)){
				log.info("====新增组织====");
				return addUpdateDepartment(content);
			}else if("2".equals(optType)){
				log.info("====更新组织====");
				return addUpdateDepartment(content);
			}else if("3".equals(optType)){
				log.info("====删除组织====");
				return removeDepartment(content);
			}
		}else if("2".equals(optObj)){
			if("1".equals(optType)){
				log.info("====新增人员====");
				return addUpdatePerson(content);
			}else if("2".equals(optType)){
				log.info("====更新人员====");
				return addUpdatePerson(content);
			}else if("3".equals(optType)){
				log.info("====更新人员====");
				return removePerson(content);
			}
		}
		return new ResultCode(FAIL, "同步参数错误");
	}
	
}
