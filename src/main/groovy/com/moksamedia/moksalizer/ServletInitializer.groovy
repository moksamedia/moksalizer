package com.moksamedia.moksalizer

import groovy.util.logging.Slf4j

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener


@Slf4j
public class ServletInitializer implements ServletContextListener
{
	public void contextInitialized(ServletContextEvent arg0)
	{
		log.info "INITIALIZING CONTEXT"
	}


	public void contextDestroyed(ServletContextEvent arg0)
	{
		log.info "CLOSING CONTEXT"
	}

}