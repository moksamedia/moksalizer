package com.moksamedia.moksalizer.exception

import groovy.util.logging.Slf4j

import org.slf4j.LoggerFactory

@Slf4j
class MoksalizerException extends Exception {
	MoksalizerException(String message) {
		super(message)
		log.error message
	}
}
