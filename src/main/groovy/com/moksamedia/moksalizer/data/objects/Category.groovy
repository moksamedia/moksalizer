package com.moksamedia.moksalizer.data.objects

import com.github.jmkgreen.morphia.annotations.Entity

@Entity
class Category {
					
	String name
	String nameComputerFriendly
		
	public int postCount() {
		Post.getCount(type:"post", publish:true, "categories":this._id)
	}
	
	public static int totalCategoryCount() {
		int totalCount = 0
		// TODO: this is horribly inefficient and should be cached or stored separately or something
		Category.getAll()?.each { totalCount += it.postCount() }
		totalCount
	}
	
	String toString() {
		name
	}

}