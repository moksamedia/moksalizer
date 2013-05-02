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

	@Override
	public Object initialize(Object params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object initialize() {
		// TODO Auto-generated method stub
		return null;
	}


}
