package com.moksamedia.moksalizer.plugin

import groovy.util.logging.Slf4j

import java.util.regex.Pattern

import org.openide.util.Lookup

@Slf4j
@Singleton
class MKPluginService {	
	
	private Set plugins
	private Lookup lookup
	
	/*
	 * This is a map of request paths that are mapped to plugins that
	 * would like to serve these requests. 
	 */
	private List registrations = []
	

	/**
	 * Creates a new instance of MKPlugin service
	 */
	private MKPluginService() {
		lookup = Lookup.getDefault()
		plugins = lookup.lookupAll(MKPluginAbstract)
		
		log.info  "${plugins.size()} service plugins found."
		
		plugins.each { MKPlugin plugin ->
			log.info "${plugin.name} : ${plugin.description}"
			registerPlugin(plugin)
		}
	}
	
	private registerPlugin(MKPlugin plugin) {

		if (plugin.hasProperty('registrations')) {
			plugin.registrations.each {

				// need a path
				if (it?.path == null) {
					throw new MKPluginException("Failed to register plugin ${plugin.name}. No path value in registration.")
				}

				// need some scopes
				if (it?.scopes == null) {
					throw new MKPluginException("Failed to register plugin ${plugin.name}. No scopes value in registration.")
				}

				def scopes = it.scopes.inject([]) { def scope, acc ->

					// deal with scopes defined with strings
					if (scope instanceof String) {

						// reality check to see if string scope is valid
						if (!MKPluginScope.isValidString(scope)) {
							throw new MKPluginException("Scope string is not valid: ${scope}")
						}

						// get the scope enum object
						acc += MKPluginScope."$scope"
					}

					// already a MKPluginScope, so just add it
					else if (scope instanceof MKPluginScope) {
						acc += scope
					}

					// oops
					else {
						throw new MKPluginException("Unable to map scope: ${scope.toString()}")
					}
				}
				
				// compile regex paths into patterns for efficiency
				Pattern pattern = Pattern.compile(it.path)
				
				// add it to the registrations
				registrations += [pluginClass:plugin, pattern:pattern, scopes:it.scopes]
			}
		}

	}
	
	private getPlugins(String path, MKPluginScope scope) {
	
		registrations.inject([]) { registration, acc ->
			if (path =~ registration.pattern ) {
				if (registration.scopes.contains(scope)) {
					acc += registration.pluginClass
				}
			}
		}
			
	}
}