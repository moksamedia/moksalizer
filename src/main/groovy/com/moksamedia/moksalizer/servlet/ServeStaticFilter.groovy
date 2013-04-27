package com.moksamedia.moksalizer.servlet

import groovy.util.logging.Slf4j

import java.util.regex.Pattern

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

import com.moksamedia.moksalizer.Controller


@Slf4j
@Singleton
class ServeStaticFilter implements Filter {

	public ServeStaticFilter() {
		
	}
	
	public static final Set staticTypes = [
			'txt',
			'css',
			'jpg',
			'png',
			'html',
			'js'
		]
		
	Closure isStatic = { String file ->
				
		assert file != null
				
		List parts = file.tokenize('.')	
				
		if (parts.size() > 1) {
			staticTypes.contains(parts.last())
		}
		else {
			false
		}
		
		
	}	
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
			/*
			 * Note: This was .getPathInfo(), but with Guice I had to change it to
			 * getServletPath()
			 */
			String path = ((HttpServletRequest)request).getPathInfo()
			
			//log.info "getServletPath() = " + path
			//log.info "getRequestURI() = " + request.getRequestURI()
			//log.info "getPathInfo() = " + request.getPathInfo()
			
			if (isStatic(path)) {
							
				def filePath
				
				// get the root template directory from the config file
				def filePathPrefix = Controller.instance.config.staticRoot
				
				if (path[0] == '/') {
					filePath = filePathPrefix + path
				}
				else {
					filePath = filePathPrefix + '/' + path
				}
								
				new ServeStaticFile().serveStatic(filePath, response)
				
			}
			else {
				
				chain.doFilter(request, response)
				
			}
								
	}


}
