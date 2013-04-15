package com.moksamedia.moksalizer.servlet

import groovy.util.logging.Slf4j

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import org.codehaus.groovy.runtime.StackTraceUtils

import com.github.jmkgreen.morphia.logging.MorphiaLoggerFactory
import com.github.jmkgreen.morphia.logging.slf4j.SLF4JLogrImplFactory
import com.moksamedia.moksalizer.Controller
import com.moksamedia.moksalizer.data.objects.BlogData
import com.moksamedia.moksalizer.exception.MoksalizerException


@Slf4j
public class ServletInitializer implements ServletContextListener
{
	public void contextInitialized(ServletContextEvent sce)
	{
		log.info "INITIALIZING CONTEXT"
		MorphiaLoggerFactory.reset()
		MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory)
		
		Controller.onServerStart()
					
		/*
		BlogData blogData = BlogData.getOne()
		
		if (blogData == null) {
			log.error 'Unable to load BlogData object.'
			throw new MoksalizerException('Unable to load BlogData object.')
		}
		else {
			ServletContext context = sce.getServletContext()
			Map blogDataContext = blogData.getContext()
			log.info "blogDataContext = ${blogDataContext.toMapString()}"
			context.setAttribute(Controller.BLOGDATA_KEY, blogData.getContext())
		}
		*/
		
	}


	public void contextDestroyed(ServletContextEvent arg0)
	{
		log.info "CLOSING CONTEXT"
		Controller.onServerStop()
	}

}