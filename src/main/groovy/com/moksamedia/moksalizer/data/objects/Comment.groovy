package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Id
import com.github.jmkgreen.morphia.annotations.Embedded
import com.moksamedia.moksalizer.Utility

@Embedded
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