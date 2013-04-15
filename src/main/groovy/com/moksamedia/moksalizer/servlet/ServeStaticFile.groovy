package com.moksamedia.moksalizer.servlet

import java.net.URL;

import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Slf4j

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.Response;

@Slf4j
class ServeStaticFile {
	
	MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap()
	
	public void serveStatic(String path, HttpServletResponse response) {
		
		if (!staticFileExists(path)) {
			//log.info "Static file does not exist at $path"
			response.status = HttpServletResponse.SC_NOT_FOUND
		}
		else {

			//log.info "Serving static file: $path"
			def output = serveStaticFile(path,response)

			output = convertOutputToByteArray(output)

			def contentLength = output.length
			response.setHeader('Content-Length', contentLength.toString())

			def stream = response.getOutputStream()
			stream.write(output)
			stream.flush()
			stream.close()
		}

	}

	private boolean staticFileExists(String path) {
		!path.endsWith('/') && staticFileFrom(path) != null
	}

	private def serveStaticFile(String path, HttpServletResponse response) {
		URL url = staticFileFrom(path)
		String contentType = mimetypesFileTypeMap.getContentType(url.toString())
		//log.info "Content-Type = $contentType"
		response.setHeader('Content-Type', contentType)
		url.openStream().bytes
	}

	private URL staticFileFrom(path) {
		
		def file = new File(path)
		
		if (file.exists()) {
			file.toURI().toURL()
		}
		else {
			null
		}
		
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
