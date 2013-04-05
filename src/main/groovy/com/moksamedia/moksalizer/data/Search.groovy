package com.moksamedia.moksalizer.data

import com.github.jmkgreen.morphia.query.Query

class Search {

	Class clazz
	Query query

	public Search(Class clazz, Query query) {
		this.clazz = clazz
		this.query = query
	}
	
	public void batch(int batchNumber, int batchSize) {
		query.limit(batchSize).offset(batchNumber * batchSize)
	}
	
	def methodMissing(String name, args) {
		/*
		 *  Ask the data object "clazz" if it wants to handle this method call. This
		 *  allows the data object class to take the get and remove methods. Could possibly
		 *  use respondsTo here, but that opens us up to accidentally calling incorrect
		 *  methods on the data object class; i.e., if for example, User implemented a
		 *  filter() method for some reason.
		 */
		if (clazz._shouldTakeFromSearch()) {
			clazz.invokeMethod(name, [query] + args)
		}
		// If now, we pass it to the query
		else {
			query.invokeMethod(name, args)
		}
	}

}
