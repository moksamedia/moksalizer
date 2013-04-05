package com.moksamedia.moksalizer.googledrive

import org.slf4j.LoggerFactory

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.CredentialStore
import com.moksamedia.moksalizer.data.objects.GoogleCredentialDataObject
import com.moksamedia.moksalizer.exception.MoksalizerException


class MoksalizerCredentialStore implements CredentialStore {

	final log = LoggerFactory.getLogger(getClass().simpleName)

	/**
	 * Loads the credential for the given user ID.
	 *
	 * <p>
	 * Upgrade warning: since version 1.10 this method throws an {@link IOException}. This was not
	 * done prior to 1.10.
	 * </p>
	 *
	 * @param userId user ID whose credential needs to be loaded
	 * @param credential credential whose {@link Credential#setAccessToken access token},
	 *        {@link Credential#setRefreshToken refresh token}, and
	 *        {@link Credential#setExpirationTimeMilliseconds expiration time} need to be set if the
	 *        credential already exists in storage
	 * @return {@code true} if the credential has been successfully found and loaded or {@code false}
	 *         otherwise
	 */
	boolean load(String userId, Credential credential) throws IOException {

		def results = GoogleCredentialDataObject.getAll(['userId':userId])

		if (results.size() == 0) {
			log.info "Failed to load credential for userId: ${userId}"
			false
		}
		else if (results.size() == 1) {
			GoogleCredentialDataObject obj = results[0]
			credential.accessToken = obj.accessToken
			credential.refreshToken = obj.refreshToken
			credential.expirationTimeMilliseconds = obj.expirationTimeMilliseconds
			log.info "Loaded credential for userId: ${userId}"
			true
		}
		else if (results.size() > 1) {
			throw new MoksalizerException('More than one userId/userName found for:' + userId)
		}

	}

	/**
	 * Stores the credential of the given user ID.
	 *
	 * <p>
	 * Upgrade warning: since version 1.10 this method throws an {@link IOException}. This was not
	 * done prior to 1.10.
	 * </p>
	 *
	 * @param userId user ID whose credential needs to be stored
	 * @param credential credential whose {@link Credential#getAccessToken access token},
	 *        {@link Credential#getRefreshToken refresh token}, and
	 *        {@link Credential#getExpirationTimeMilliseconds expiration time} need to be stored
	 */
	void store(String userId, Credential credential) throws IOException {

		def results = GoogleCredentialDataObject.getAll(['screenName':userId])
		GoogleCredentialDataObject obj

		Closure storeCred = {
			obj.accessToken = credential.accessToken
			obj.refreshToken = credential.refreshToken
			obj.expirationTimeMilliseconds = credential.expirationTimeMilliseconds
			obj.userId = userId
			obj.save()
		}

		if (results.size() == 0) {
			obj = new GoogleCredentialDataObject()
			storeCred()
			log.info "Saving new credential for userId: ${userId}"
		}
		else if (results.size() == 1) {
			obj = results[0]
			storeCred()
			log.info "Updated credential for userId: ${userId}"
		}
		else if (results.size() > 1) {
			throw new MoksalizerException('More than one userId/userName found for:' + userId)
		}



	}

	/**
	 * Deletes the credential of the given user ID.
	 *
	 * <p>
	 * Upgrade warning: since version 1.10 this method throws an {@link IOException}. This was not
	 * done prior to 1.10.
	 * </p>
	 *
	 * @param userId user ID whose credential needs to be deleted
	 * @param credential credential to be deleted
	 */
	void delete(String userId, Credential credential) throws IOException {
		int numRemoved = GoogleCredentialDataObject.remove(['userId':userId])
		if (numRemoved == 1) {
			log.info "Removed credential for userId: ${userId}"
		}
		else if (numRemoved == 0){
			log.info "Trying to remove credential for userId: ${userId}, but none found."
		}
		else {
			throw new MoksalizerException('More than one userId/userName found for:' + userId)
		}
	}

}
