package com.sense.iam.api.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.sense.iam.api.model.im.TopData;


public class LoadAppFuncTreeUtil {

	private static List<TopData> menuCommon;
	private static List<TopData> list;

    public static List<TopData> menuList(List<TopData> menu) {
    	menuCommon=new ArrayList<TopData>();
        menuCommon = menu;
        list = new ArrayList<TopData>();
        Iterator<TopData> it=menu.iterator();
        while (it.hasNext()) {
        	
        	TopData x = it.next();
        	if(x==null)continue;
        	if (x.getParentId()!=null&&x.getParentId().longValue()==-1L) {
            	x.setChildren(menuChild(x.getId().longValue()));
                list.add(x);
                it.remove();
            }	
		}
        return list;
    }

    private static List<TopData> menuChild(long id) {
        List<TopData> lists = new ArrayList<TopData>();
        Iterator<TopData> it=menuCommon.iterator();
        while (it.hasNext()) {
        	TopData a = it.next();
        	if(a==null)continue;
            if (a.getParentId()!=null&&a.getParentId().longValue() == id) {
                a.setChildren(menuChild(a.getId()));
                lists.add(a);
            }
        }
        return lists;
    }    
}
