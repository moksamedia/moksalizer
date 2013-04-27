package com.moksamedia.moksalizer.plugin

import org.openide.util.lookup.ServiceProvider

@ServiceProvider(service=MKPlugin)
public class TestPlugin1 extends MKPlugin {
	
	public TestPlugin1() {
		name = "TestPlugin1"
		description = "The first test plugin."
	}
	
	@Override
	public String doPlugin() {
		"Doing TestPlugin1"
	}

}
