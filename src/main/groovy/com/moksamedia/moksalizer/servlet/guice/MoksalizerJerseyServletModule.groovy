package com.moksamedia.moksalizer.servlet.guice

import org.apache.shiro.guice.web.ShiroWebModule

import com.moksamedia.moksalizer.servlet.ServeStaticFilter;
import com.sun.jersey.api.core.PackagesResourceConfig
import com.sun.jersey.guice.JerseyServletModule
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer

class MoksalizerJerseyServletModule extends JerseyServletModule {

	public final packages = 'com.moksamedia.moksalizer.rest'
	
	@Override
	protected void configureServlets() {

		// serve static filter
		bind(ServeStaticFilter).asEagerSingleton()
		filter("/*").through(ServeStaticFilter)
				
		
		def params = [ (PackagesResourceConfig.PROPERTY_PACKAGES) : packages]
		serve("/*").with(GuiceContainer, params)
		
		ShiroWebModule.bindGuiceFilter("/*", binder())
		
	}
	
}
