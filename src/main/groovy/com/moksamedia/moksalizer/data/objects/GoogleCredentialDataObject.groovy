package com.moksamedia.moksalizer.data.objects

import com.github.jmkgreen.morphia.annotations.Entity


@Entity
class GoogleCredentialDataObject {

	String userId
	String googleId
	String googleEmail
	
	String accessToken
	String refreshToken
	Long expirationTimeMilliseconds 
	
}
