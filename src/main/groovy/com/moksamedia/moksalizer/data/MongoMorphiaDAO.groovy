package com.moksamedia.moksalizer.data

import groovy.util.logging.Slf4j

import com.github.jmkgreen.morphia.Datastore
import com.github.jmkgreen.morphia.Morphia
import com.github.jmkgreen.morphia.mapping.MappingException
import com.github.jmkgreen.morphia.query.Query
import com.github.jmkgreen.morphia.utils.ReflectionUtils
import com.mongodb.Mongo

@Slf4j
class MongoMorphiaDAO {

	Mongo mongo
	Datastore ds	
	
	/*
	 * Should not know anything about test or credential datastores. This is
	 * controlled by controller and by config files.
	 */

	/**
	 * Map params:
	 * @param databaseName
	 * @param databaseHost
	 * @param databasePort
	 * @param dataObjectsPackage
	 */
	public MongoMorphiaDAO(Map params = [:]) {
				
		if (params?.dataObjectsPackage == null) { throw new IllegalArgumentException("Creation of MongoMorphiaDAO requires dataObjectsPackage.") }
		if (params?.databaseName == null) { throw new IllegalArgumentException("Creation of MongoMorphiaDAO requires databaseName.") }
		
		// Create Mongo instance
		if (params?.databaseHost != null && params?.databasePort != null) {
			mongo = new Mongo(params.databaseHost, params.databasePort)
		}
		else if (params?.databaseHost != null) {
			mongo = new Mongo(params.databaseHost)
		}
		else {
			mongo = new Mongo()
		}
				
		// Create Morphia instance
		Morphia morphia = new Morphia()
		
		// Map and inject all Entity and Embedded classes
		mapPackage(params.dataObjectsPackage, morphia)
				
		// Create datastore
		ds = morphia.createDatastore(mongo, params.databaseName)		
				
		// Inject some convenience methods
		injectConvenience()
	}
	
	private void injectConvenience() {
		// inject a filterMap method in the query object
		Query.metaClass.filterMap = { Map params ->
			params.each  { key, value ->
				delegate.filter(key, value)
			}
		}
	}
	
	/**
	 * Finds all @Entity or @Embedded classes in a package, maps them with Morphia and
	 * injects the Entity classes with the DAO methods. This code is essentially stolen
	 * from the Morphia.mapPackage() method.
	 * @param packageName
	 * @return
	 */
	private mapPackage(String packageName, Morphia morphia) {
		try {
			for (Class c : ReflectionUtils.getClasses(packageName)) {
				// Embedded
				if (ReflectionUtils.getClassEmbeddedAnnotation(c) != null) {
					morphia.map(c)
				}
				// Entity
				else if (ReflectionUtils.getClassEntityAnnotation(c) != null) {
					morphia.map(c)
					injectEntityClass(c)
				}
			}
		} catch (IOException ioex) {
			throw new MappingException("Could not get map classes from package " + packageName, ioex)
		} catch (ClassNotFoundException cnfex) {
			throw new MappingException("Could not get map classes from package " + packageName, cnfex)
		}
	}
	
	public getDatabaseName() {
		ds.getMongo().getDatabaseName()
	}
	
	private injectEntityClass = { Class clazz -> 
		
		clazz.metaClass.static.buildSearch = {
			new Search(clazz)
		}

		clazz.metaClass.save = {
			ds.save(delegate)
		}
		
		clazz.metaClass.remove = {
			ds.delete(delegate)
		}

		/*
		 *  Called by Search to determine if the search criteria have been
		 *  terminated and this DAO should be called below
		 */
		clazz.metaClass.static._shouldTakeFromSearch = { String name ->
			name =~ /^remove/ || name =~ /^get/
		}
		
		clazz.metaClass.static.search = {
			new Search(clazz, ds.createQuery(clazz))
		}

		clazz.metaClass.static.remove = { Query query ->
			ds.delete(query)
		}
		
		clazz.metaClass.static.getAll = { Query query ->
			query.asList()
		}

		clazz.metaClass.static.getCount = { Query query ->
			ds.getCount(query)
		}
			
		clazz.metaClass.static.getOne = { Query query ->
			query.get()
		}

		clazz.metaClass.static.getFromId = { def id ->
			ds.get(clazz, id)
		}
		
		clazz.metaClass.static.getBatch = { Query query, int batchNum, int batchSize ->
			query.limit(batchSize).offset(batchNum * batchSize).asList()
		}

		clazz.metaClass.static.dropCollection = {
			ds.getCollection(clazz).drop()
		}
		
		/*
		 *  Convenience: allows DataObject.getAll(map of params) instead of
		 *  DataObject.search().filterMap(params).getAll()
		 */
		
		clazz.metaClass.static.remove = { Map params ->
			ds.delete(clazz.search.filterMap(params))
		}
		
		clazz.metaClass.static.getAll = { Map params ->
			clazz.search.filterMap(params).asList()
		}

		clazz.metaClass.static.getCount = { Map params ->
			ds.getCount(clazz.search.filterMap(params))
		}
			
		clazz.metaClass.static.getOne = { Map params ->
			clazz.search.filterMap(params).get()
		}
		
		/*
		 * Convenience: allows DataType.getAll(), .getOne(), and .getCount()
		 * to get all, one, and the count of a given data type.
		 */
		
		clazz.metaClass.static.getAll = { 
			ds.find(clazz).asList()
		}

		clazz.metaClass.static.getCount = { 
			ds.getCount(clazz)
		}
			
		clazz.metaClass.static.getOne = { 
			ds.find(clazz).get()
		}


			
	}
	
}
