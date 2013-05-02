package com.moksamedia.moksalizer.plugin

abstract class MKPluginAbstract {

	String name = "Default Plugin Name"
	String description = "Default plugin description."
	
	
	public abstract String doPlugin();
	
	public abstract initialize(def params = [:]);
}
