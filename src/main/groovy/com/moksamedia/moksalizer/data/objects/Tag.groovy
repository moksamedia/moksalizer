package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Id


@Entity
class Tag {
	
	@Id
	private ObjectId id;
	
	String name
	String slug
	
	int postCount
	
	/**
	 * Convenience method that calls the Post.addTag() method, which increments the post count.
	 * @param post
	 * @return
	 */
	public int addPost(Post post) {
		post.addTag(this)
	}

	/**
	 * Convenience method that calls the Post.removeTag() method, which decrements the post count.
	 * @param post
	 * @return
	 */
	public int removePost(Post post) {
		post.removeTag(this)
	}

	public int calculatePostCount() {
		def posts = Post.getAll(type:"post", publish:true, "tags":this._id)
		posts != null ? posts.size() : 0
	}

	public static int totalTagsCount() {
		int totalCount = 0
		Tag.getAll()?.each { totalCount += it.postCount }
		totalCount
	}
	
	String toString() {
		name
	}
	
	public boolean equals(Object obj) {
		(obj instanceof Tag) ? false : (obj.name == this.name)
	}
	
}