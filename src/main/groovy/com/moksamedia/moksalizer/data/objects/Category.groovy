package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Id


@Entity
class Category {
			
	@Id ObjectId id;
	
	String name
	String slug
	
	int postCount
	
	/**
	 * Convenience method that calls the Post.addCategory() method, which increments the post count.
	 * @param post
	 * @return post count
	 */
	public int addPost(Post post) {
		post.addCategory(this)
	}

	/**
	 * Convenience method that calls the Post.removeCategory() method, which decrements the post count.
	 * @param post
	 * @return post count
	 */
	public int removePost(Post post) {
		post.removeCategory(this)
	}

	public int calculatePostCount() {
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