package com.moksamedia.moksalizer.plugin

import groovy.util.logging.Slf4j

import org.openide.util.Lookup
import org.openide.util.Lookup.Template

@Slf4j
@Singleton
class MKPluginService {

	private Class pluginInterface = MKPlugin
	private Lookup lookup
	private Lookup.Template template
	private Lookup.Result results
	private def plugins

	private MKPluginService() {
		
		log.info "Loading plugin service"
		
		lookup = Lookup.getDefault()
		template = new Lookup.Template(MKPlugin)
		results = lookup.lookup(template)
		
		log.info "${results.allClasses().toString()}"
		
		plugins = Lookup.getDefault().lookupAll(pluginInterface);
		
		log.info "${plugins.size()} plugins found"
		
		plugins.each {
			log.info "${it.class.simpleName} registered. ${it.name}:${it.description}"
		}
		
	}

}
