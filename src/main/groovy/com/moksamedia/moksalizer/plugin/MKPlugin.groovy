package com.moksamedia.moksalizer.plugin

abstract class MKPlugin {

	String name = "Default Plugin Name"
	String description = "Default plugin description."
	
	public abstract String doPlugin();
}
