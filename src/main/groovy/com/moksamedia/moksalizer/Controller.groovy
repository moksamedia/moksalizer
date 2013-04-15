package com.moksamedia.moksalizer

import groovy.util.logging.Slf4j

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.config.IniSecurityManagerFactory
import org.apache.shiro.mgt.DefaultSecurityManager

import com.moksamedia.moksalizer.data.MongoMorphiaDAO
import com.moksamedia.moksalizer.data.objects.BlogData
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Seq
import com.moksamedia.moksalizer.data.objects.Tag
import com.moksamedia.moksalizer.exception.MoksalizerException
import com.moksamedia.moksalizer.googledrive.GoogleDriveController
import com.moksamedia.moksalizer.security.MoksalizerRealm


@Slf4j
@Singleton
class Controller {

	final static String BLOGDATA_KEY = 'blogdata'
	
	final static String RESET_ON_LOAD = 'MOKSALIZER_RESET_ON_LOAD'
	final static String DEPLOYED = 'MOKSALIZER_DEPLOYED'
	
	MongoMorphiaDAO daoMongo
	GoogleDriveController googleDriveController

	BlogData blogData
	
	public boolean isTest = false

	def config

	public boolean isDeployed = false
	public boolean resetOnLoad = true
	
	public Controller() {
				
		URL urlToConfig  = Controller.classLoader.getResource('config.groovy')

		assert urlToConfig != null

		config = new ConfigSlurper().parse(urlToConfig)

		verifyConfig()
		
		log.info "CONFIG=" + config.toString()
		
		setIsDeployed()
		
		setResetOnLoad()		
		
		//configureSecurity()
		
		//googleDriveController = new GoogleDriveController(config.googledrive)

	}
	
	private void configureSecurity() {
		
		log.info "Configuring Apache Shiro Security"
		
		MoksalizerRealm realm = new MoksalizerRealm()
		
		HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher()
		credentialsMatcher.hashAlgorithm = MoksalizerRealm.HASH_NAME
		credentialsMatcher.storedCredentialsHexEncoded = false
		credentialsMatcher.hashIterations = MoksalizerRealm.HASH_ITERATIONS
		realm.credentialsMatcher = credentialsMatcher
		
		DefaultSecurityManager securityManager = new DefaultSecurityManager(realm)
		SecurityUtils.setSecurityManager(securityManager)
	}
	
	private void configureSecurityIni() {
		def factory = new IniSecurityManagerFactory("classpath:shiro.ini");
		def securityManager = factory.getInstance();
		SecurityUtils.setSecurityManager(securityManager);
	}
	
	public void setIsDeployed() {
		
		String serverip = (config?.serverip == null) ? "No ip address" : config.serverip
		
		// is deployed?
		if (System.getenv(DEPLOYED) == true || "curl ifconfig.me".execute().text.trim() == serverip)
		{
			log.info "isDeployed = TRUE"
			isDeployed = true
		}
		else {
			log.info "isDeployed = FALSE"
		}

	}
	
	public void setResetOnLoad() {
		
		// reset db on load?
		if (System.getenv(RESET_ON_LOAD) == true) {
			log.info "resetOnLoad = TRUE"
			resetOnLoad = true
		}
		else {
			log.info "resetOnLoad = FALSE"
		}

	}

	public static void onServerStart() {
		Controller controller = Controller.instance
		controller.init()
	}
	
	public static void onServerStop() {
	
	}
	
	// Creates the dao controller, loads the bootstrap, and sets the blog data object. 
	public init(def params = [:]) {
		
		String dataObjectsPackage
		String databaseName
		String databaseHost
		int databasePort
				
		ConfigObject envConfig = (params?.test == true) ? config.datastore.test : config.datastore.prod
		
		dataObjectsPackage = envConfig.dataObjectsPackage
		databaseName = envConfig.databaseName
		databaseHost = envConfig.containsKey('databaseHost') ? envConfig.databaseHost : null
		databasePort = envConfig.containsKey('databasePort') ? envConfig.databasePort : null

		
		log.info "Creating DAO"
		daoMongo = new MongoMorphiaDAO(dataObjectsPackage:dataObjectsPackage,
									   databaseName:databaseName,
									   databaseHost:databaseHost,
									   databasePort:databasePort)
		
		log.info "Loading Bootstrap"
		loadBootstrap()

		log.info "Loading BlogData object"
		blogData = BlogData.getOne()
		assert blogData != null
		
	}
	
	public void verifyConfig() {
		
		// datastore
		if (!config.containsKey('datastore')) { throw new MoksalizerException('Config does not define \'datastore\' parameters.') }
		
		// prod & test
		if (!config.datastore.containsKey('prod')) { throw new MoksalizerException('Config does not define \'datastore.prod\' parameters.') }
		if (!config.datastore.containsKey('test')) { throw new MoksalizerException('Config does not define \'datastore.test\' parameters.') }
		
		// prod
		if (!config.datastore.prod.containsKey('dataObjectsPackage')) { throw new MoksalizerException('Config does not define prod.dataObjectsPackage.') }
		if (!config.datastore.prod.containsKey('databaseName')) { throw new MoksalizerException('Config does not define prod.databaseName.') }
		
		if (config.datastore.prod.containsKey('databasePort')) { 
			if (!config.datastore.prod.containsKey('databaseHost')) { throw new MoksalizerException('Config does not defines prod.databasePort but not prod.databaseHost.') }
		}
		
		// test
		if (!config.datastore.test.containsKey('dataObjectsPackage')) { throw new MoksalizerException('Config does not define test.dataObjectsPackage.') }
		if (!config.datastore.test.containsKey('databaseName')) { throw new MoksalizerException('Config does not define test.databaseName.') }
		
		if (config.datastore.test.containsKey('databasePort')) { 
			if (!config.datastore.test.containsKey('databaseHost')) { throw new MoksalizerException('Config does not defines test.databasePort but not test.databaseHost.') }
		}

		
	}
	
	public void loadBootstrap() {

		def engine = new GroovyScriptEngine(new ClassLoaderResourceConnector(this.class))

		// Create a binding of any variables we want to pass to the script
		def binding = [controller:this, reset:resetOnLoad, deployed:isDeployed, test:isTest] as Binding

		// Run the script
		def obj = engine.run('/bootstrap.groovy', binding)

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