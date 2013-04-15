package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Id


@Entity
class Seq {

	@Id
	private ObjectId id;
	
	int post = 0
	
	public int _nextPostNumber() {
		post +=1 
		this.save()
		(post - 1)
	}
	
	public static int nextPostNumber() {
		Seq.getOne()._nextPostNumber()
	}
	
}
