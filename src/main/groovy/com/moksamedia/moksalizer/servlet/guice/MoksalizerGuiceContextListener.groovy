package com.moksamedia.moksalizer.servlet.guice

import groovy.util.logging.Slf4j

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent

import com.github.jmkgreen.morphia.logging.MorphiaLoggerFactory
import com.github.jmkgreen.morphia.logging.slf4j.SLF4JLogrImplFactory
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.servlet.GuiceServletContextListener
import com.moksamedia.moksalizer.Controller


@Slf4j
class MoksalizerGuiceContextListener extends GuiceServletContextListener {

	private ServletContext servletContext
	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		log.info "INITIALIZING CONTEXT"
		MorphiaLoggerFactory.reset()
		MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory)
		
		Controller.onServerStart()

		this.servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
	}
	
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new MoksalizerJerseyServletModule(),new MoksalizerShiroWebModule(this.servletContext));
	}
}
