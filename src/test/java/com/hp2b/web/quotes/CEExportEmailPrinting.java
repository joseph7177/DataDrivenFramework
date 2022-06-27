package com.hp2b.web.quotes;

import org.apache.log4j.Logger;

import com.hp2b.common.FrameworkMethods;

public class CEExportEmailPrinting extends FrameworkMethods {
	
	Logger logger = Logger.getLogger(CEExportEmailPrinting.class);
	
	private String url = "";
	
	private static final String config = "config";
	private static final String module = "Quotes";

	public void setEnvironment() {
		if (url.isEmpty()) {			
			url = _webcontrols.get().propFileHandler().readProperty(config, ENVIRONMENT);
		}
	}

}
