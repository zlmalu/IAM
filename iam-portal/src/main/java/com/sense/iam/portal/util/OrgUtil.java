package com.sense.iam.portal.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrgUtil {

	private static List<OrgTreeModel> menuCommon;
	private static List<OrgTreeModel> list;

    public static List<OrgTreeModel> menuList(List<OrgTreeModel> menu) {
    	menuCommon=new ArrayList<OrgTreeModel>();
        menuCommon = menu;
        list = new ArrayList<OrgTreeModel>();
        Iterator<OrgTreeModel> it=menu.iterator();
        while (it.hasNext()) {
        	OrgTreeModel x = it.next();
            if (x.getParentId().longValue()==-1) {
            	x.setChildren(menuChild(x.getId()));
                list.add(x);
                it.remove();
            }	
		}
        return list;
    }

    private static List<OrgTreeModel> menuChild(Long id) {
        List<OrgTreeModel> lists = new ArrayList<OrgTreeModel>();
        Iterator<OrgTreeModel> it=menuCommon.iterator();
        while (it.hasNext()) {
        	OrgTreeModel a = it.next();
            if (a.getParentId().longValue() == id.longValue()) {
                a.setChildren(menuChild(a.getId()));
                lists.add(a);
            }
        }
        return lists;
    }    
}
