package com.moksamedia.moksalizer.plugin

import groovy.util.logging.Slf4j;

@Slf4j
class MKPluginException extends Exception {
	MKPluginException(String message) {
		super(message)
		log.error message
	}
	
	MKPluginException(String message, Exception ex) {
		super(message, ex)
		log.error message
	}

}
