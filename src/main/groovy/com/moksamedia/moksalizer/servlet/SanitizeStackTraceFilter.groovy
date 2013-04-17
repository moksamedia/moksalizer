package com.moksamedia.moksalizer.servlet

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

import org.codehaus.groovy.runtime.StackTraceUtils

class SanitizeStackTraceFilter implements Filter {
		
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
					
		try {			
			chain.doFilter(request, response)
		}
		catch (Exception ex){
			//StackTraceUtils.addClassTest { it.contains('com.moksamedia') }
			throw StackTraceUtils.sanitizeRootCause(ex)
		}

	}

	@Override
	public void destroy() {}

}
