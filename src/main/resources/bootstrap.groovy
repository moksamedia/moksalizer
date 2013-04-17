

import org.slf4j.LoggerFactory

import com.moksamedia.moksalizer.data.objects.BlogData
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Seq
import com.moksamedia.moksalizer.data.objects.Tag
import com.moksamedia.moksalizer.data.objects.User
import com.moksamedia.moksalizer.data.objects.Category
import com.moksamedia.moksalizer.security.MoksalizerRealm

final log = LoggerFactory.getLogger(getClass().simpleName)

// bindings: test, deployed, controller, and reset

User admin

if (reset) {

	Post.dropCollection()
	Tag.dropCollection()
	Category.dropCollection()
	Seq.dropCollection()
	User.dropCollection()

	log.info "Creating admin user"
			
	admin = new User(
			lastName:"Hughes",
			firstName:"Andrew",
			username:"cantgetnosleep",
			email:"andrewcarterhughes@gmail.com",
			roles:['admin'],
			verified:true) // verified should be skipped, defaults to false

	admin.setPassword('admin')
	
	admin.save()

	new Seq().save()

	log.info "Creating BlogData"


}
else {
	admin = User.getOne(username:'cantgetnosleep')
}

BlogData.dropCollection()
BlogData blogData = new BlogData()
blogData.admin = admin
blogData.description = "Andrew's awesome blog."
blogData.name = "The path is..."

if (deployed) {
	blogData.homeUrl = 'http://www.moksamedia.com'
	blogData.homeUrlSsl = 'https://www.moksamedia.com'
	
}
else {
	blogData.homeUrl = 'http://localhost:8080'
	blogData.homeUrlSsl = 'https://localhost:8443'
	
}

blogData.save()


