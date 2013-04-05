package com.moksamedia.moksalizer.data.objects

@Entity
class Comment {
	
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