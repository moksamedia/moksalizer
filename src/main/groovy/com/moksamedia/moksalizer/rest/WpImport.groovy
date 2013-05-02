package com.moksamedia.moksalizer.rest

import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException

import com.moksamedia.moksalizer.Controller
import com.moksamedia.moksalizer.PageContext
import com.moksamedia.moksalizer.data.WordpressImporterJson
import com.moksamedia.moksalizer.security.MoksalizerRealm


@Slf4j
@Path('wpimport')
class WpImport extends ResourceCommon {

	
	/*
	 * Import a wordpress site via JSON REST API
	 */
	@GET
	@Produces('text/html')
	public String wordpressImport() {
		
		if (!MoksalizerRealm.isAdmin(request)) {
			throw new WebApplicationException(HttpServletResponse.SC_FORBIDDEN)
		}

		def context = new PageContext([
			windowTitle:"Import from Wordpress Site",
			body:'wpimport'])

		Root.render('shell.html', context)

	}
	
	@GET
	@Produces('application/json')
	@Path('importgetjson')
	public String wordpressImportGetJson(@QueryParam('pagesToImport') String pagesToImport,
										 @QueryParam('url') String url) {
										 
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

}
