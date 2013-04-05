package com.moksamedia.moksalizer.data.objects

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Reference

@Entity
class Post {

	/*
	def hasTypes = [
		author: refer(User),
		comments: embed(Comment),
		categories: refer(Category),
		tags: refer(Tag)
	]
	*/
	
	String type = "post" // post or page
	
	@Reference 
	User author
	
	int sequenceNumber
	String nameComputerFriendly
	
	String title
	String html
	String summary
	
	Date dateCreated
	Date lastEdited
	Date datePublished
		
	boolean publish = false
	boolean allowComments = false
	
	String importId
	
	Map ext = [:]
		
	def comments = []
	
	def categories = []
	
	def tags = []
	
	public void onRemove() {
		tags.each {
			it.removeReference(this)
			it.save()
		}
		categories.each {
			it.removeReference(this)
			it.save()
		}
	}
	
	String datePublishedFormatted() {
		if (datePublished != null) datePublished.toString()
		else "Not published"
	}
	
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