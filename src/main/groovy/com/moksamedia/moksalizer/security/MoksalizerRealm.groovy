package com.moksamedia.moksalizer.security

import groovy.util.logging.Slf4j

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.crypto.RandomNumberGenerator
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha256Hash
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.util.ByteSource
import org.apache.shiro.util.SimpleByteSource;

import com.moksamedia.moksalizer.data.objects.User


/**
 * This is used to provide a hook for Shiro to store credentials in the Moksalizer mongodb
 * database. doGetAuthorizationInfo() looks up role information (as well as some other
 * options--see the api). doGetAuthenticationInfo() looks up the hashed password and
 * password salt. I also added some non-override utility functions and properties, making
 * this class more of a utility authentication and authorization class.
 * @author cantgetnosleep
 *
 */
@Slf4j
class MoksalizerRealm extends AuthorizingRealm {

	public static final int HASH_ITERATIONS = 1024
	public static final String HASH_NAME = Sha256Hash.ALGORITHM_NAME
	public static final String REALM_NAME = 'moksalizer-login'
		
	/**
	 * Given a password, returns a map with the salt and hashed password
	 * as byte arrays.
	 * @param password to be hashed with salt
	 * @return
	 */
	public static def saltAndHashForPassword(String password) {
		RandomNumberGenerator rng = new SecureRandomNumberGenerator()
		ByteSource salt = rng.nextBytes()
		def hash = new Sha256Hash(password, salt, HASH_ITERATIONS)
		[salt:salt.getBytes(), hash:hash.getBytes()]
	}

	/**
	 * Is the current user an admin?
	 */
	public static Closure isAdmin = { request ->
		SecurityUtils.getSubject().hasRole('admin')
	}
	
	/**
	 * Is the current user authenticated/logged in?
	 */
	public static Closure isAuthenticated = { request ->
		SecurityUtils.getSubject().isAuthenticated()
	}
	
	/**
	 * Is the current user remembered (NOT the same as authenticated)?
	 */
	public static Closure isRemembered = { request ->
		SecurityUtils.getSubject().isRemembered()
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection princinalsCollection) {
		
		String username = princinalsCollection.primaryPrincipal
		
		User user = User.getOne(username:username)
		
		if (user == null) {
			throw new AuthorizationException("Username '$username' not found!")
		}
		
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo()
		
		info.roles = user.roles
		
		info

	}

	/*
	 * I am storing both the hashed password and the password salt as a byte array, so no
	 * base64 encoding or decoding is necessary. NOTE: previously, when I was base64 encoding
	 * the password, it was being decoded automatically BUT the salt WAS NOT, and this
	 * caused authentication to fail. 
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		
		String username = token.principal
		
		log.info "Authenticating user: $username"
		
		User user = User.getOne(username:username)
				
		if (user == null) {
			throw new AuthenticationException("Username '$username' not found!")
		}
		
		if (user.passwordSalt == null || user.hashedPassword == null) {
			throw new AuthenticationException("Username '$username' has invalid credentials!")
		}
				
		def info = new SimpleAuthenticationInfo(username, user.hashedPassword, REALM_NAME)
		info.setCredentialsSalt(new SimpleByteSource(user.passwordSalt))
		
		info
		
	}

}
