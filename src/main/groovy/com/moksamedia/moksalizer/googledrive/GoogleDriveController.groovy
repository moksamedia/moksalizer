package com.moksamedia.moksalizer.googledrive

import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET
import groovyx.net.http.HTTPBuilder

import javax.swing.JOptionPane

import org.slf4j.LoggerFactory

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow.Builder
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory

class GoogleDriveController {

	final log = LoggerFactory.getLogger(getClass().simpleName)

	Credential credential

	Closure oauth2callback
	
	def config

	/*
	String approvalPrompt = "auto"
	String accessType = "offline"
	String clientId = "459169345205.apps.googleusercontent.com"
	String clientSecret = "X1LCmLLHWhkIcQuuCb2Qdq1d"
	String redirectURL = "https://www.moksamedia.com/oauth2callback"
	List scopes = ['https://www.googleapis.com/auth/drive', 'https://www.googleapis.com/auth/userinfo.profile', 'https://www.googleapis.com/auth/userinfo.email']
	*/

	public GoogleDriveController(def config) {
		this.config = config

		log.info "approvalPrompt = " + config.approvalPrompt
		log.info "accessType = " + config.accessType
		log.info "clientId = " + config.clientId
		log.info "redirectURL = " + config.redirectURL
		log.info "scopes = " + config.scopes

		assert config != null


	}

	
	public Credential loadCredential(String userId) {

		log.info "Credential for $userId not found. Authorizing."

		HttpTransport httpTransport = new NetHttpTransport();

		Builder flowBuilder = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
				jsonFactory,
				config.clientId,
				config.clientSecret,
				config.scopes);

		flowBuilder.setAccessType(config.accessType)
		flowBuilder.setApprovalPrompt(config.approvalPrompt)
		flowBuilder.setCredentialStore(new MoksalizerCredentialStore())
		
		GoogleAuthorizationCodeFlow flow = flowBuilder.build();

		Credential credential = flow.loadCredential(userId)
		
		// Obtain the authorization URL

		if (credential != null) {
			log.info "Credential for $userId already stored. Returning."
		}
		else {
			
			log.info "Credential for $userId not found. Requesting new one."
			
			//credential = requestNewCredential_DIALOG(flow, userId)
			credential = requestNewCredential_BOUNCE(flow, userId)
			
		}

		assert credential != null

		log.info "Credential for $userId created and stored."
		log.info "Credential.accessToken=${credential.accessToken}"
		log.info "Credential.refreshToken=${credential.refreshToken}"
		log.info "Credential.expirationTimeMilliseconds=${credential.expirationTimeMilliseconds}, or ${(new Date(credential.expirationTimeMilliseconds)).toString()}"

		credential
		
	}
	
	public Credential requestNewCredential_BOUNCE(GoogleAuthorizationCodeFlow flow, String userId) {

		Credential cred = null
		
		String url = flow.newAuthorizationUrl().setRedirectUri(config.redirectURL);
		
		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

		if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
			log.error( "Desktop doesn't support the browse action (fatal)" );
			return
		}

		log.info "Browsing"
		
		URI uri = new URI(url)
		desktop.browse(uri)

		log.info "Done browsing"
		
		String code = ""

		def http 

		int i
		for (i=0;i<10;i++) {
			log.info "Sleeping..."
			sleep(1000)
		}		
		
		http = new HTTPBuilder('http://www.moksamedia.com')

		http.request('http://www.moksamedia.com', GET, TEXT) { req ->

			uri.path = '/bounce/code'

			log.info uri.toString()

			response.success = { resp, reader ->

				assert resp.status == 200
				code = reader.getText()
				log.info "Received code: $code"

			}

			response.'404' = { resp ->
				log.error "404 Not Found! Failed to set bounceback URL!"
			}


		}
				
		
		if (code != "NO CODE FOUND!") {

			log.info "code received: $code"

			TokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(config.redirectURL).execute()

			cred = flow.createAndStoreCredential(tokenResponse, userId);

		}
		else {
			log.info "No code found."
			
			cred =  null
		}
		
		cred
		
	}
	
	private void setBouncebackURL() {
		
		String bounceIP = "curl ifconfig.me".execute().text.trim()
		
		log.info "bounceIP set to ${bounceIP}:5000"
		
		def http = new HTTPBuilder('http://www.moksamedia.com')
				
		http.request('http://www.moksamedia.com', GET, TEXT) { req ->
			
			uri.path = '/bounce/to'
			uri.query = [url:"http://${bounceIP}:5000/oauth2callback"]
			
			log.info uri.toString()
			
			response.success = { resp, reader ->
				
				assert resp.status == 200
				log.info reader.getText()
				
			}
			
			response.'404' = { resp ->
				
				log.error "404 Not Found! Failed to set bounceback URL!"
							
			}

			
			
		}
		
	}
	
	public Credential requestNewCredential_DIALOG(GoogleAuthorizationCodeFlow flow, String userId) {
		
		
		String url = flow.newAuthorizationUrl().setRedirectUri(config.redirectURL);
		
		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

		if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
			log.error( "Desktop doesn't support the browse action (fatal)" );
			return
		}

		URI uri = new URI(url)
		desktop.browse(uri)

		String code = (String)JOptionPane.showInputDialog(null,"Please enter the code:");

		if (code == null) {
			log.info "Canceled."
			return null
		}

		log.info "Code from dialog: '$code'"

		TokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(config.redirectURL).execute()

		flow.createAndStoreCredential(tokenResponse, userId);

	}

}
