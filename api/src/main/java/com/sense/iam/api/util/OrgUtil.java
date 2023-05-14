package com.sense.iam.api.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sense.iam.api.model.im.OrgTree;

public class OrgUtil {
	
	 public static Map<String, Object> mapArray = new LinkedHashMap<String, Object>();
	    public List<OrgTree> menuCommon;
	    public List<Object> list = new ArrayList<Object>();

	    public List<Object> menuList(List<OrgTree> menu) {
	        this.menuCommon = menu;
	        Iterator<OrgTree> it=menu.iterator();
	        while (it.hasNext()) {
	        	OrgTree x = it.next();
	        	Map<String, Object> mapArr = new LinkedHashMap<String, Object>();
	            if (x.getParent_id() == -1) {
	                mapArr.put("id", x.getId());
	                mapArr.put("sn", x.getSn());
	                mapArr.put("name", x.getName());
	                mapArr.put("pid", x.getParent_id());
	                mapArr.put("usercount", x.getUsercount());
	                mapArr.put("children", menuChild(x.getId()));
	                list.add(mapArr);
	                //移除元素，减少递归层级
	                it.remove();
	            }	
			}
	        return list;
	    }

	    public List<?> menuChild(long id) {
	        List<Object> lists = new ArrayList<Object>();
	        Iterator<OrgTree> it=menuCommon.iterator();
	        while (it.hasNext()) {
	        	OrgTree a = it.next();
	            Map<String, Object> childArray = new LinkedHashMap<String, Object>();
	            if (a.getParent_id() == id) {
	                childArray.put("id", a.getId());
	                childArray.put("sn", a.getSn());
	                childArray.put("name", a.getName());
	                childArray.put("pid", a.getParent_id());
	                childArray.put("usercount", a.getUsercount());
	                childArray.put("children", menuChild(a.getId()));
	                lists.add(childArray);
	            }
	        }
	        return lists;
	    }
}
