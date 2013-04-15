package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Id
import com.moksamedia.moksalizer.security.MoksalizerRealm


@Entity
class User {
	
	@Id
	private ObjectId id;
	
	String lastName
	String firstName
	
	String username
	
	String email
	
	byte[] hashedPassword
	byte[] passwordSalt
	
	boolean verified = false
	
	Set<String> roles = []
	
	String toString() {
		"$username : $firstName $lastName"
	}
	
	public void setPassword(String password) {
		def val = MoksalizerRealm.saltAndHashForPassword(password)
		hashedPassword = val.hash
		passwordSalt = val.salt
	}
	
}