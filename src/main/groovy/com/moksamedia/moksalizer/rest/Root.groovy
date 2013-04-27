package com.moksamedia.moksalizer.rest

import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo

import org.apache.shiro.SecurityUtils

import com.moksamedia.moksalizer.MoksalizerTemplater
import com.moksamedia.moksalizer.Controller
import com.moksamedia.moksalizer.PageContext
import com.moksamedia.moksalizer.ShiroFilter
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
	
	// these are READ ONLY bc of thread safety issues
	final Map blogData = Controller.instance.blogData.getContext()
	final ConfigObject config = Controller.instance.config
		
	final Map cssCtx = ([
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
		MoksalizerTemplater templater = new MoksalizerTemplater()
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
	
	@ShiroFilter('authc,ssl[8443]')
	@GET
	@Produces('text/html')
	public String getRoot(@QueryParam('batchnum') @DefaultValue('0') int batchNum, 
						  @QueryParam('batchsize') @DefaultValue('10') int batchSize) {
					
		def posts = Post.find(
			search		: [type:'post', publish:true],
			sort		: '-dateCreated',
			batchSize	: batchSize,
			batchNumber	: batchNum)						
							 				 
		def context = new PageContext([
			posts:posts, 
			body:'listajax', 
			windowTitle:blogData['blogName'],
			passToPage:[ajaxloadtype:'post']
			])
				
		render('shell.html', context)
	}
	
	/**
	 * An ajax method used to dynamically load more posts.
	 * 
	 * @param batchNum
	 * @param batchSize
	 * @param ajaxloadtype post, tag, or category (posts = front page, all posts)
	 * @param id identifier of the tag or category for the list of posts
	 * @return
	 */
	@GET
	@Produces('text/html') 
	@Path('loadpostsajax')
	public String loadPostsAjax (@QueryParam('batchnum') @DefaultValue('0') int batchNum, 
						  		 @QueryParam('batchsize') @DefaultValue('10') int batchSize,
								 @QueryParam('ajaxloadtype') @DefaultValue('') String ajaxloadtype,
								 @QueryParam('id') @DefaultValue('') String id) {
		
		def search = [type:"post", publish:true]
		
		// posts for a specific tag
		if (ajaxloadtype == 'tag') { // start at one bc '/tag/verse' is split to ['','tag','verse']
			Tag tag = Tag.getOne()
			assert tag != null
			search += ["tags.id":tag.id]
		}
		// posts for a category
		else if (ajaxloadtype == 'category') {
			Category category = Category.getOne()
			assert category != null
			search += ["categories.id":category.id]
		}
		else if (ajaxloadtype != 'post'){
			log.error "ajaxloadtype unexpected value: $ajaxloadtype"
			throw new WebApplicationException(HttpServletResponse.SC_BAD_REQUEST)
		}

		// get a batch of posts specified by search params and sorted by date
		def posts = Post.find(
			search		: search,
			sort		: '-dateCreated',
			batchSize	: batchSize,
			batchNumber	: batchNum)
		
		
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

		def context = new PageContext([
			windowTitle:"Import from Wordpress Site", 
			body:'wpimport'])

		render('/templates/shell.html', context)

	}
	
	@GET
	@Produces('application/json')
	@Path('importgetjson')
	public String wordpressImportGetJson(@QueryParam('pagesToImport') String pagesToImport,
										 @QueryParam('url') String url) {

		/*	
		if (!MoksalizerRealm.isAdmin(request)) {
			throw new WebApplicationException(HttpServletResponse.SC_FORBIDDEN)
		}
		
		if (url == null || url.trim() == '') {
			throw new WebApplicationException(HttpServletResponse.SC_BAD_REQUEST)
		}
		*/
										 
		log.info "Importing from URL:" + url
		log.info "Importing pages: $pagesToImport"

		WordpressImporterJson importer = new WordpressImporterJson(Controller.instance.blogData.admin, url, './static/images', '/images')

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

	/*
	 * The authc shiro filter takes care of the actual login by intercepting a POST
	 * to /login, and retrieving the username, password, and rememberme form values.
	 */
	@GET
	@Produces('text/html')
	@Path('login')
	public String login() {

		def context = new PageContext([
			windowTitle:"Login",
			body:'login'])

		render('/templates/shell.html', context)
		
	}

	/*
	 * This should actually never get reached bc the authc filter catches
	 * the logout and does it automatically.
	 */
	@Path('logout')
	public String logout() {
		SecurityUtils.getSubject().logout()
		response.sendRedirect(uri.getBaseUri())
	}
	
	@GET
	@Produces('text/html')
	@Path("/tag/{tagslug}")
	public String getTag(@PathParam('tagslug') String tagslug,
						 @QueryParam('batchnum') @DefaultValue('0') int batchNum, 
						 @QueryParam('batchsize') @DefaultValue('10') int batchSize) {
		
		Tag tag = Tag.getOne(slug:tagslug)

		if (tag == null) {
			log.error "tag: $tagslug not found"
			throw new WebApplicationException(HttpServletResponse.SC_NOT_FOUND)
		}
			
		def posts = Post.find(
			search		: [type:'post', publish:true, 'tags.id':tag.id],
			sort		: '-dateCreated',
			batchSize	: batchSize,
			batchNumber	: batchNum)
		
		
		def subTitle = render('pagesubtitle.html', [subTitle:"Tag: ${tag.name}"])
		
		def context = new PageContext([
			posts:posts,
			subTitle:subTitle,
			body:'listajax',
			windowTitle:blogData.blogName,
			passToPage:[ajaxloadtype:'tag']
			])

		render('shell.html', context)

	}
						 
						 
	@GET
	@Produces('text/html')
	@Path('/summary')
	public String getSummary() {
		
		def posts = Post.find(
			search		: (MoksalizerRealm.isAdmin() ? [type:'post'] : [type:"post", publish:true]),
			sort		: '-dateCreated',
			withOnly	: ['title', 'datePublished', 'sequenceNumber', 'publish'])
		
		def tags = Tag.getAll()
		
		String windowTitle = blogData.blogName + ' - Summary'
		
		def context = new PageContext([
			posts:posts,
			tags:tags,
			windowTitle:windowTitle,
			body:'summary'
			])
		
		render('shell.html', context)

	}


						  
}