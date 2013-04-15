package com.moksamedia.moksalizer

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/*
 * Returns an empty string if the property isn't found. This is used to avoid
 * having to check for existence of binding in templates and avoid property
 * not found errors if binding doesn't exist.
 */
class SafeMap {
	
	final def DEFAULT_VALUE = ""
	
    @Delegate Map storage = [:]
	
	public SafeMap(def init) {
		storage += init
	}

	public SafeMap() {
	}

    def getProperty(String name) { 
		if (storage.containsKey(name)) {
			storage[name] 
		}
		else {
			DEFAULT_VALUE
		}
	}
	
	def plus(Map other) {		
		storage += other
		this
	}
	
	def minus(Map other) {
		storage -= other
		this
	}
	
	def methodMissing(String name, args) {
		storage.invokeMethod(name, args)
	}

	public Object get(Object key) {
		storage.containsKey(key) ? storage[key] : DEFAULT_VALUE
	}


}
