package com.moksamedia.moksalizer

import groovy.util.logging.Slf4j

import com.github.jmkgreen.morphia.Datastore

import java.lang.annotation.Annotation

@Slf4j
class MongoMorphiaDAO {

	def entityClasses = []
	def embeddedClasses = []
	
	Datastore ds	
	
	/*
	 * Should not know anything about test or credential datastores. This is
	 * controlled by controller and by config files.
	 */

	/**
	 * Params:
	 * databaseName
	 * databaseHost
	 * databasePort
	 */
	public MongoMorphiaDAO(Map params = [:]) {
		
		// Create Mongo instance
		
		// Create Morphia instance
		
		// Find all Entity and Embedded classes
		
		// Map all classes
		
		// Create datastore
				
		// Inject Entity classes with methods
		
	}
	
	public getDatabaseName() {
		ds.getMongo().getDatabaseName()
	}


	
}
