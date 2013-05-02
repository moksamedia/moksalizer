package com.moksamedia.moksalizer.rest

import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletResponse
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException

import org.apache.shiro.SecurityUtils

import com.moksamedia.moksalizer.PageContext
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Tag
import com.moksamedia.moksalizer.security.MoksalizerRealm


@Slf4j
@Path('/')
public class Root extends ResourceCommon {
				
	public Root() {
		log.info "Creating ROOT"
	}
	
		
	@GET
	@Produces('text/css')
	@Path(/{filename: \S+\.gcss$}/) // *.gcss
	public String getRootStyle(@PathParam("filename") String filename) {
		doRender(filename, cssCtx)
	}
	
	@GET
	@Produces('text/html')
	public String getRoot(@QueryParam('batchnum') @DefaultValue('0') int batchNum, 
						  @QueryParam('batchsize') @DefaultValue('10') int batchSize) {
		
		Map find = doFind([
			search		: [type:'post', publish:true],
			sort		: '-dateCreated',
			batchSize	: batchSize,
			batchNumber	: batchNum
			])
					
		def posts = Post.find(find)						
							 				 
		def context = doContext([
			posts:posts, 
			body:'listajax', 
			windowTitle:blogData['blogName'],
			passToPage:[ajaxloadtype:'post']
			])
				
		doRender('shell.html', context)
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
	@Path('ajax/posts')
	public String loadPostsAjax (@QueryParam('batchnum') @DefaultValue('0') int batchNum, 
						  		 @QueryParam('batchsize') @DefaultValue('10') int batchSize,
								 @QueryParam('id') @DefaultValue('') String id) {
						
		def find = doFind([
			search		: [type:"post", publish:true],
			sort		: '-dateCreated',
			batchSize	: batchSize,
			batchNumber	: batchNum
			])			
				
		// get a batch of posts specified by search params and sorted by date
		def posts = Post.find(find)
		
		// add the posts to the context var
		def context = doContext([
			posts:posts, 
			body:'listajax'
			])
		
		doRender('listajax.html', context)

	}
						  
}