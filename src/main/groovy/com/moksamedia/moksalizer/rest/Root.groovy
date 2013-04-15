package com.moksamedia.moksalizer.rest

import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.DefaultValue
import javax.ws.rs.FormParam
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.UsernamePasswordToken

import com.moksamedia.moksalizer.BlogratTemplater
import com.moksamedia.moksalizer.Controller
import com.moksamedia.moksalizer.PageContext
import com.moksamedia.moksalizer.data.WordpressImporterJson
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Tag
import com.moksamedia.moksalizer.security.MoksalizerRealm

@Slf4j
@Path("/")
public class Root {

	// Injects a thread-local proxy
	@Context HttpServletResponse response
	@Context HttpServletRequest request
	@Context UriInfo uri
	
	// Controller is a singleton, save a pointer for convenience
	public static final Controller controller = Controller.instance
	
	// This is final and immutable to avoid threading problems
	public static final Map blogCtx = controller.blogData.getImmutableContext()
	
	public static final Map cssCtx = ([
			contentWidth:800
		]).asImmutable()
		
	public Root() {
		log.info "Creating ROOT"
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// STANDARD CONTEXT
	
	/*
	 * Don't think renderer is thread-safe, so probably need to create a new
	 * instance for each request rendering. Worth caching these? There's no
	 * state in the BlogratTemplater class.
	 */
	Closure render = { String template, def context ->
		BlogratTemplater templater = new BlogratTemplater()
		if (context instanceof PageContext) context = context.ctx
		templater.render(template, context)
	}
	

	Closure stripLeadingSlash = { String str ->
		if (str == null || str == "") return str
		(str[0] == '/') ? str[1..-1] : str
	}
	
	@GET
	@Produces('text/css')
	@Path(/{filename: \S+\.gcss$}/) // *.gcss
	public String getRootStyle(@PathParam("filename") String filename) {
		render(filename, cssCtx)
	}
	
	@GET
	@Produces('text/html')
	public String getRoot(@QueryParam('batchnum') @DefaultValue('0') int batchNum, 
						  @QueryParam('batchsize') @DefaultValue('10') int batchSize) {
			
		log.info "batchNum = $batchNum, batchSize = $batchSize"
		
		def posts = Post.find(
				search		: [type:'post', publish:true],
				sort		: '-dateCreated',
				batchSize	: batchSize,
				batchNumber	: batchNum)						
							 				 
		def context = new PageContext([
			posts:posts, 
			body:'listajax', 
			windowTitle:PageContext.blogCtx.blogName,
			passToPage:[ajaxloadtype:'post']
			])
				
		render('shell.html', context)
	}
	
	@GET
	@Produces('text/html') 
	@Path('loadpostsajax')
	public String loadPostsAjax (@QueryParam('batchnum') @DefaultValue('0') int batchNum, 
						  @QueryParam('batchsize') @DefaultValue('10') int batchSize,
						  @QueryParam('ajaxloadtype') @DefaultValue('allposts') String ajaxloadtype) {
		
		def search = [type:"post", publish:true]
		
		// posts for a specific tag
		if (ajaxloadtype == 'tag') { // start at one bc '/tag/verse' is split to ['','tag','verse']
			assert parts.size() == 3
			Tag tag = Tag.getOne(slug:parts[2])
			assert tag != null
			search += ["tags":tag._id]
		}
		// posts for a category
		else if (ajaxloadtype == 'category') {
			assert parts.size() == 3
			Category category = Category.getOne(slug:parts[2])
			assert tag != null
			search += ["categories":tag._id]
		}

		// get a batch of posts specified by search params and sorted by date
		def posts = Post.find(
				search		: search,
				sort		: '-dateCreated',
				batchSize	: batchSize,
				batchNumber	: batchNum
			)
		
		
		// add the posts to the context var
		def context = new PageContext([
			posts:posts, 
			body:'listajax'
			])

		render('listajax.html', context)

	}

	/*
	 * Import a wordpress site via JSON REST API
	 */
	@GET
	@Produces('text/html')
	@Path('wpimport')
	public String wordpressImport() {
		
		if (!MoksalizerRealm.isAdmin(request)) {
			throw new WebApplicationException(HttpServletResponse.SC_FORBIDDEN)
		}

		def context = [windowTitle:"Import from Wordpress Site", body:'wpimport'] + getStandardContext(request)

		render('/templates/shell.html', context)

	}
	
	@GET
	@Produces('application/json')
	@Path('importgetjson')
	public String wordpressImportGetJson(@PathParam('pagesToImport') String pagesToImport) {

		if (!MoksalizerRealm.isAdmin(request)) {
			throw new WebApplicationException(HttpServletResponse.SC_FORBIDDEN)
		}

		log.info "URL:" + params?.url

		WordpressImporterJson importer = new WordpressImporterJson(controller.blogData.admin, params?.url, './static/images', '/images')

		String posts = importer.fetchAllPosts()
		importer.processPosts(posts)
		importer.saveTagsAndCategories()

		if (pagesToImport != null && pagesToImport != "") {
			def pages = importer.fetchPages(pagesToImport)
			importer.processPages(pages)
		}

		if (posts == "") {
			throw new WebApplicationException(HttpServletResponse.SC_BAD_REQUEST)
		}
		
		posts
	}

	@POST
	@Path('login')
	public String login(@FormParam('username') String username,
						@FormParam('password') String password) {
		log.info "$username, $password"
		UsernamePasswordToken token = new UsernamePasswordToken(username, password)
		token.setRememberMe(true)
		SecurityUtils.getSubject().login(token)						
	}

	@Path('logout')
	public String logout() {
		SecurityUtils.getSubject().logout()
		response.sendRedirect(uri.getBaseUri())
	}


						  
}