package com.sense.iam.api.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sense.iam.api.model.im.OrgApiTree;

public class OrgCUtil {
	
	 public static Map<String, Object> mapArray = new LinkedHashMap<String, Object>();
	    public List<OrgApiTree> menuCommon;
	    public List<OrgApiTree> list = new ArrayList<OrgApiTree>();

	    public List<OrgApiTree> menuList(List<OrgApiTree> menu) {
	        this.menuCommon = menu;
	        Iterator<OrgApiTree> it=menu.iterator();
	        while (it.hasNext()) {
	        	OrgApiTree x = it.next();
	        	x.setChildren( menuChild(x.getId()));
                list.add(x);
                it.remove();
			}
	        return list;
	    }

	    public List<OrgApiTree> menuChild(long id) {
	        List<OrgApiTree> lists = new ArrayList<OrgApiTree>();
	        Iterator<OrgApiTree> it=menuCommon.iterator();
	        while (it.hasNext()) {
	        	OrgApiTree a = it.next();
	            if (a.getParentId() == id) {
	            	a.setChildren(menuChild(a.getId()));
	                lists.add(a);
	            }
	        }
	        return lists;
	    }
}
