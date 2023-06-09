package com.sense.iam.sso;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sense.core.util.ContextUtil;
 
/**
 * @author ypp 创建时间：2018年10月17日 上午11:59:11
 * @Description: TODO(用一句话描述该文件做什么)
 */
@Component
@WebListener
public class StartListener implements ApplicationContextAware, ServletContextListener {
	
	protected Log log=LogFactory.getLog(getClass());
	/**
	 * 上下文对象实例
	 */
	private ApplicationContext applicationContext;
 
	private ServletContext servletContext;
 
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
 
	/**
	 * 获取applicationContext
	 * 
	 * @return
	 */
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
 
	/**
	 * 获取servletContext
	 * 
	 * @return
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}
 
	/**
	 * 通过name获取 Bean.
	 * 
	 * @param name
	 * @return
	 */
	public Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}
 
	/**
	 * 通过class获取Bean.
	 * 
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}
 
	/**
	 * 通过name,以及Clazz返回指定的Bean
	 * 
	 * @param name
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T> T getBean(String name, Class<T> clazz) {
		Assert.hasText(name, "name为空");
		return getApplicationContext().getBean(name, clazz);
	}
 
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		this.servletContext = sce.getServletContext();
		ContextUtil.context=WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
		log.info("spring boot listener applicationContext:"+ContextUtil.context);
	}
 
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
 
	}
 
}
