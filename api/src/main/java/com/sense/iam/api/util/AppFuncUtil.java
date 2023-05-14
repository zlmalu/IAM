package com.sense.iam.api.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.sense.iam.model.im.AppFunc;

public class AppFuncUtil {

	private static List<AppFunc> menuCommon;
	private static List<AppFunc> list;

    public static List<AppFunc> menuList(List<AppFunc> menu) {
    	menuCommon=new ArrayList<AppFunc>();
        menuCommon = menu;
        list = new ArrayList<AppFunc>();
        Iterator<AppFunc> it=menu.iterator();
        while (it.hasNext()) {
        	AppFunc x = it.next();
            if (x.getFuncType().longValue() == 1) {
            	x.setChildren(menuChild(x.getId()));
                list.add(x);
                it.remove();
            }	
		}
        return list;
    }

    private static List<AppFunc> menuChild(long id) {
        List<AppFunc> lists = new ArrayList<AppFunc>();
        Iterator<AppFunc> it=menuCommon.iterator();
        while (it.hasNext()) {
        	AppFunc a = it.next();
            if (a.getParentId().longValue() == id) {
                a.setChildren(menuChild(a.getId()));
                lists.add(a);
            }
        }
        return lists;
    }    
}
