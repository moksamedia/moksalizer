package com.moksamedia.moksalizer.dataobjects


class Category {
					
	String name
	String nameComputerFriendly
		
	public int postCount() {
		def posts = Post.getAll(type:"post", publish:true, "categories":this._id)
		posts != null ? posts.size() : 0
	}
	
	public static int totalTagsCount() {
		int totalCount = 0
		Tag.getAll()?.each { totalCount += it.postCount() }
		totalCount
	}
	
	String toString() {
		name
	}

}