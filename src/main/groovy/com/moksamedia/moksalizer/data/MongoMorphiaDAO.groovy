package com.moksamedia.moksalizer.data

import groovy.util.logging.Slf4j

import com.github.jmkgreen.morphia.Datastore
import com.github.jmkgreen.morphia.Morphia
import com.github.jmkgreen.morphia.mapping.MappingException
import com.github.jmkgreen.morphia.query.Query
import com.github.jmkgreen.morphia.utils.ReflectionUtils
import com.moksamedia.moksalizer.data.objects.Role
import com.moksamedia.moksalizer.data.objects.User
import com.moksamedia.moksalizer.exception.MoksalizerException
import com.mongodb.MongoClient


@Slf4j
class MongoMorphiaDAO {

	MongoClient mongo
	Datastore ds	
	Morphia morphia
	
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
		
		log.info "PARAMS=${params.toMapString()}"		
		
		// Make sure we have a data objects package and a database name
		if (params?.dataObjectsPackage == null) { throw new IllegalArgumentException("Creation of MongoMorphiaDAO requires dataObjectsPackage.") }
		if (params?.databaseName == null) { throw new IllegalArgumentException("Creation of MongoMorphiaDAO requires databaseName.") }
				
		// Set default host and port, if necessary
		params.databaseHost = params.get('databaseHost', 'localhost')
		params.databasePort = params.get('databasePort', 27017)
		
		log.info "Creating MongoClient with ${params.databaseHost}:${params.databasePort}"
		mongo = new MongoClient(params.databaseHost, params.databasePort)
			
		// Create Morphia instance
		morphia = new Morphia()
		
		//Role.metaClass.people = [] as List<User>
		
		// Map and inject all Entity and Embedded classes
		mapPackage(params.dataObjectsPackage, morphia)
				
		// Create datastore
		log.info "Creating datastore for database: ${params.databaseName}"
		ds = morphia.createDatastore(mongo, params.databaseName)		
				
		// Inject some convenience methods
		injectConvenience()
	}
	
	/**
	 * Inject some convenience methods on classes
	 */
	private void injectConvenience() {
		// inject a filterMap method in the query object
		Query.metaClass.filterMap = { Map params ->
			params.each  { key, value ->
				delegate.filter(key, value)
			}
			delegate
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
		
		def embedded = [], entity = []
		
		try {
			for (Class c : ReflectionUtils.getClasses(packageName)) {
				// Embedded
				if (ReflectionUtils.getClassEmbeddedAnnotation(c) != null) {
					embedded += [c]
					morphia.map(c)
				}
				// Entity
				else if (ReflectionUtils.getClassEntityAnnotation(c) != null) {
					entity += [c]
					morphia.map(c)
					injectEntityClass(c)
				}
			}
		} catch (IOException ioex) {
			throw new MappingException("Could not get map classes from package " + packageName, ioex)
		} catch (ClassNotFoundException cnfex) {
			throw new MappingException("Could not get map classes from package " + packageName, cnfex)
		}
		
		log.info "@Entity classes found:"; entity.each { log.info "--" + it.simpleName + " (${it.package})" }
		log.info "@Embedded classes found:"; embedded.each { log.info "--" + it.simpleName + " (${it.package})" }
	}
	
	/**
	 * Returns the name of the Mongo database being used
	 * @return database name
	 */
	public getDatabaseName() {
		ds.getMongo().getDatabaseName()
	}
	
	/**
	 * Inject the data object classes with the main convenience methods used to find,
	 * get, remove, and count data objects.
	 */
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
			clazz.search().filterMap(params).asList()
		}

		clazz.metaClass.static.getCount = { Map params ->
			ds.getCount(clazz.search.filterMap(params))
		}
			
		clazz.metaClass.static.getOne = { Map params ->
			clazz.search().filterMap(params).query.get()
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

		clazz.metaClass.static.find = { Map prm = [:] ->
			
			Query query = ds.createQuery(clazz)
			
			def keys = prm.keySet()
						
			prm.each { k, v ->
				
				if (k == 'search') {
					assert v instanceof Map
					query.filterMap(v)
				}
				else if (k == 'sort') {
					assert v instanceof String
					query.order(v)
				}
				else if (k == 'withOnly') {
					assert v instanceof String || v instanceof List
					assert !keys.contains('withoutFields')
					query.retrievedFields(true, v as String[])
				}
				else if (k == 'without') {
					assert v instanceof String || v instanceof List
					assert !keys.contains('onlyFields')
					query.retrievedFields(false, v as String[])
				}
				else if (k == 'limit') {
					assert v instanceof Integer && v > 0
					query.limit(v)
				}
				
			}
			/*
			 * Deal with batches. batchSize AND batchNum (or batchNumber) must be defined
			 * for a batch. Will throw an error if one is defined without the other.
			 */
			if (prm.containsKey('batchSize') || prm.containsKey('batchNum') || prm.containsKey('batchNumber')) {
				
				int batchNum, batchSize
				
				if (!(prm.containsKey('batchSize') && (prm.containsKey('batchNum') || prm.containsKey('batchNumber')))) {
					throw new MoksalizerException("When creating batches, BOTH batchSize and batchNum must be specified.")	
				}
				
				batchSize = prm.batchSize
				batchNum = prm.containsKey('batchNum') ? prm.batchNum : prm.batchNumber
				
				assert batchSize instanceof Integer && batchSize > 0
				assert batchNum instanceof Integer && batchNum >= 0
				
				query.limit(batchSize)
				query.offset(batchNum * batchSize)
				
			}
			
			query.asList()
			
		}

			
	}
	
}
