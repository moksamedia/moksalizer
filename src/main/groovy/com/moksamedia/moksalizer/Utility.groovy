package com.moksamedia.moksalizer

import java.text.SimpleDateFormat


class Utility {
	
	static def findAllPropertiesForClassWithAnotation( obj, annotClass ) {
	  obj.properties.findAll { prop ->
	    obj.getClass().declaredFields.find { 
	      it.name == prop.key && annotClass in it.declaredAnnotations*.annotationType()
	    }
	  }
	}
	
	static final formattedDateString = "MMMMM dd, yyyy"
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(formattedDateString)
	static String formatDate(Date date) {
		dateFormatter.clone().format(date)
	}
	
}