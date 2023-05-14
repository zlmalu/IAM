package org.iam.compoment.sync.ldap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import com.sense.core.util.XMLUtil;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.SyncInteface;

/**
 * LDAP数据同步API
 * 
 * Description: 
 * 
 * @author w_jfwen
 * 
 * Copyright 2005, 2015 Sense Software, Inc. All rights reserved.
 *
 */
@Name("LDAP同步")
public class SyncApi implements SyncInteface{
	
	@Param("服务器地址")
	private String host;
	@Param("端口号")
	private String port;
	@Param("管理员用户名")
	private String username;
	@Param("管理员密码")
	private String password;
	@Param("操作类型(1=新增,2=修改,3=删除,4=移动)")
	private String optType;//1 add  2 edit 3delete
	@Param("根目录")
	private String baseDn;
	@Param("节点名称")
	private String idName="uid";
	@Param("操作对象")
	private String objectclass="top,person";
	
	private DirContext getLdapContext() throws NamingException{
		Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://"+host+":"+port);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialLdapContext(env,null);
	}
	
	
	private void add(DirContext dirContext,Map<String,String> contentMap) throws Exception{
		Attributes attrs=new BasicAttributes();
		for (Map.Entry<String, String> entry : contentMap.entrySet()) {
			if(entry.getValue()!=null){
				attrs.put(new BasicAttribute(entry.getKey(), entry.getValue()));
				System.out.println(entry.getKey()+","+ entry.getValue().toString());
			}
		}
		//添加objectclass
		Attribute attr=new BasicAttribute("objectClass");
		String[] objCls=objectclass.split(",");
		for(String ojc:objCls){
			if(ojc!=null && ojc.length()>0){
				attr.add(ojc);
				System.out.println(ojc);
			}
		}
		attrs.put(attr);
		System.out.println(attrs);
		dirContext.bind(idName+"="+contentMap.get(idName)+","+baseDn,null ,attrs);
	}
	
	private void edit(DirContext dirContext,Map<String,String> contentMap) throws NamingException{
		SearchControls controls=new SearchControls();
		controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		NamingEnumeration<SearchResult> nes=dirContext.search(baseDn, idName+"="+contentMap.get(idName), null);
    	Attributes allAttrs=null;
    	while(nes.hasMoreElements()){
    		allAttrs=nes.next().getAttributes();
    		break;
    	}
    	List<ModificationItem> mods=new ArrayList<ModificationItem>();
    	if(allAttrs!=null){
    		for (Map.Entry<String, String> entry : contentMap.entrySet()) {
    			if(allAttrs.get(entry.getKey())!=null){
    				Object obj=entry.getValue();
    				if(obj==null || obj.toString().length()==0){
    					mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,new BasicAttribute(entry.getKey())));
    					
    				}else{
    					mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,new BasicAttribute(entry.getKey(),entry.getValue())));
    				}
    			}else{
    				mods.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,new BasicAttribute(entry.getKey(),entry.getValue())));
    			}
    		}
    	}
		//判断原始对象与现有对象是否一致
		dirContext.modifyAttributes(idName+"="+contentMap.get(idName)+","+baseDn, mods.toArray(new ModificationItem[mods.size()]));
	}
	
	private void remove(DirContext dirContext,Map<String,String> contentMap) throws NamingException{
		dirContext.unbind(idName+"="+contentMap.get(idName)+","+baseDn);
	}
	
	private void move(DirContext dirContext,Map<String,String> contentMap) throws NamingException{
		dirContext.rename(contentMap.get("oldName"),contentMap.get("newName"));
	}
	
	@Override
	public ResultCode execute(String content) {
		DirContext dirContext=null;
		try {
			Map<String,String> contentMap=XMLUtil.simpleXml2Map(content);
			dirContext=getLdapContext();
			if(optType.equals("1")){//add
				add(dirContext,contentMap);
			}else if(optType.equals("2")){//edit
				edit(dirContext,contentMap);
			}else if(optType.equals("3")){//remove
				remove(dirContext,contentMap);
			}else if(optType.equals("4")){//move
				move(dirContext,contentMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultCode(FAIL,e.getMessage());
		}finally{
			if(dirContext!=null){
				try {
					dirContext.close();
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}
		}
		return new ResultCode(SUCCESS,"");
	}
	
	
	
//	public static void main(String[] args) {
//		new SyncApi().execute("<content><ou>123</ou><cn>炫富科技</cn><parentOu>1233</parentOu></content>", "<config>"+
//				"<HOST>localhost</HOST>"+
//				"<PORT>389</PORT>"+
//				"<USERNAME>cn=Manager,dc=axbsec,dc=com</USERNAME>"+
//				"<PASSWORD>123456</PASSWORD>"+
//				"<OPT_TYPE>1</OPT_TYPE>"+
//				"<BASE_DN>cn=orgs,dc=axbsec,dc=com</BASE_DN>"+
//				"<ID_NAME>ou</ID_NAME>"+
//				"<OBJECTCLASS>organizationalUnit,top</OBJECTCLASS>"+
//				"</config>");
//	}

	
}
