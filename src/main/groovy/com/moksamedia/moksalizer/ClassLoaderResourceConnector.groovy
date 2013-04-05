package com.moksamedia.moksalizer

import groovy.util.ResourceConnector;
import groovy.util.ResourceException;

import java.net.URLConnection;

/**
 * Used in conjunction with GroovyScriptEngine to load scripts from war (bootstrap.groovy).
 * @author cantgetnosleep
 *
 */
class ClassLoaderResourceConnector implements ResourceConnector  {
	
	Class clazz;
  
	public ClassLoaderResourceConnector(Class clazz) {
		this.clazz = clazz;
	}

	@Override
	public URLConnection getResourceConnection(final String resource) throws ResourceException
	{
		URL url = clazz.getResource(resource);
		if( url == null ) {
			throw new ResourceException("Resource not found: " + resource);
		}
	  
		try
		{
			return url.openConnection();
		} 
		catch (IOException ex)
		{
			throw new ResourceException("An IO exception occurred", ex);
		}
	}
	
}
