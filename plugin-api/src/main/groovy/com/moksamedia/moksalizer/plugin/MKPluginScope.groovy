package com.moksamedia.moksalizer.plugin

enum MKPluginScope {
	
	FIND ('FIND', 'Allows plugin to inject before search map is used to retrieve search objects.'), 
	CONTEXT ('CONTEXT', 'Allows plugins to inject before context is sent to render method.'), 
	RENDER ('RENDER', 'Allows plugins to modify context and template before final rendering done.')
	
	String scopeString, description
	
	public MKPluginScope(String val, String desc) {
		scopeString = val	
		description = desc
	}
	
	public static isValidString(String val) {
		MKPluginScope.values().findResult {
			it.scopeString == val ? true : null
		}
	}
	
}
