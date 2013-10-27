package org.tdmx.console.application.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtils {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private static Logger log = LoggerFactory.getLogger(ValidationUtils.class);
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public static <E extends Object> void mandatoryTextField(String field, E error, List<E> errors ) {
		if ( !hasText(field)) {
			errors.add(error);
		}
	}
	
	public static <E extends Object> void mandatoryNumberField(Number field, E error, List<E> errors ) {
		if ( !hasValue(field)) {
			errors.add(error);
		}
	}
	
	public static <E extends Object> void optionalTextFieldGroup(String[] fields, E error, List<E> errors ) {
		boolean all = true;
		boolean none = true;
		for( String field : fields ) {
			if ( hasText(field)) {
				none = false;
			} else {
				all = false;
			}
		}
		if ( !all && !none ) {
			errors.add(error);
		}
	}
	
	public static <E extends Object> void optionalHostnameField(String hostname, E error, List<E> errors ) {
		if ( hasText(hostname) && !isValidHostname(hostname)) {
			errors.add(error);
		}
	}
	
	public static <E extends Object> void optionalPortField(Integer port, E error, List<E> errors ) {
		if ( hasValue(port) && !isValidPort(port)) {
			errors.add(error);
		}
	}
	
	public static <E extends Object> void optionalEnumeratedTextField(String field, List<String> values, E error, List<E> errors ) {
		if ( hasText(field) && !values.contains(field)) {
			errors.add(error);
		}
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private static boolean hasText( String text ) {
		return ( text != null && text.length() > 0 );
	}
	
	private static boolean hasValue( Number value ) {
		return ( value != null );
	}
	
	private static boolean isValidHostname( String hostname ) {
		try {
			InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			log.info("Invalid hostname "+ hostname, e);
			return false;
		}
		return true;
	}
	
	private static boolean isValidPort( int port ) {
		return port >= 0 && port <= 65536;
	}
	
}
