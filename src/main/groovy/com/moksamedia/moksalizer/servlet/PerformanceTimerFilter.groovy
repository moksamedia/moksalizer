package com.moksamedia.moksalizer.servlet

import groovy.util.logging.Slf4j

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.moksamedia.moksalizer.Utility

@Slf4j
class PerformanceTimerFilter implements Filter  {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		long startTime, endTime
		
		String path = ((HttpServletRequest) request).getPathInfo()
		
		startTime = System.currentTimeMillis()
		chain.doFilter(request, response)
		endTime = System.currentTimeMillis()

		String contentLength = ((HttpServletResponse) response).getHeader("Content-Length")
		
		if (contentLength != null) {
			Long length = contentLength.toLong()
			log.info "${endTime - startTime}ms \t ${(Utility.formatBytes(length,true)).padRight(6)} \t $path"
	
		}
		else {
			log.info "${endTime - startTime}ms \t ?????? \t $path"
		}
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
