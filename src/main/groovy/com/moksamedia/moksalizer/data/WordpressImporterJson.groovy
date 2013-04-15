package com.moksamedia.moksalizer.data

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder

import java.text.SimpleDateFormat

import org.apache.commons.lang.StringEscapeUtils
import org.htmlparser.Parser
import org.htmlparser.nodes.TextNode
import org.htmlparser.tags.ImageTag
import org.htmlparser.util.NodeList
import org.slf4j.LoggerFactory

import com.moksamedia.moksalizer.data.objects.Category
import com.moksamedia.moksalizer.data.objects.Comment
import com.moksamedia.moksalizer.data.objects.Post
import com.moksamedia.moksalizer.data.objects.Seq
import com.moksamedia.moksalizer.data.objects.Tag
import com.moksamedia.moksalizer.data.objects.User

import com.moksamedia.moksalizer.exception.MoksalizerException

class WordpressImporterJson {

	static final log = LoggerFactory.getLogger(getClass().simpleName)

	User author
	String wpSiteUrl
	String pathToImageFiles
	String urlToImageFiles
	Map tagsCreated = [:], categoriesCreated = [:]
	def postsCreated = [], pagesCreated = []


	public WordpressImporterJson(User author, String wpSiteUrl, String pathToImageFiles, String urlToImageFiles) {
		this.author = author
		this.wpSiteUrl = wpSiteUrl
		this.pathToImageFiles = pathToImageFiles
		this.urlToImageFiles = urlToImageFiles

		assert this.author != null
		assert this.pathToImageFiles != null
		assert this.urlToImageFiles != null
	}

	/*
	 * Pages are just a post of type "page". (Posts are type "post"). processPosts() is hacked to be able to handle pages individually.
	 */
	private boolean processPages(def pagesArray) {
		pagesArray.each { String page ->
			log.info "Process page data:\n" + page
			processPosts(page, "page")
		}
	}

	/*
	 * Processes a single post, a collection of posts, or a single page. 
	 */
	private void processPosts(String postsString, String type = "post") {

		log.info "Processing posts for data: " + JsonOutput.prettyPrint(postsString)

		String dateFormatString = "yyyy-MM-dd HH:mm:ss"
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString)

		Closure toDate = { String val ->
			dateFormat.clone().parse(val)
		}

		JsonSlurper slurper = new JsonSlurper()

		// POSTS

		def postsJson = slurper.parseText(postsString)

		if (postsJson.status != 'ok') {
			throw new MoksalizerException("Failed to import Posts from Json. Payload received but status = ${postsJson.status}.")
		}

		def posts
		// hack to handle a single post
		if (postsJson.containsKey('post')) {
			posts = [postsJson.post]
		}
		// hack to handle a page
		else if (postsJson.containsKey('page')) {
			posts = [postsJson.page]
		}
		else {
			posts = postsJson.posts
		}

		Seq sequence = Seq.getOne()

