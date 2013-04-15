package com.moksamedia.moksalizer.rest

import groovy.util.logging.Slf4j

import javax.activation.MimetypesFileTypeMap
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

@Slf4j
@Path("/static/{filename}")
class Static {

	MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap()
	
	public static String urlPath = 'static'
	public static String filesystemPath = 'static'
	
	@GET
	public Response serveStatic(@PathParam("filename") String filename, @Context HttpServletResponse response) {
		
		log.info "Serving static file: $filename"
		
		serveStaticFile(response,filename)
	}

	protected boolean staticFileExists(String path) {
		!path.endsWith('/') && staticFileFrom(path) != null
	}	

	protected def serveStaticFile(HttpServletResponse response, String path) {
		URL url = staticFileFrom(path)
		String contentType = mimetypesFileTypeMap.getContentType(url.toString())
		log.info "Content-Type = $contentType"
		response.setHeader('Content-Type', contentType)
		url.openStream().bytes
	}

	protected URL staticFileFrom(path) {
		
		def fullPath = ['static', path].join(File.separator)
		
		def file = new File(fullPath)

		if(file.exists()) return file.toURI().toURL()

		try {
			return Thread.currentThread().contextClassLoader.getResource(['static', path].join('/'))
		} catch(Exception e) {
			return null
		}
	}
	
}
