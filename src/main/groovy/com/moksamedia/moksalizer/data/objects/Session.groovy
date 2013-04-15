package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Id

@Entity
class Session {
	
	@Id
	private ObjectId id;
	
	Date created
	Date expires

}
