package org.tdmx.console.application.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {

	private static Logger log = LoggerFactory.getLogger(StringUtils.class);
	
	/**
	 * Convert Calendar to Date 
	 */
	public static boolean hasText( String text ) {
		return ( text != null && text.length() > 0);
	}

}
