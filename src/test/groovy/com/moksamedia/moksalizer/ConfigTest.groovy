package com.moksamedia.moksalizer

import groovy.util.logging.Slf4j

import org.junit.BeforeClass
import org.junit.Test

import com.github.jmkgreen.morphia.Morphia
import com.moksamedia.moksalizer.data.MongoMorphiaDAO
import com.moksamedia.moksalizer.data.objects.Role
import com.moksamedia.moksalizer.data.objects.User


@Slf4j
class ConfigTest {
	
	static Controller controller
	
	@BeforeClass
	public static void setupController() {
		
		Role.metaClass.someprop = [] as List<User>
		
		controller = Controller.instance
		controller.init()
	}
	
	@Test
	public void testConfig() {
		assert controller.isTest
	}
	
	@Test
	public void testInit() {
		assert controller.daoMongo.ds.getDB().getName() == 'moksalizer_test'
	}
	
	@Test
	public void testMetaClass() {
		
		MongoMorphiaDAO daoMongo = controller.daoMongo
		Morphia morphia = daoMongo.morphia
		
		def props = Role.metaClass.properties
		
		props.each { 
			log.info it.name
		}
		
		User admin = User.getOne(username:'cantgetnosleep')
		assert admin != null
		
		Role role = new Role()
		role.someprop = [admin]
		
		def dbObj = morphia.toDBObject(role.someprop as List<User>)
		
		log.info dbObj.toMap().toMapString()
	
		
		role.save()
	}

}
