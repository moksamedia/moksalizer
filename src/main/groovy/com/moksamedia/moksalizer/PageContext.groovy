package com.moksamedia.moksalizer

import javax.servlet.http.HttpServletRequest

import com.moksamedia.moksalizer.security.MoksalizerRealm


class PageContext {

	// This is final and immutable to avoid threading problems
	public static final Map blogCtx = Controller.instance.blogData.getImmutableContext()

	public Map ctx = [:]
	
	public PageContext(Map moreCtx = [:]) {
		ctx += blogCtx 
		ctx += moreCtx
		ctx += [admin:MoksalizerRealm.isAdmin(), alreadyLoggedIn:MoksalizerRealm.isAuthenticated()]
		Map toPass = [:]
		toPass += blogCtx
		toPass.remove('admin')
		passToPage(toPass)
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
