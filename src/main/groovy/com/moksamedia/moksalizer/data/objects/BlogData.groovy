package com.moksamedia.moksalizer.data.objects

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Reference

@Entity
class BlogData {
	
	String name = 'Blograt Blog'
	String description = "A blograt blog..."
	String homeURL = 'http://localhost:5000'
	
	@Reference
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
	
}
