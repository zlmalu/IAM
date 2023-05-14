package org.iam.compoment.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sense.core.security.UIM;
import com.sense.core.util.ContextUtil;
import com.sense.core.util.StringUtils;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;
import com.sense.iam.model.im.Org;
import com.sense.iam.service.JdbcService;
import com.sense.iam.service.OrgService;

import cn.hutool.http.HttpUtil;

@Name("全量用户/组织定时任务")
public class InitDataTask implements TaskInterface {

	@Param("机构地址")
	private String orgUrl = "http://localhost:8082/workflow/api/v1/org/saveOrUpdate";
	@Param("机构地址")
	private String userUrl = "http://localhost:8082/workflow/api/v1/user/saveOrUpdate";
	@Param("密钥")
	private String secret = "ee58f77294d84fd196f98a505e9780ca";
	@Param("流程的应用标识")
	private String appSn = "APP00X";

	@SuppressWarnings("unchecked")
	@Override
	public void run(Long timerTaskId, Date runTime) {
		JdbcService jdbcService = (JdbcService) ContextUtil.getBean("jdbcService");
		List<Map<String, Object>> orgList = jdbcService.findList("SELECT o.ID,o.SN,o.NAME,o.PARENT_ID,oo.sn as PARENT_SN,oo.id as parentId from im_org o LEFT JOIN im_org oo on oo.ID=o.PARENT_ID  where o.company_sn='100001' and o.status=1 ORDER BY o.PARENT_ID asc");
		System.out.println("orgsize:"+orgList.size());
		Org org = new Org();
		org.setIsControl(false);
		Map<Long, String> ids = new HashMap<Long, String>();
		for (Map<String, Object> map : orgList) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("sn", map.get("SN"));
			params.put("parentSn",map.get("PARENT_SN"));
			params.put("name", map.get("NAME"));
			params.put("status", 1);
			params.put("secret", secret);
			HttpUtil.post(orgUrl, params);
		}
	
		// call org

		// 处理用户
		StringBuffer sql = new StringBuffer(
				" select a.LOGIN_NAME,a.LOGIN_PWD,a.STATUS,u.NAME,u.SN,u.SEX,u.EMAIL,u.TELEPHONE,ou.ORG_ID ");
		sql.append(" from IM_ACCOUNT a ");
		sql.append(" left join IM_USER u on u.ID = a.USER_ID ");
		sql.append(" left join IM_ORG_USER ou on ou.USER_ID = u.ID  ");
		sql.append(" where a.APP_ID  = (select ID from IM_APP where SN = '" + appSn + "' ) ");
		
		List<Map<String, Object>> userList = jdbcService.findList(sql.toString());
		// call user
		for (Map<String, Object> vo : userList) {
			Map<String, Object> params = new HashMap<String, Object>();
			String orgId = vo.get("ORG_ID") == null ? "0" : vo.get("ORG_ID").toString();
			params.put("id", vo.get("LOGIN_NAME"));
			params.put("sn", vo.get("SN"));
			params.put("password", UIM.decode(StringUtils.getString(vo.get("LOGIN_PWD"))));
			params.put("name", vo.get("NAME"));
			params.put("sex", vo.get("SEX"));
			params.put("email", vo.get("EMAIL"));
			params.put("telephone", vo.get("TELEPHONE"));
			params.put("status", vo.get("STATUS"));
			params.put("orgSn", ids.get(Long.valueOf(orgId)));
			params.put("secret", secret);

			HttpUtil.post(userUrl, params);
		}

	}

	/**
	 * 上下级排序算法
	 * 
	 * @param list
	 * @return
	 */
	protected List<Org> sort(Set<Long> ids, List<Org> list) {
		Map<Long, Org> result = new LinkedHashMap<Long, Org>();
		Iterator<Org> iterator = list.iterator();
		while (iterator.hasNext()) {
			Org org = iterator.next();
			if (org.getParentId() == null || !ids.contains(org.getParentId())
					|| result.keySet().contains(org.getParentId())) {
				result.put(org.getId(), org);
				iterator.remove();
			}
			if (!iterator.hasNext() && list.size() > 0) {
				iterator = list.iterator();
			}
		}
		return new ArrayList<>(result.values());

	}

}
