package com.moksamedia.moksalizer.plugin

import groovy.util.logging.Slf4j

import org.openide.util.Lookup

@Slf4j
@Singleton
class MKPluginService {

	private Set plugins
	private Lookup lookup;

	/**
	 * Creates a new instance of DictionaryService
	 */
	private MKPluginService() {
		lookup = Lookup.getDefault()
		plugins = lookup.lookupAll(MKPluginAbstract)
		
		log.info  "${plugins.size()} service plugins found."
		
		plugins.each {
			log.info "${it.name} : ${it.description}"
		}
	}
}