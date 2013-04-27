package com.moksamedia.moksalizer

import com.moksamedia.moksalizer.security.MoksalizerRealm


class PageContext {

	Map ctx = [:]
	
	public PageContext(Map moreCtx = [:]) {
		
		Map blogCtx = Controller.instance.blogData.getContext()
		ctx += blogCtx 
		ctx += moreCtx
		ctx += [admin:MoksalizerRealm.isAdmin(), alreadyLoggedIn:MoksalizerRealm.isAuthenticated()]
		
		passToPage(blogCtx)
	}
		
	public PageContext add(def params) {
		ctx += params
		this
	}
	
	public void passToPage(Map params) {
		if (ctx.containsKey('passToPage')) {
			ctx.passToPage += params
		}
		else {
			ctx += [passToPage:params]
		}
	}
	
}
