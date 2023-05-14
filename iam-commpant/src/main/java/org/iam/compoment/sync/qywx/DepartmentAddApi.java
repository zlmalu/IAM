package org.iam.compoment.sync.qywx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sense.core.util.ContextUtil;
import com.sense.core.util.HttpUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.DefaultContent;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;
import com.sense.iam.service.JdbcService;

@Name("部门新增")
public class DepartmentAddApi extends SyncApi implements SyncInteface {

	private Log log = LogFactory.getLog(getClass());
	@Param("企业ID")
	private String corpid = "ww00a52dcb1e35a5c3";
	@Param("通讯录密钥")
	private String corpsecret = "n8XObnc-HpbY6M0WWDRuIKB6jticSWSHXPY44X4mhbs";
	@DefaultContent
	private String defaultContent="<@JDBC id=\"org\" sql=\"select * from (select a.ID,a.SN,a.NAME,a.PARENT_ID,a.SORT_NUM,b.SN as PSN from im_org a left join im_org b on a.parent_id=b.ID where a.id=${oid}) a\"/><#if org[0].PARENT_ID!=-1>{\"id\": \"${org[0].SN}\",\"name\": \"${org[0].NAME}\",\"parentid\": \"${org[0].PSN}\",\"order\": \"${org[0].SORT_NUM}\"}</#if>";
	
	
	@Override
	public ResultCode execute(String content) {
		JdbcService jdbcService=(JdbcService) ContextUtil.getBean("jdbcService");
		try{
			JSONObject contObj=JSONObject.fromObject(content);
			Object parentId=contObj.get("parentid");
			Long orgId = contObj.getLong("id");
			//判断是否重新设置顶级节点
			if(StringUtils.getString(parentId).trim().length()==0 || StringUtils.getString(parentId).trim().equals("-1")|| StringUtils.getString(parentId).trim().equals("1")){
				//设置
				List list=jdbcService.findList("select attr.value as WX_PARENT_ID from im_org o LEFT JOIN im_org_attr attr on o.parent_id=attr.org_id where attr.NAME='WX_ORG_ID' and id="+orgId);
				if(list!=null && list.size()>0){
					contObj.put("parentid", ((Map)list.get(0)).get("WX_PARENT_ID"));
					content=contObj.toString();
				}
			}
			System.out.println("sync org add content===="+content);
			String result=HttpUtil.POST_API("https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token="+getAccessToken(corpid,corpsecret), content, new HashMap());
			JSONObject json=JSONObject.fromObject(result);
			//反写本地组织机构对应的企业微信组织ID
			jdbcService.executeSql("insert into im_org_ATTR(ORG_ID,NAME,VALUE) values("+orgId+",'WX_ORG_ID','"+json.get("id")+"')");
			
			if(json.getInt("errcode")==0){
				return new ResultCode(SUCCESS,json.getString("errmsg"));
			}else{
				return new ResultCode(FAIL,json.getString("errmsg"));
			}
		}catch(Exception e){
			log.error("sync qywx add exception",e);
			return new ResultCode(FAIL,"sync error "+e.getMessage());
		}
	}
	
}
