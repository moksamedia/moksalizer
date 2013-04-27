package com.moksamedia.moksalizer.servlet

import groovy.util.logging.Slf4j

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import org.apache.shiro.web.env.MutableWebEnvironment
import org.apache.shiro.web.env.WebEnvironment
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter
import org.apache.shiro.web.filter.mgt.FilterChainManager
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver
import org.apache.shiro.web.util.WebUtils

import com.github.jmkgreen.morphia.logging.MorphiaLoggerFactory
import com.github.jmkgreen.morphia.logging.slf4j.SLF4JLogrImplFactory
import com.moksamedia.moksalizer.Controller

@Slf4j
public class ServletInitializer implements ServletContextListener
{
	public void contextInitialized(ServletContextEvent sce)
	{
		log.info "INITIALIZING CONTEXT"
		
		/*
		 * This was necessary to use SLF4J with Morphia to avoid some annoying
		 * warning/errors on startup.
		 */
		MorphiaLoggerFactory.reset()
		MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory)
				
		/*
		 * Get the Controller going. Initializes DAO, which connects to mongo
		 * database as well as injects data objects with access methods.
		 */
		Controller.onServerStart()
		
		/*
		 * Get Shiro FilterChainManager so that filter chain can be
		 * configured.
		 */
		ServletContext context = sce.getServletContext()		
		WebEnvironment we = WebUtils.getWebEnvironment(context)
		PathMatchingFilterChainResolver filterChainResolver = we.getFilterChainResolver()
		
		// If there's no [urls] section in the shiro.ini, then the filterChainResolver
		// will not have been created, so need to create it.
		if (filterChainResolver == null) {
			filterChainResolver = new PathMatchingFilterChainResolver()
			((MutableWebEnvironment)we).setFilterChainResolver(filterChainResolver)
		}
		
		// the FilterChainManager
		FilterChainManager filterChainManager = filterChainResolver.getFilterChainManager()
		
		assert filterChainManager != null
		
		// LOGIN
		filterChainManager.addToChain('/login', 'ssl', '8443')
		filterChainManager.addToChain('/login', 'authc')
		
		// LOGOUT
		filterChainManager.addToChain('/logout', 'logout')
		
		def filters = filterChainManager.getFilters()
		
		FormAuthenticationFilter authc = filters['authc']
		
		authc.setLoginUrl('/login')
				
		
	}


	public void contextDestroyed(ServletContextEvent arg0)
	{
		log.info "CLOSING CONTEXT"
		Controller.onServerStop()
		ServeStaticFile.nullCache()
	}

}