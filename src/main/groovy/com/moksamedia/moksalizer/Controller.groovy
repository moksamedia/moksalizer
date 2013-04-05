package com.moksamedia.moksalizer

import groovy.util.logging.Slf4j

import com.moksamedia.moksalizer.data.MongoMorphiaDAO
import com.moksamedia.moksalizer.data.objects.BlogData
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Seq
import com.moksamedia.moksalizer.data.objects.Tag
import com.moksamedia.moksalizer.googledrive.GoogleDriveController


@Slf4j
@Singleton
class Controller {

	final static String RESET_ON_LOAD = 'MOKSALIZER_RESET_ON_LOAD'
	final static String DEPLOYED = 'MOKSALIZER_DEPLOYED'
	
	MongoMorphiaDAO daoMongo
	GoogleDriveController googleDriveController

	BlogData blogData
	
	public boolean isTest = false

	def config

	public boolean isDeployed = false
	public boolean resetOnLoad = false
	
	public Controller() {

		// is deployed?
		if (System.getenv(DEPLOYED) == true || "curl ifconfig.me".execute().text.trim() == '50.116.24.233') isDeployed = true
		
		// reset db on load?
		if (System.getenv(RESET_ON_LOAD) == true) resetOnLoad = true
				
		URL urlToConfig  = Controller.classLoader.getResource('config.groovy')

		assert urlToConfig != null

		config = new ConfigSlurper().parse(urlToConfig)

		log.info "CONFIG=" + config.toString()

		googleDriveController = new GoogleDriveController(config.googledrive)

	}

	
	// Creates the dao controller, loads the bootstrap, and sets the blog data object. 
	public init(def params = [:]) {

		String dataObjectsPackage
		String databaseName
		String databaseHost
		int databasePort
		
		if (params?.test == true) {
			dataObjectsPackage = config.datastore.test.dataObjectsPackage
			databaseName = config.datastore.test.databaseName
			databaseHost = config.datastore.test.databaseHost
			databasePort = config.datastore.test?.databasePort // optional
		}
		else {
			dataObjectsPackage = config.datastore.prod.dataObjectsPackage
			databaseName = config.datastore.prod.databaseName
			databaseHost = config.datastore.prod.databaseHost
			databasePort = config.datastore.prod?.databasePort // optional
		}
		
		daoMongo = new MongoMorphiaDAO(dataObjectsPackage:dataObjectsPackage,
									   databaseName:databaseName,
									   databaseHost:databaseHost,
									   databasePort:databasePort)
		
		loadBootstrap()

		blogData = BlogData.getOne()
		assert blogData != null
		
	}
	
	public void loadBootstrap() {

		log.info "Loading Bootstrap!"

		def engine = new GroovyScriptEngine(new ClassLoaderResourceConnector(this.class))

		// Create a binding of any variables we want to pass to the script
		def binding = [controller:this, reset:resetOnLoad, deployed:isDeployed, test:isTest] as Binding

		// Run the script
		def obj = engine.run('/scripts/bootstrap.groovy', binding)

	}
	
	public void dropAllPosts() {
		Post.dropCollection()
		Tag.dropCollection()
		Category.dropCollection()
		Seq.dropCollection()
		new Seq().save()
	}
	
	public void oauth2callback(def params) {
		googleDriveController.oauth2callback(params)
	}

}