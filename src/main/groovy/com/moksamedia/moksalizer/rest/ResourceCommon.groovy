package com.moksamedia.moksalizer.rest

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo

import com.moksamedia.moksalizer.Controller
import com.moksamedia.moksalizer.MoksalizerTemplater
import com.moksamedia.moksalizer.PageContext

class ResourceCommon {

	// Injects a thread-local proxy
	@Context HttpServletResponse response
	@Context HttpServletRequest request
	@Context UriInfo uri
	
	// these are READ ONLY bc of thread safety issues
	final Map blogData = Controller.instance.blogData.getContext()
	final ConfigObject config = Controller.instance.config
		
	final Map cssCtx = ([
			contentWidth:800
		]).asImmutable()

	Closure stripLeadingSlash = { String str ->
		if (str == null || str == "") return str
		(str[0] == '/') ? str[1..-1] : str
	}
	
	/*
	 * These are the plugin hook methods. Plugins can inject themselves here by
	 * registering for request paths (regex compatible) and are given a chance
	 * to modify:
	 * 1) the search params
	 * 2) the context passed to the renderer
	 * 3) the template used and context passed to the renderer
	 */
	public def doFind(Map params) {
		// allow plugins to hook in here
		params
	}
	
	public def doContext(Map params) {
		// allow plugins to hook in here
		new PageContext(params)
	}
	
	public def doRender(String template, def context) {
		// allow plugins to hook in here
		render(template, context)
	}
	
	/*
	 * Don't think renderer is thread-safe, so probably need to create a new
	 * instance for each request rendering. Worth caching these? There's no
	 * state in the BlogratTemplater class.
	 */
	Closure render = { String template, def context ->
		MoksalizerTemplater templater = new MoksalizerTemplater()
		if (context instanceof PageContext) context = context.ctx
		templater.render(template, context)
	}


}
