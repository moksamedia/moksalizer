package com.moksamedia.moksalizer.rest

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import groovy.util.logging.Slf4j

@Slf4j
@Path("/")
public class Root {

	public Root() {
		log.info "Creating ROOT"
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {
		log.info "HTML"
		return "<html> " + "<title>" + "Hello Jersey" + "</title>" + "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayTextHello() {
		log.info "TEXT"
		return "Hello"
	}

}