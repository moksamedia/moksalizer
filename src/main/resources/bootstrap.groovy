

import java.security.MessageDigest

import org.slf4j.LoggerFactory

import com.moksamedia.moksalizer.data.objects.BlogData
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Seq
import com.moksamedia.moksalizer.data.objects.Tag
import com.moksamedia.moksalizer.data.objects.User

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

	MessageDigest digest = MessageDigest.getInstance("MD5")
	String hashed = digest.digest('admin'.padLeft(32, 'X').bytes).encodeBase64().toString()

	admin = new User(
			lastName:"Hughes",
			firstName:"Andrew",
			screenName:"cantgetnosleep",
			email:"andrewcarterhughes@gmail.com",
			hashedPassword:hashed,
			verified:true) // verified should be skipped, defaults to false

	admin.save()

	new Seq().save()

	log.info "Creating BlogData"


}
else {
	admin = User.getOne(screenName:'cantgetnosleep')
}

BlogData.dropCollection()
BlogData blogData = new BlogData()
blogData.admin = admin
blogData.description = "Andrew's awesome blog."
blogData.name = "The path is..."
blogData.homeURL = (deployed ? 'http://www.moksamedia.com' : '')
blogData.save()


