package com.moksamedia.moksalizer.dataobjects


class User {
	
	/*
	def hasTypes = [
		roles: refer(Role)
	]
	*/
	
	String lastName
	String firstName
	
	String screenName
	
	String email
	
	String hashedPassword
	
	boolean verified = false
	
	def roles = []
	
	String toString() {
		"$screenName : $firstName $lastName"
	}
	
}