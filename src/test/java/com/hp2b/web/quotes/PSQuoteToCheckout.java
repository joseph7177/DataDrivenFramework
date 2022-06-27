package com.hp2b.web.quotes;

import org.apache.log4j.Logger;

import com.hp2b.common.FrameworkMethods;

public class PSQuoteToCheckout extends FrameworkMethods {
	
	Logger logger = Logger.getLogger(PSQuoteToCheckout.class);

	private String url = "";

	public void setEnvironment(String store) {
		String selector = System.getProperty("env");
		selector = selector + "-PS" + store;
		url = _webcontrols.get().propFileHandler().readProperty("config", selector);
	}

}
