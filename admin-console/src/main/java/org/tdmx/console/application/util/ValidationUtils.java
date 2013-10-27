package org.tdmx.console.application.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class ValidationUtils {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

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
		if ( hasText(hostname) ) {
			try {
				InetAddress.getByName(hostname);
			} catch (UnknownHostException e) {
				return false;
			}
		}
		return false;
	}
	
	private static boolean isValidPort( int port ) {
		return port >= 0 && port <= 65536;
	}
	
}
