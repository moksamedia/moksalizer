package com.moksamedia.moksalizer.data.objects

import org.bson.types.ObjectId

import com.github.jmkgreen.morphia.annotations.Entity
import com.github.jmkgreen.morphia.annotations.Id


@Entity
class GoogleCredentialDataObject {

	@Id
	private ObjectId id;
	
	String userId
	String googleId
	String googleEmail
	
	String accessToken
	String refreshToken
	Long expirationTimeMilliseconds 
	
}
