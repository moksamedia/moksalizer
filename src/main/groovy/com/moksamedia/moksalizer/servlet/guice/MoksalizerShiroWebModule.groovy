package com.moksamedia.moksalizer.servlet.guice

import groovy.util.logging.Slf4j

import javax.servlet.ServletContext

import org.apache.shiro.authc.credential.CredentialsMatcher
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.config.Ini
import org.apache.shiro.guice.web.ShiroWebModule

import com.google.inject.Provides
import com.google.inject.name.Names
import com.moksamedia.moksalizer.security.MoksalizerRealm

@Slf4j
class MoksalizerShiroWebModule extends ShiroWebModule {
	
	MoksalizerShiroWebModule(ServletContext sc) {
		super(sc);
	}

	protected void configureShiroWeb() {
		try {
			bind(CredentialsMatcher).to(HashedCredentialsMatcher)
			bind(HashedCredentialsMatcher)
			bindRealm().to(MoksalizerRealm).asEagerSingleton()
			
			bindConstant().annotatedWith(Names.named("shiro.loginUrl")).to('/login')
			
			addFilterChain("/login", ANON)
			addFilterChain("/**", AUTHC)
			
		} catch (NoSuchMethodException e) {
			addError(e);
		}

	}

	@Provides
	Ini loadShiroIni() {
		log.info "Loading Shiro INI"
		return Ini.fromResourcePath("classpath:shiro.ini");
	}
}