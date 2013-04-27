package com.moksamedia.moksalizer

import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Slf4j

import com.moksamedia.moksalizer.exception.TemplateRenderException


/*
 * The IGNORE_TOKEN is used to exclude portions of the template from rendering, esp.
 * important with JQuery code because of the dollar signs.
 * 
 * [template:templatename] can be used to load a template and render its contents
 * 
 * [block:blockname] is used to load a block of un-rendered text/html/javascript
 */
@Slf4j
class MoksalizerTemplater {

	public static final String IGNORE_TOKEN = "<!--GSP IGNORE-->"
	public static final String TEMPLATE_PATTERN = /\[template:\s*(.+?)\]/
	public static final String BLOCK_PATTERN = /\[block:\s*(.+?)\]/
	
	public static final String templateRoot = Controller.instance.config.templateRoot
	
	///////////////////////////////////////////////////////////////////////////////////////////
	//MAIN ENTRY METHOD
	
	public String render(templateName, context=[:]) {
		String rendered = renderTemplate(loadTemplateFile(templateName), new SafeMap(context))
		rendered.replaceAll(~IGNORE_TOKEN, '')
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// FIND AND LOAD TEMPLATE RAW TEXT
	
	private String loadTemplateFile(templateName) {
		
		File file = new File('.')
		log.info "BASE PATH = ${file.getAbsolutePath()}"
		
		String text = ''

		try {
			text += loadTemplateFileText(templateName)
		}
		catch(java.io.IOException ex) {
			throw new TemplateRenderException("Unable to find template: $templateName", ex)
		}
		
		text
	}
	
	private String loadTemplateFileText(templateName) {
		
		String text = ''
		String fullTemplateFilename = [templateRoot, templateName].join(File.separator)
			
		try {
			text += new File(fullTemplateFilename).text
			log.info "Template file (${fullTemplateFilename}) found."
		} 
		catch(java.io.FileNotFoundException origEx) {
						
			def resource = loadResource(templateName)
			
			if (!resource) {
			
				resource = loadResource(fullTemplateFilename) // added this so that the test templates were found
			
				if (!resource) {
					throw new java.io.FileNotFoundException(templateName)
				}
			}
			
			text += resource.text
			
			log.info "Template file (${templateName}) found as resource."
			
		}
		text
	 }
	
	private InputStream loadResource(String path) {
		Thread.currentThread().contextClassLoader.getResourceAsStream(path)
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// RENDER THE TEMPLATE (this is where the magic happens)
	
	
	private String processTags(String text, def context) {
		
		Closure processExpression = { String expression ->
			
			if (expression[0] == '$') {

				expression = expression[1..-1] // remove '$'
				
				if (expression[0] == '{') {
					expression = expression[1..expression.size()-3]// remove the brackets
				}
				
				GroovyShell shell = new GroovyShell(new Binding(context));

				def result = shell.evaluate(expression)

				
				if (result instanceof String && result != "") {
					result
				}
				else {
					null
				}
			}
			else {
				expression
			}
		}

		// load block tags
		text = text.replaceAll(~BLOCK_PATTERN) { Object[] it ->

			String templateName = processExpression(it[1])

			if (templateName == null) {
				""
			}
			else {
				IGNORE_TOKEN + loadTemplateFileText(templateName + ".html") + IGNORE_TOKEN
			}

		}

		// load template tags
		text = text.replaceAll(~TEMPLATE_PATTERN) { Object[] it ->

			String templateName = processExpression(it[1])

			if (templateName == null) {
				""
			}
			else {
				processTags(loadTemplateFileText(templateName + ".html"), context) // note the recursive call here so that template tags inside templates loaded from
																			   	   // tags are themselves processed
			}

		}

		text
	}

	
	/**
	 * Most of the code below allows the user to use the ignoreToken to mark off sections of the
	 * template to be ignored by the GSP processing. This is done by splitting the input text
	 * by the ignoreToken and (essentially) processing every other chunk. 
	 * 
	 * @param text
	 * @param context
	 * @return rendered text
	 */
	private String renderTemplate(String text, def context) {

		text  = processTags(text, context)

		String result = []

		Closure process = { String textToProcess ->
			//log.info "PROCESSING: " + textToProcess
			SimpleTemplateEngine engine = new SimpleTemplateEngine()
			def writable = engine.createTemplate(textToProcess).make(context as Map)
			writable.toString()
		}

		Closure passThrough = { String textToProcess ->
			IGNORE_TOKEN + textToProcess + IGNORE_TOKEN
		}

		def chunks = text.split(IGNORE_TOKEN)

		//log.info "CHUNKS: " + chunks.toString()

		// just process whole thing
		if (chunks.size() == 1) {
			result = process(text)
		}
		else {

			/* 
			 * We always process the first block and ignore every other block after because
			 * there are two possible states:
			 * 1) We start with a block to process
			 * 2) We start with an ignoreToken, which is split out and leaves an empty string
			 *    at the beginning (which we process, but does nothing), and ignore the next
			 *    block
			 */

			boolean shouldProcess = true
			result = chunks.inject("") { acc, String chunk ->

				if (chunk == "") {
					// ignore
				}
				else if (shouldProcess) {
					acc += process(chunk)
				}
				else {
					acc += passThrough(chunk)
				}
				shouldProcess = !shouldProcess
				acc
			}

		}

		//log.info "RESULT: " + result

		result

	}


}
