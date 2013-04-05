package com.moksamedia.moksalizer.dataobjects

class BlogData {
	
	String name = 'Blograt Blog'
	String description = "A blograt blog..."
	String homeURL = 'http://localhost:5000'
	
	// reference
	User admin
	
	// expandable, custom map of values
	public Map ext = [:]
	
	// values passed into servlet pages as they are rendered
	public def getContext() {
		
		def context = [
			blogName: name,
			blogDescription:description,
			blogHomeURL: homeURL,
			admin:admin
			]
		
		context += ext
		
		context
	}
	
	/*
	static final List primitiveTypes = [String, int, Integer, boolean, Boolean, double, Long, long, Double, float, Float, Date]
	
	def getProperty(String name) { 
		if (this.hasProperty(name) || name =='class' || name == 'metaClass') {
			this.@"$name"
		}
		else {
			ext[name]
		} 
	}
	
	void setProperty(String name, value) { 
		if (this.hasProperty(name) || name =='class' || name == 'metaClass') {
			this.@"$name" = value
		}
		else {
			if (primitiveTypes.find(value.getClass())) {
				ext[name] = value
			}
			else {
				throw new BlogratException("Can only store primitive types and Dates in BlogData.")
			}
		} 
	}
	*/
	
}
