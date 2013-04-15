package com.moksamedia.moksalizer

import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Slf4j

import com.moksamedia.moksalizer.exception.TemplateRenderException

@Slf4j
class BlogratTemplater {

	public static final String IGNORE_TOKEN = "<!--GSP IGNORE-->"
	public static final String TEMPLATE_PATTERN = /\[template:\s*(.+?)\]/
	public static final String BLOCK_PATTERN = /\[block:\s*(.+?)\]/
	
	String templateRoot = 'templates'
		
	String render(templateName, context=[:]) {
		renderTemplate(loadBlock(templateName), new SafeMap(context))
	}
	
	String loadBlock(templateName) {
		
		String text = ''

		try {
			text += loadTemplateText(templateName)
		}
		catch(java.io.IOException ex) {
			throw new TemplateRenderException("Unable to find template: $templateName", ex)
		}
		
		text
	}
	
	String loadTemplateText(templateName) {
		
		String text = ''
		String fullTemplateFilename = [templateRoot, templateName].join(File.separator)
			
		try {
			text += new File(fullTemplateFilename).text
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
		}
		text
	 }
	
	InputStream loadResource(String path) {
		Thread.currentThread().contextClassLoader.getResourceAsStream(path)
	}
	
	protected String processTags(String text, def context) {
		
		Closure processExpression = { String expression ->

			log.info "expression=$expression"
			
			if (expression[0] == '$') {

				expression = expression[1..-1] // remove '$'
				
				if (expression[0] == '{') {
					expression = expression[1..expression.size()-3]// remove the brackets
				}

				log.info "EXPERSSION = $expression"
				
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
				IGNORE_TOKEN + loadTemplateText(templateName + ".html") + IGNORE_TOKEN
			}

		}

		// load template tags
		text = text.replaceAll(~TEMPLATE_PATTERN) { Object[] it ->

			String templateName = processExpression(it[1])

			if (templateName == null) {
				""
			}
			else {
				processTags(loadTemplateText(templateName + ".html"), context) // note the recursive call here so that template tags inside templates loaded from
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
	protected String renderTemplate(String text, def context) {

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
