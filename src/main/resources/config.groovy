import java.util.List;

datastore {
	
	prod {
	
		databaseHost = 'localhost'
		databasePort = 27017
		dataObjectsPackage = 'com.moksamedia.moksalizer.data.objects'
		databaseName = 'moksalizer'

	}
	test {
		
		databaseHost = 'localhost'
		databasePort = 27017
		dataObjectsPackage = 'com.moksamedia.moksalizer.data.objects'
		databaseName = 'moksalizer_test'

	}
	
}

serverip = '50.116.24.233'

googledrive {

	/*
	 * After the initial refresh token has been issued, to get a new refresh token, approvalPrompt must 
	 * be set to 'force'. AcessType must be set to 'offline' if the program needs to be able to access 
	 * the google drive while the user is offline.
	 */
	approvalPrompt = 'force'
	accessType = 'offline'
	clientId = '459169345205.apps.googleusercontent.com'
	clientSecret = 'X1LCmLLHWhkIcQuuCb2Qdq1d'
	redirectURL = 'http://www.moksamedia.com/bounce/oauth2callback'
	scopes = ['https://www.googleapis.com/auth/drive', 'https://www.googleapis.com/auth/userinfo.profile', 'https://www.googleapis.com/auth/userinfo.email']

}

bloginfo {
	
	name = 'moksalizer'
	description = 'a blograt blog - awesome!'
	
	admin {
		fullName = 'Andrew Hughes'
		alias = 'cantgetnosleep'
		email = 'andrewcarterhughes@gmail.com'
	}
	
}