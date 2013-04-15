package com.moksamedia.moksalizer.exception


class TemplateRenderException extends MoksalizerException {
	public TemplateRenderException(String message) {
		super(message)
	}
	
	public TemplateRenderException(String message, Exception ex) {
		super(message, ex)
	}
}
