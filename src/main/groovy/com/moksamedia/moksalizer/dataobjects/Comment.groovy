package com.moksamedia.moksalizer.dataobjects

class Comment {
	
	/*
	def hasTypes = [
			author: referLazy(User)
		]
	*/
	
	String authorName
	String authorEmail
	String authorURL
	
	Date date
	
	int number
	
	String html
	
	String toString() {
		"${authorName}: $html"
	}
	
	String dateFormatted() {
		if (date != null) Utility.formatDate(date)
		else "No Date"
	}


}