package com.moksamedia.moksalizer.rest

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

import com.moksamedia.moksalizer.PageContext
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Tag
import com.moksamedia.moksalizer.security.MoksalizerRealm

@Path('summary')
class Summary extends ResourceCommon {

	@GET
	@Produces('text/html')
	public String getSummary() {
		
		def find = doFind([
			search		: (MoksalizerRealm.isAdmin() ? [type:'post'] : [type:"post", publish:true]),
			sort		: '-dateCreated',
			withOnly	: ['title', 'datePublished', 'sequenceNumber', 'publish']
			])
				
		def posts = Post.find(find)
		
		def tags = Tag.getAll()
		
		String windowTitle = blogData.blogName + ' - Summary'
		
		def context = doContext([
			posts:posts,
			tags:tags,
			windowTitle:windowTitle,
			body:'summary'
			])
				
		doRender('shell.html', context)

	}

}
