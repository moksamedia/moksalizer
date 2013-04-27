package com.moksamedia.moksalizer

import java.text.SimpleDateFormat


/**
 * A Utility class to hold some static functions that might be useful in various
 * places and do not belong any particular place.
 * @author cantgetnosleep
 *
 */
class Utility {
	
	/**
	 * Finds all fields/properties of an 'obj' that are annotated with 
	 * a given annotation class.
	 * @param obj the object who's properties should be searched
	 * @param annotClass the annotation class to look for
	 * @return map of properties with matching annotation
	 */
	static def findAllPropertiesForClassWithAnotation( obj, annotClass ) {
	  obj.properties.findAll { prop ->
	    obj.getClass().declaredFields.find { 
	      it.name == prop.key && annotClass in it.declaredAnnotations*.annotationType()
	    }
	  }
	}
	

	static final formattedDateString = "MMMMM dd, yyyy"
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(formattedDateString)
	/**
	 * Formats a date by cloning to: MMMM dd, yyyy
	 * @param date
	 * @return
	 */
	static String formatDate(Date date) {
		dateFormatter.clone().format(date)
	}
	
	/**
	 * Formats plain bytes to more human-readable format.
	 * @param bytes number of bytes
	 * @param si base of 1000 or 1024
	 * @return
	 */
    static String formatBytes(long bytes, boolean si) {
		int unit = si ? 1000 : 1024
		if (bytes < unit) return bytes + " B"
		int exp = (int) (Math.log(bytes) / Math.log(unit))
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1)
		if (si) pre + 'i'
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre)
	}
	
}