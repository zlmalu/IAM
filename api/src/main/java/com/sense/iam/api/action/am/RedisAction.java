package com.sense.iam.api.action.am;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sense.core.util.JWTUtil;
import com.sense.core.util.PageList;
import com.sense.core.util.StringUtils;
import com.sense.iam.api.model.am.RedisReq;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiSort;
import net.sf.json.JSONObject;

/**
 * 用户会话 - Action
 *
 */
@Api(tags = "Redis管理")
@Controller
@RestController
@RequestMapping("amRedis")
@ApiSort(value = 16)
public class RedisAction {
	protected Log log=LogFactory.getLog(getClass());
	
	@Resource
	StringRedisTemplate stringRedisTemplate;
	
	private static String isRecordAdr;
	@Value("${login.record.address.enable}")
	public void setIsRecordAdr(String isRecordAdr) {
		this.isRecordAdr = isRecordAdr;
	}
	
	/**
	 * 分页查询列表
	 * @param entity 查询对象
	 * @param page 当前页码
	 * @param limit 读取条数
	 * @return
	 */
	@ApiOperation(value = "分页查询列表")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", required = true, dataType = "Integer", value = "页码,默认0",example="0"),
		@ApiImplicitParam(name = "limit", required = true, dataType = "Integer", value = "页大小，默认20页", example = "20")
	})
	@RequestMapping(value = "findList", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 2)
	protected PageList<RedisReq> findList(@RequestBody RedisReq entity, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit) {
		PageList<RedisReq> pagList=new PageList<RedisReq>();
		try {
			//查询门户的数据
			Set<String> keys =stringRedisTemplate.keys(Constants.CURRENT_REDIS_SESSION_ID+"*");
			Iterator<String> it = keys.iterator();
			List<RedisReq> list= new java.util.ArrayList<RedisReq>();
			while (it.hasNext()) {
				RedisReq model=new RedisReq();
				String key=it.next();
				String data=stringRedisTemplate.opsForValue().get(key);
				long timeOut=stringRedisTemplate.getExpire(key);
				key=key.replaceAll(Constants.CURRENT_REDIS_SESSION_ID+":", "");
				//解密加密token
				String payload=JWTUtil.parseToken(data, Constants.JWT_SECRECTKEY);
				JSONObject payloadJson=JSONObject.fromObject(payload);
				model.setSessionId(key);
				model.setTimeOut(timeOut);
				model.setEnTrdata(data);
				model.setData(payload.toString());
				model.setIp(getString(payloadJson,("ip")));
				model.setSn(getString(payloadJson,("sn")));
				model.setAccountId(getLong(payloadJson,("accountId")));
				model.setUserId(getLong(payloadJson,("userId")));
				model.setUsername(getString(payloadJson,("loginName")));
				model.setName(getString(payloadJson,("name")));
				model.setRemark(getString(payloadJson,("remark")));
				model.setCreateTime(getString(payloadJson,("createTime")));
				if(payloadJson.containsKey("device")){
					model.setDevice(payloadJson.getString("device"));
				}else{
					model.setDevice("PC");
				}
				list.add(model);
			}
			SimpleDateFormat sformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	        Collections.sort(list, new Comparator<RedisReq>(){
	            @Override
	        	public int compare(RedisReq month1, RedisReq month2) {
	        		int mark = 1;
	        		try {
	        		    Date dt1 = sformat.parse(month1.getCreateTime());
	        		    Date dt2 = sformat.parse(month2.getCreateTime());
	        		    if (dt1.getTime() > dt2.getTime()) {
	                        return -1;
	                    } else if (dt1.getTime() < dt2.getTime()) {
	                        return 1;
	                    } else {
	                        return 0;
	                    }
	        		} catch (ParseException e) {
	        			e.printStackTrace();
	        		}
	        		return mark;
	        	} 
	        });
			pagList=pageBySubList(entity.getSn(), list, limit, page);
			
		} catch (Exception e) {
			log.error("findList error:", e);
		}
		return pagList;
	}
	
	public String getString(JSONObject jsonObject,String key){
		if(jsonObject.containsKey(key)){
			return jsonObject.getString(key);
		}else{
			return "";
		}
	}
	
	public Long getLong(JSONObject jsonObject,String key){
		if(jsonObject.containsKey(key)){
			return jsonObject.getLong(key);
		}else{
			return null;
		}
	}
	
	/**
	 * 在线用户地址及用户数
	 * @return
	 */
	@ApiOperation(value = "获取地址及在线用户数")
	@RequestMapping(value = "findAddress", method = RequestMethod.GET)
	@ResponseBody
	protected Object findAddress() {
		if(isRecordAdr.equals("false"))return new JSONObject();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		List<Map<String, Object>> pList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> cList = new ArrayList<Map<String, Object>>();
		List<String> cityList=new ArrayList<String>();
		Set<String> provinceSet = new HashSet<String>();
		Set<String> keys =stringRedisTemplate.keys(Constants.CURRENT_REDIS_SESSION_ID+"*");
		Iterator<String> it = keys.iterator();
		try{
			while (it.hasNext()) {
				String key=it.next();
				String data=stringRedisTemplate.opsForValue().get(key);
				String payload=JWTUtil.parseToken(data, Constants.JWT_SECRECTKEY);
				JSONObject payloadJson=JSONObject.fromObject(payload);
				if(StringUtils.getString(payloadJson.get("address")).length()>0 && !JSONObject.fromObject(payloadJson.get("address")).isEmpty()){
					cityList.add(payloadJson.getString("address"));
					provinceSet.add(JSONObject.fromObject(payloadJson.getString("address")).getString("province"));
				}
			}
			Map<String,Integer> cityMap = new HashMap<>();
			for (String str : cityList) {
				Integer i = 1; 
				if(cityMap.get(str) != null){
	                i=cityMap.get(str)+1;
	            }
				cityMap.put(str,i);
			}
			
			for (String p : provinceSet) {
				Map<String, Object> provinceMap=new HashMap<String, Object>();
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (String key : cityMap.keySet()) {
					if(p.equals(JSONObject.fromObject(key).get("province").toString())){
						Map<String, Object> map=new HashMap<String, Object>();
						map.put("name", JSONObject.fromObject(key).get("city").toString());
						map.put("value", cityMap.get(key));
						list.add(map);
					}
				}
				provinceMap.put("name", processProvince(p));
				provinceMap.put("value", list);
				cList.add(provinceMap);
				Map<String, Object> pMap=new HashMap<String, Object>();
				pMap.put("name", processProvince(p));
				pMap.put("value", list.size());
				pList.add(pMap);
			}
			List<Map<String, Object>> pcList = new ArrayList<Map<String, Object>>();
			for (String key : cityMap.keySet()) {
				Map<String, Object> pcMap=new HashMap<String, Object>();
				pcMap.put("province", JSONObject.fromObject(key).get("province").toString());
				pcMap.put("city", JSONObject.fromObject(key).get("city").toString());
				pcMap.put("nums", cityMap.get(key));
				pcList.add(pcMap);
			}
			Collections.sort(pcList, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> o1, Map<String, Object> o2) {
					Integer o1Value = Integer.valueOf(o1.get("nums").toString());
		            Integer o2Value = Integer.valueOf(o2.get("nums").toString());
		            return o2Value.compareTo(o1Value);
				}
			});
			dataMap.put("provinceData", pList);
			dataMap.put("cityData", cList);
			dataMap.put("sortData", pcList.size()>10?pcList.subList(0, 10):pcList);
		} catch (Exception e) {
			log.error("findList error:", e);
		}
		return dataMap;
	}
	
	/**
	 * 获取全部会话
	 * @return
	 */
	@ApiOperation(value = "获取全部会话")
	@RequestMapping(value = "findAll", method = RequestMethod.GET)
	@ResponseBody
	protected List<RedisReq> findAll() {
		List<RedisReq> list= new java.util.ArrayList<RedisReq>();
		try {
			//查询门户的数据
			Set<String> keys =stringRedisTemplate.keys(Constants.CURRENT_REDIS_SESSION_ID+"*");
			Iterator<String> it = keys.iterator();
			
			while (it.hasNext()) {
				RedisReq model=new RedisReq();
				String key=it.next();
				String data=stringRedisTemplate.opsForValue().get(key);
				long timeOut=stringRedisTemplate.getExpire(key);
				key=key.replaceAll(Constants.CURRENT_REDIS_SESSION_ID+":", "");
				//解密加密token
				String payload=JWTUtil.parseToken(data, Constants.JWT_SECRECTKEY);
				JSONObject payloadJson=JSONObject.fromObject(payload);
				model.setSessionId(key);
				model.setTimeOut(timeOut);
				model.setEnTrdata(data);
				model.setData(payload.toString());
				model.setIp(payloadJson.getString("ip"));
				model.setSn(payloadJson.getString("sn"));
				model.setAccountId(payloadJson.getLong("accountId"));
				model.setUserId(payloadJson.getLong("userId"));
				model.setUsername(payloadJson.getString("loginName"));
				model.setName(payloadJson.getString("name"));
				model.setRemark(payloadJson.getString("remark"));
				list.add(model);
			}
		} catch (Exception e) {
			log.error("findAll error:", e);
		}
		return list;
	}
	
	
	@ApiOperation(value="移除")
	@RequestMapping(value="remove", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperationSupport(order = 1)
	public ResultCode remove(@RequestBody @ApiParam(name = "唯一标识集合", value="多数据采取英文逗号分割", required = true)List<String> ids) {
		try{
			if(ids!=null&&ids.size()>0){
				for(int i=0;i<ids.size();i++){
					stringRedisTemplate.delete(ids.get(i));
				}
			}
			return new ResultCode(Constants.OPERATION_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			return new ResultCode(Constants.OPERATION_FAIL);
		}	
	}
	
	/**
     * 利用subList方法进行分页
     * @param sn 用户工号查询条件
     * @param list 分页数据
     * @param pagesize  页面大小
     * @param currentPage   当前页面
     */
    public static PageList<RedisReq> pageBySubList(String sn,List<RedisReq> list, int pagesize, int currentPage) {
    	
    	if(currentPage==0){
    		currentPage=1;
    	}
    	PageList<RedisReq> pagList=new PageList<RedisReq>();
    	List<RedisReq> selectsubList=new ArrayList<RedisReq>();
    	if(sn!=null&&sn.length()>0){
    		for(int i=0;i<list.size();i++){
    			if(list.get(i).getSn().equals(sn)){
    				selectsubList.add(list.get(i));
    			}
    		}
    	}else{
    		selectsubList=list;
    	}
    	
        int totalcount = selectsubList.size();
        int pagecount = 0;
        List<RedisReq> subList=new ArrayList<RedisReq>();
        int m = totalcount % pagesize;
        if (m > 0) {
            pagecount = totalcount / pagesize + 1;
        } else {
            pagecount = totalcount / pagesize;
        }
        if (m == 0) {
            subList = selectsubList.subList((currentPage - 1) * pagesize, pagesize * (currentPage));
        } else {
            if (currentPage == pagecount) {
                subList = selectsubList.subList((currentPage - 1) * pagesize, totalcount);
            } else {
                subList = selectsubList.subList((currentPage - 1) * pagesize, pagesize * (currentPage));
            }
        }
        pagList.setCurrentPage(currentPage);
        pagList.setDataList(subList);
        pagList.setTotalcount(totalcount);
        pagList.setPageSize(pagesize);
        return pagList;
    }

    
    private static String processProvince(String p) {
    	if(p.contains("省")){
    		return p.replace("省", "");
    	}else if(p.contains("市")){
    		return p.replace("市", "");
    	}else if(p.contains("自治区")){
    		return p.replace("自治区", "");
    	}else if(p.contains("壮族自治区")){
    		return p.replace("壮族自治区", "");
    	}else if(p.contains("回族自治区")){
    		return p.replace("回族自治区", "");
    	}else if(p.contains("维吾尔自治区")){
    		return p.replace("维吾尔自治区", "");
    	}else if(p.contains("特别行政区")){
    		return p.replace("特别行政区", "");
    	}
		return p;
	}
}
