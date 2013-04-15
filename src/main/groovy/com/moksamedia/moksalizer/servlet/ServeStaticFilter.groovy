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

@Slf4j
class ServeStaticFilter implements Filter {

	public static final Set staticTypes = [
			'txt',
			'css',
			'jpg',
			'png',
			'html',
			'js'
		]
	
	public static String URL_PATH = 'static'
	public static String FILE_SYSTEM_PATH = 'static'
	
	public Pattern urlPathPattern
	public absolutePathOfStaticFiles
	
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
		urlPathPattern = Pattern.compile(/^\/${URL_PATH}/) // = '/static' at the beginning, or '^/static' in regex	
		absolutePathOfStaticFiles = new File(FILE_SYSTEM_PATH).getAbsolutePath()
	}
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
			String path = ((HttpServletRequest)request).getPathInfo()
			
			log.info "Context Path = " + path
						
			if (isStatic(path)) {
			
				//def filePath = path.replaceFirst(urlPathPattern, absolutePathOfStaticFiles)
				
				def filePath
				
				if (path[0] == '/') {
					filePath = FILE_SYSTEM_PATH + path
				}
				else {
					filePath = FILE_SYSTEM_PATH + '/' + path
				}
				
				
				//log.info "File Path = " + filePath
				
				ServeStaticFile serveStaticFile = new ServeStaticFile()
				
				serveStaticFile.serveStatic(filePath, response)
				
			}
			else {
				
				chain.doFilter(request, response)
				
			}
								
	}


}
