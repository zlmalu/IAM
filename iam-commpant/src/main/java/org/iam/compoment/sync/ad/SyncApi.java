package org.iam.compoment.sync.ad;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
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
@Name("AD同步")
public class SyncApi implements SyncInteface{
	
	@Param("服务器地址")
	private String host="10.156.1.30";
	@Param("端口好")
	private String port="636";
	@Param("管理员用户名")
	private String username="dandian@sunlife-everbright.com";
	@Param("管理员密码")
	private String password="Hello2018";
	@Param("操作类型(1= 新增,2=修改,3=删除)")
	private String optType="2";//1 add  2 edit 3delete
	@Param("用户根目录")
	private String baseDn="CN=Users, DC=test,DC=com";
	@Param("节点名称")
	private String idName="ou";
	@Param("包含的对象类，多个采用英文都好进行分割")
	private String objectclass="organizationalUnit";
	@Param("查询关键字,用户使用sAMAccountName,机构使用description")
	private String keyField="description";
	
	private DirContext getLdapContext() throws NamingException{
		Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldaps://"+host+":"+port);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put("java.naming.ldap.factory.socket", "com.sense.core.util.DummySSLSocketFactory");
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put(Context.URL_PKG_PREFIXES, "com.sun.jndi.url");
        env.put(Context.REFERRAL, "ignore");
        return new InitialLdapContext(env,null);
	}

	private void add(DirContext dirContext,Map<String,String> contentMap) throws Exception{
		Attributes attrs=new BasicAttributes();
		for (Map.Entry<String, String> entry : contentMap.entrySet()) {
			if(entry.getValue()!=null){
				if(entry.getKey().equalsIgnoreCase("unicodePwd")){
					attrs.put(new BasicAttribute(entry.getKey(), ("\""+entry.getValue()+"\"").getBytes("UTF-16LE")));
				}else{
					attrs.put(new BasicAttribute(entry.getKey(), entry.getValue()));
				}
			}
		}
		//添加objectclass
		Attribute attr=new BasicAttribute("objectClass");
		String[] objCls=objectclass.split(",");
		for(String ojc:objCls){
			if(ojc!=null && ojc.length()>0){
				attr.add(ojc);
			}
		}
		attrs.put(attr);
		dirContext.bind(idName+"="+contentMap.get(idName)+","+baseDn,null ,attrs);
	}
	
	private void edit(DirContext dirContext,Map<String,String> contentMap) throws NamingException, UnsupportedEncodingException{
		String idNameValue=contentMap.remove(idName);
		String keyFieldValue=contentMap.remove(keyField);
		String newName=contentMap.remove("newName");
		SearchControls controls=new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<SearchResult> nes=dirContext.search(baseDn, keyField+"="+keyFieldValue, controls);
    	Attributes allAttrs=null;
    	String oldName=null;
    	while(nes.hasMoreElements()){
    		SearchResult sr=nes.next();
    		allAttrs=sr.getAttributes();
    		oldName=sr.getName()+","+baseDn;
    		break;
    	}
    	if(!newName.equals(oldName)){
			rename(dirContext,oldName,newName);
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
    				if(entry.getKey().equalsIgnoreCase("unicodePwd")){
    					mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,new BasicAttribute(entry.getKey(),("\""+entry.getValue()+"\"").getBytes("UTF-16LE"))));
    					
    				}else{
    					mods.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,new BasicAttribute(entry.getKey(),entry.getValue())));
    				}
    			}
    		}
    	}
		//判断原始对象与现有对象是否一致
		dirContext.modifyAttributes(newName, mods.toArray(new ModificationItem[mods.size()]));
	}
	
	private void remove(DirContext dirContext,Map<String,String> contentMap) throws NamingException{
		dirContext.unbind(idName+"="+contentMap.get(idName)+","+baseDn);
	}
	
	/**
	 * 当机构移动时触发
	 */
	private void rename(DirContext dirContext,String oldName,String newName) throws NamingException{
		dirContext.rename(oldName, newName);
	}
	
	@Override
	public ResultCode execute(String content) {
		DirContext dirContext=null;
		try {
			Map<String,String> contentMap=XMLUtil.simpleXml2Map(content);
			dirContext=getLdapContext();
			String objectDn=contentMap.remove("baseDN");
			if(baseDn!=null){
				baseDn=objectDn;
			}
			if(optType.equals("1")){//add
				add(dirContext,contentMap);
			}else if(optType.equals("2")){
				edit(dirContext,contentMap);
			}else if(optType.equals("3")){
				remove(dirContext,contentMap);
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
	
	public static void main(String[] args) {
		new SyncApi().execute("<content><baseDN>OU=光大测试组织, OU=dandian, DC=sunlife-everbright,DC=com</baseDN><newName>OU=光大测试组织6,OU=天津测试,OU=光大测试组织, OU=dandian, DC=sunlife-everbright,DC=com</newName><description>1009281</description></content>");
	}

	
}
