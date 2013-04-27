package com.moksamedia.moksalizer.servlet

import java.net.URL;

import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Slf4j

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.Response;

@Slf4j
class ServeStaticFile {
	
	private static volatile Map cache = [:]
	
	MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap()
	
	public static void clearCache() {
		cache = [:]
	}
	
	public static void nullCache() {
		cache = null
	}
	
	public void serveStatic(String path, HttpServletResponse response) {

		File file = staticFileFrom(path)
		
		if (file == null) {
			response.status = HttpServletResponse.SC_NOT_FOUND
			return
		}
		
		def output
		
		// CACHED
		if (cache.containsKey((path)) && file.lastModified() == cache[(path)].lastModified) {
			output = cache[(path)].output
			//log.info "Loading cached $path"
		}
		
		// NOT CACHED
		else {
			output = serveStaticFile(file,response)
			output = convertOutputToByteArray(output)
			cache[(path)] = [output:output, lastModified:file.lastModified()]
		}
		

		def contentLength = output.length
		//log.info "SERVING STATIC: ${file.toString()}:${contentLength.toString()}"
		response.setHeader('Content-Length', contentLength.toString())

		def stream = response.getOutputStream()
		stream.write(output)
		stream.flush()
		stream.close()


	}

	private def serveStaticFile(File file, HttpServletResponse response) {
		String contentType = mimetypesFileTypeMap.getContentType(file)
		response.setHeader('Content-Type', contentType)
		file.bytes
	}

	/**
	 * If the file exists and it's a file, not a directory, it's converted to a URL
	 * and returned.
	 */
	private File staticFileFrom(String path) {
				
		def file = new File(path)
		
		(file.exists() && file.isFile()) ? file : null
						
		// TODO: integrate ServletContext.getResouce() as well as classloader searching
		/*
		try {
			return Thread.currentThread().contextClassLoader.getResource(['static', path].join('/'))
		} catch(Exception e) {
			return null
		}
		*/
	}
	
	
	private byte[] convertOutputToByteArray(output) {
		if(output instanceof String)
			output = output.getBytes()
		else if(output instanceof GString)
			output = output.toString().getBytes()
		return output
	}
	
}
