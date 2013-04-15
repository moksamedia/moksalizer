package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Id
import com.github.jmkgreen.morphia.annotations.Reference


@Entity
class Post {
	
	@Id
	private ObjectId id;
	
	String type = "post" // post or page
	
	@Reference 
	User author
	
	int sequenceNumber
	String slug
	
	String title
	String html
	String summary
	
	Date dateCreated
	Date lastEdited
	Date datePublished
		
	boolean publish = false
	boolean allowComments = false
	
	String importId
			
	List<Comment> comments = []
	
	Set<Category> categories = []
	
	Set<Tag> tags = []
	
	/**
	 * Add a single tag to the post. Increments the post count for the tag.
	 * @param tag
	 * @return post count for tag
	 */
	public int addTag(Tag tag) {
		tags.add(tag)
		tag.postCount += 1
	}
	
	/**
	 * Add a single category to the post. Increments the post count for the category.
	 * @param category
	 * @return post count for category
	 */
	public int addCategory(Category category) {
		categories.add(category)
		category.postCount += 1
	}

	/**
	 * Removes a single tag from the post. Decrements the post count.
	 * @param tag
	 * @return post count for tag
	 */
	public int removeTag(Tag tag) {
		tags.remove(tag)
		tag.postCount -= 1
	}
	
	/**
	 * Removes a single category from the post. Decrements the post count.
	 * @param category
	 * @return post count for category
	 */
	public int removeCategory(Category category) {
		categories.remove(category)
		category.postCount -= 1
	}
	
	/**
	 * Adds a list of tags to the post. Increments the post count for each tag.
	 * @param tags
	 */
	public void addTags(def tags) {
		tags.each { addTag(it) }
	}
	
	/**
	 * Adds a list of categories to the post. Increments the post count for each category.
	 * @param categories
	 */
	public void addCategories(def categories) {
		categories.each { addCategory(it) }
	}

	/**
	 * Removes a list of tags from the post. Decrements the post count for each tag.
	 * @param tags
	 */
	
	public void removeTags(def tags) {
		tags.each { removeTag(it) }
	}
	
	/**
	 * Removes a list of categories from the post. Decrements the post count for each category.
	 * @param categories
	 */
	public void removeCategories(def categories) {
		categories.each { removeCategory(it) }
	}
	
	/**
	 * Returns a properly formatted date string
	 * @return date string formatted
	 */
	String datePublishedFormatted() {
		if (datePublished != null) datePublished.toString()
		else "Not published"
	}
	
	/**
	 * Returns a string representation of the post
	 */
	String toString() {
		
		String result = ""
		
		result += "'${title}' by ${author?.screenName} (${author?.firstName}, ${author?.lastName})\n"
		result += "Date created: " + dateCreated?.toString() + "\n"
		result += "Date last edited: " + lastEdited?.toString() + "\n"
		result += "Date published: " + datePublished?.toString() + "\n"
		result += "Tags: " + tags.inject("") { acc, val -> acc += val.toString();acc }  + "\n"
		result += "Categories: " + categories.inject("") { acc, val -> acc += val.toString();acc } + "\n"
		result += "Comments: " + comments.inject("") { acc, val -> acc += val.toString();acc }
		result += "CONTENT\n" + html
		
	}

}