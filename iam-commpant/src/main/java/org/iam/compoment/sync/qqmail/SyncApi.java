package org.iam.compoment.sync.qqmail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.security.UIM;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.HttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.JdbcService;

import net.sf.json.JSONObject;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SyncApi implements SyncInteface{

	private Log log=LogFactory.getLog(getClass());
	
	@Param("企业ID")
	private String corpid;
	@Param("管理密钥")
	private String corpsecret;
	@Param("操作类型(1=新增,2=修改,3=删除)")
	private String optType="2";//1 add  2 edit 3delete
	@Param("操作目标(1=组织,2=用户)")
	private String optDest="2";//1 组织  2 用户
	
	
	/**
	 * 获取AccessToken
	 * @return
	 * description :  
	 * wenjianfeng 2019年12月24日
	 */
	private String getAccessToken() {
		
		try {
			String result = HttpUtil.GET_API("https://api.exmail.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret,new HashMap());
			log.debug(result);
			if(!result.contains("ok")){
				log.error("获取Token异常");
			}
			return JSONObject.fromObject(result).get("access_token").toString();
		} catch (Exception e) {
			log.error("获取访问token失败",e);
		}
		return "";
	}

	@Override
	public ResultCode execute(String content) {
		content=content.trim();
		System.out.println("corpid="+corpid+",corpsecret="+corpsecret);
		System.out.println("============SIM下推数据："+content);
		if(optDest.equals("1")){
			if(optType.equals("1")){//组织新增
				return orgAdd(content);
			}else if(optType.equals("2")){//组织修改
				return orgEdit(content);
			}else if(optType.equals("3")){//组织删除
				return orgDel(content);
			}
		}else if(optDest.equals("2")){
			if(optType.equals("1")){//用户新增
				return userAdd(content);
			}else if(optType.equals("2")){//用户修改
				return userEdit(content);
			}else if(optType.equals("3")){//用户删除
				return userDel(content);
			}
		}
		return new ResultCode(FAIL,"同步目标为空");
	}
	
	
	public ResultCode orgAdd(String content){
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		try{
			
			JSONObject contObj=JSONObject.fromObject(content);
			Object parentId=contObj.get("parentid");
			Long orgId = contObj.getLong("orgId");
			//判断是否重新设置顶级节点
			if(StringUtils.getString(parentId).trim().length()==0 || StringUtils.getString(parentId).trim().equals("null")|| StringUtils.getString(parentId).trim().equals("1")){
				//设置
				List list=jdbcService.findList("select attr.value as QQ_MAIL_ORG_ID from im_org o LEFT JOIN im_org_attr attr on o.parent_id=attr.org_id where attr.NAME='QQ_MAIL_ORG_ID' and id="+orgId);
				if(list!=null && list.size()>0){
					contObj.put("parentid", ((Map)list.get(0)).get("QQ_MAIL_ORG_ID"));
					content=contObj.toString();
				}
			}
			System.out.println("sync org add content===="+content);
			
			String result=HttpUtil.POST_API("https://api.exmail.qq.com/cgi-bin/department/create?access_token="+getAccessToken(), content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				//反写本地组织机构对应的qq邮箱组织ID
				
				jdbcService.executeSql("insert into im_org_ATTR(ORG_ID,NAME,VALUE) values("+JSONObject.fromObject(content).getLong("id")+",'QQ_MAIL_ORG_ID','"+jo.get("id")+"')");
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail add exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode orgEdit(String content){
		try{
			String result=HttpUtil.POST_API("https://api.exmail.qq.com/cgi-bin/department/update?access_token="+getAccessToken(), content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				//反写本地组织机构对应的qq邮箱组织ID
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail edit exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode orgDel(String content){
		try{
			String result=HttpUtil.GET_API("https://api.exmail.qq.com/cgi-bin/department/delete?access_token="+getAccessToken()+"&id="+content.trim(), new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				//反写本地组织机构对应的qq邮箱组织ID
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail del exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode userAdd(String content){
		try{
			String result=HttpUtil.POST_API("https://api.exmail.qq.com/cgi-bin/user/create?access_token="+getAccessToken(), content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail add exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	
	public ResultCode userEdit(String content){
		try{
			String result=HttpUtil.POST_API("https://api.exmail.qq.com/cgi-bin/user/update?access_token="+getAccessToken(), content, new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			System.out.println("result="+result);
			if(jo.getInt("errcode")==0){
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail edit exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	
	public ResultCode userDel(String content){
		try{
			String result=HttpUtil.GET_API("https://api.exmail.qq.com/cgi-bin/user/delete?access_token="+getAccessToken()+"&userid="+content.trim(), new HashMap());
			JSONObject jo=JSONObject.fromObject(result);
			if(jo.getInt("errcode")==0){
				return new ResultCode(SUCCESS,jo.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,jo.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qq mail del exception",e);
			return new ResultCode(FAIL,"sysn error "+e.getMessage());
		}
	}
	

	
}
