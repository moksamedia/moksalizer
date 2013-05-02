package com.moksamedia.moksalizer.rest

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

import org.apache.shiro.SecurityUtils

import com.moksamedia.moksalizer.PageContext

class LoginLogout extends ResourceCommon {

	/*
	 * The authc shiro filter takes care of the actual login by intercepting a POST
	 * to /login, and retrieving the username, password, and rememberme form values.
	 */
	@GET
	@Produces('text/html')
	@Path('login')
	public String login() {

		def context = new PageContext([
			windowTitle:"Login",
			body:'login'])

		render('shell.html', context)
		
	}

	/*
	 * This should actually never get reached bc the authc filter catches
	 * the logout and does it automatically.
	 */
	@Path('logout')
	public String logout() {
		SecurityUtils.getSubject().logout()
		response.sendRedirect(uri.getBaseUri())
	}

}
