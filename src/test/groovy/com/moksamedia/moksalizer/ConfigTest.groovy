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

}