		int i=0;
		for (i=0;i<posts.size();i++) {

			def aPost = posts[i]

			assert aPost != null
			assert aPost?.id != null

			if (!Post.getOne([importId:aPost.id])) {

				def postTags = []
				def postCategories = []

				aPost.tags.each { postTags += createOrGetTag(it) }

				aPost.categories.each { postCategories += createOrGetCategory(it) }

				def comments = []
				aPost.comments.eachWithIndex { def commentData, int idx ->
					Comment comment = new Comment()
					comment.authorName = commentData?.name
					comment.authorEmail = commentData?.email
					comment.authorURL = commentData?.url
					comment.number = idx
					comment.html = fixEscapes(commentData.content, pathToImageFiles)
					comment.date = toDate(commentData.date)
					comments += [comment]
				}

				String title = aPost.title
				Date datePublished = toDate(aPost.date)
				String content = aPost.content
				String slug = aPost.slug
				String status = aPost.status
				String importId = aPost.id
				int sequenceNumber = sequence.nextPostNumber()

				log.info "JSON CONTENT:\n" + content

				String fixed = fixEscapes(content, pathToImageFiles)

				log.info "FIXED" + fixed

				Post post = new Post(
						author:author,
						title:title,
						html:fixed,
						slug:slug,
						datePublished:datePublished,
						publish: (status == 'publish'),
						importId:importId,
						type:type,
						sequenceNumber:sequenceNumber
						);

				if (comments.size() > 0) {
					post.comments = comments
				}
				
				if (postTags.size() > 0) {
					post.addTags(postTags)
				}

				if (postCategories.size() > 0) {
					post.addCategories(postCategories)
				}
				
				post.save()

				if (type == "post") {
					log.info "Post created: ${post.author.screenName} - ${sequenceNumber} - ${post.title} - ${post.nameComputerFriendly} - ${post.html}"
					postsCreated += post
				}
				else {
					log.info "Page created: ${post.author.screenName} - ${sequenceNumber} - ${post.title} - ${post.nameComputerFriendly} - ${post.html}"
					pagesCreated += post
				}


			}

			else {
				log.info "Post already found for importId = ${aPost.post_id.text()}"

			}

		}

	}

	private void saveTagsAndCategories() {

		tagsCreated.values().each { it.save() }

		categoriesCreated.values().each { it.save() }


	}

	private def createOrGetTag(def tagJson) {

		Tag tag = tagsCreated.get(tagJson.slug, null)

		if (tag == null) {
			tag = new Tag(name:tagJson.title, slug:tagJson.slug)
			tag.save()
			tagsCreated += [(tag.slug):tag]
			log.info "Tag CREATED: ${tag.name}, ${tag.slug}"
		}
		else {
			log.info "Tag FOUND: ${tag.name}, ${tag.slug}, (${tag.postCount})"
		}

		tag
	}

	private def createOrGetCategory(def categoryJson) {

		Category category = categoriesCreated.get(categoryJson.slug, null)

		if (category == null) {
			category = new Category(name:categoryJson.title, slug:categoryJson.slug)
			category.save()
			categoriesCreated += [(category.slug):category]
			log.info "Category CREATED: ${category.name}, ${category.slug}"
		}
		else {
			log.info "Category FOUND: ${category.name}, ${category.slug}"
		}

		category

	}


	private String fixEscapes(String htmlToFix, String urlForImages = null) {

		Parser htmlParser = Parser.createParser(htmlToFix, null)

		processElements(htmlParser.elements())

	}

	/*
	 * OK. This seems really inane, but the only way to avoid having character encoding/escaping 
	 * issues was to parse the HTML and process the TextNode elements by unescapingHtml and 
	 * then re-escapingHtml. The problem is that, coming from the JSON Rest API plugin, we are
	 * getting a mix of "&#x201C" encoded characters and "\u201C" characters. The first unescape
	 * takes care of the html entities, encoding them to unicode, and then we re-escape to
	 * get everything coded as html entities. We have to parse the HTML and extract the text
	 * nodes to avoid slashing the HTML tags themselves (treating them as strings).
	 */
	private String processElements(def elems) {

		String output = ""

		def node
		while (elems.hasMoreNodes()) {

			node = elems.nextNode()

			log.info "${node.toHtml()}"

			if (node instanceof TextNode) {
				//output += StringEscapeUtils.escapeHtml(node.getText())
				//output += StringEscapeUtils.escapeHtml(Translate.decode(node.getText()))
				output += StringEscapeUtils.escapeHtml(StringEscapeUtils.unescapeHtml(node.getText()))

			}
			else if (node instanceof ImageTag) {

				String remoteUrl = node.getImageURL()
				String tagText = node.toHtml()
				String fileName = remoteUrl.split('/').last()
				String localFilePath = "${pathToImageFiles}/${fileName}"
				File localFile = new File(localFilePath)

				if (!localFile.exists()) {
					log.info "Downloading image from: '${remoteUrl}' to '${pathToImageFiles}/${fileName}'"
					localFile.bytes = new java.net.URL(remoteUrl).bytes
				}
				tagText = tagText.replaceAll(remoteUrl, "${urlToImageFiles}/${fileName}")

				output += tagText

			}
			else {

				String nodeName = node.getRawTagName()

				output += "<${node.getText()}>"

				NodeList children = node.getChildren()

				if (children?.size() > 0) {
					output += processElements(children.elements())
				}

				if (node.getEndTag() != null) output += "</${nodeName}>"
			}

		}

		output

	}


	private String fetchAllPosts() {

		log.info "Fetching all posts for URL:" + wpSiteUrl

		def http = new HTTPBuilder(wpSiteUrl)

		// perform a GET request, expecting JSON response data
		http.request( GET, TEXT ) {
			uri.path = '/'
			uri.query = [json:'get_recent_posts', count:1000000] // just a really big number to make sure we get ALL posts

			headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

			// response handler for a success response code:
			response.success = { resp, InputStreamReader reader ->

				String text = reader.text

				log.info "RECEIVED:" + text

				text

			}

			// handler for any failure status code:
			response.failure = { resp ->
				log.error "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				""
			}
		}
	}

	private def fetchPages(String pagesToImport) {

		log.info "Fetching pages ($pagesToImport) for URL:" + wpSiteUrl

		def pageList = pagesToImport.toLowerCase().replaceAll(';', ',').split(',')

		def pages = []

		pageList.each { String pageSlug ->

			def http = new HTTPBuilder(wpSiteUrl)

			// perform a GET request, expecting JSON response data
			http.request( GET, TEXT ) {
				uri.path = '/'
				uri.query = [json:'get_page', page_slug:pageSlug]

				headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

				// response handler for a success response code:
				response.success = { resp, InputStreamReader reader ->

					String text = reader.text

					log.info "RECEIVED:" + text

					pages += [text]

				}

				// handler for any failure status code:
				response.failure = { resp ->
					log.error "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
					[""]
				}
			}
		}

		pages

	}

	private void processTags(String tagsString) {

		JsonSlurper slurper = new JsonSlurper()

		// TAGS

		def tagsJson = slurper.parseText(tagsString)

		if (tagsJson.status != 'ok') {
			throw new MoksalizerException("Failed to import Tags from Json. Payload received but status = ${tagsJson.status}.")
		}

		tagsJson.tags.each { createOrGetTag(it) }

	}

	private def processCategories(String categoriesString) {

		JsonSlurper slurper = new JsonSlurper()

		// CATEGORIES

		def categoriesJson = slurper.parseText(categoriesString)

		if (categoriesJson.status != 'ok') {
			throw new MoksalizerException("Failed to import Categories from Json. Payload received but status = ${categoriesJson.status}.")
		}

		categoriesJson.categories.each { createOrGetCategory(it) }


	}

	private def fetchAllTags() {

		def http = new HTTPBuilder(wpSiteUrl)

		// perform a GET request, expecting JSON response data
		http.request( GET, TEXT ) {
			uri.path = '/'
			uri.query = [json:'get_tag_index']

			headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

			// response handler for a success response code:
			response.success = { resp, InputStreamReader reader ->

				String tags = reader.text

				log.info "RECEIVED:" + JsonOutput.prettyPrint(tags)

				tags
			}

			// handler for any failure status code:
			response.failure = { resp ->
				log.error "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				""
			}
		}
	}

	private def fetchAllCategories() {

		def http = new HTTPBuilder(wpSiteUrl)

		// perform a GET request, expecting JSON response data
		http.request( GET, TEXT ) {
			uri.path = '/'
			uri.query = [json:'get_category_index']

			headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

			// response handler for a success response code:
			response.success = { resp, InputStreamReader reader ->

				String categories = reader.text

				log.info "RECEIVED:" + JsonOutput.prettyPrint(categories)

				categories
			}

			// handler for any failure status code:
			response.failure = { resp ->
				log.error "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				""
			}
		}
	}
}
