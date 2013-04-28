package com.moksamedia.moksalizer.plugin

import org.openide.util.lookup.ServiceProvider


@ServiceProvider(service=MKPluginAbstract)
class TestPlugin extends MKPluginAbstract {

	public TestPlugin() {
		name = "Test Plugin"
		description = "Your basic proof-of-concept development plugin."
	}
	
	public String doPlugin() {
		
	}
}
