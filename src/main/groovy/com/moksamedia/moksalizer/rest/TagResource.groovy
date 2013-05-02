package com.moksamedia.moksalizer.rest

import javax.servlet.http.HttpServletResponse
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException

import com.moksamedia.moksalizer.PageContext
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Tag

@Path('tag')
class TagResource extends ResourceCommon {

	
	@GET
	@Produces('text/html')
	@Path("{tagslug}")
	public String getTag(@PathParam('tagslug') String tagslug,
			@QueryParam('batchnum') @DefaultValue('0') int batchNum,
			@QueryParam('batchsize') @DefaultValue('10') int batchSize) {

		Tag tag = Tag.getOne(slug:tagslug)

		if (tag == null) {
			log.error "tag: $tagslug not found"
			throw new WebApplicationException(HttpServletResponse.SC_NOT_FOUND)
		}

		def find = doFind([
			search		: [type:'post', publish:true, 'tags.id':tag.id],
			sort		: '-dateCreated',
			batchSize	: batchSize,
			batchNumber	: batchNum
			])
		
		def posts = Post.find(find)

		def subTitle = doRender('pagesubtitle.html', [subTitle:"Tag: ${tag.name}"])

		def context = doContext([
			posts:posts,
			subTitle:subTitle,
			body:'listajax',
			windowTitle:blogData.blogName,
			passToPage:[ajaxloadtype:'tag']
		])

		doRender('shell.html', context)

	}

	/**
	 * An ajax method used to dynamically load more posts for a tag.
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
		
		def search = [type:"post", publish:true, "tags.id":id]

		def find = doFind([
				search		: search,
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
