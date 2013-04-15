package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Id
import com.github.jmkgreen.morphia.annotations.Reference

@Entity
class BlogData {
		
	@Id ObjectId id;
	
	String name = 'Blograt Blog'
	String description = "A blograt blog..."
	String homeURL = 'http://localhost:5000'
	
	@Reference
	User admin
		
	// values passed into servlet pages as they are rendered
	public def getImmutableContext() {
		
		def context = [
			blogName: name,
			blogDescription:description,
			blogHomeURL: homeURL,
			admin:admin
			]
				
		context.asImmutable()
	}
	
}
