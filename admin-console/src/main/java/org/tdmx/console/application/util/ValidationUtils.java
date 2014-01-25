package org.tdmx.console.application.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError.ERROR;
import org.tdmx.core.system.lang.CalendarUtils;

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
	
	public static void mandatoryField(Object fieldValue, String fieldName, ERROR error, List<FieldError> errors ) {
		if ( fieldValue == null ) {
			errors.add( new FieldError(fieldName, error));
		}
	}
	
	public static void mandatoryTextField(String fieldValue, String fieldName, ERROR error, List<FieldError> errors ) {
		if ( !hasText(fieldValue)) {
			errors.add( new FieldError(fieldName, error));
		}
	}
	
	public static void mandatoryDateField(Date fieldValue, String fieldName, ERROR error, List<FieldError> errors ) {
		if ( fieldValue == null ) {
			errors.add( new FieldError(fieldName, error));
		}
	}
	
	public static void futureDateField(Date fieldValue, String fieldName, ERROR error, List<FieldError> errors ) {
		if ( fieldValue != null ) {
			Calendar fieldDate = CalendarUtils.getDate(fieldValue);
			Calendar todayMidnight = CalendarUtils.getDate(new Date());
			
			if ( todayMidnight.after(fieldDate)) {
				errors.add( new FieldError(fieldName, error));
			}
		}
	}
	
	public static void mandatoryNumberField(Number fieldValue, String fieldName, ERROR error, List<FieldError> errors ) {
		if ( !hasValue(fieldValue)) {
			errors.add(new FieldError(fieldName, error));
		}
	}
	
	public static void optionalTextFieldGroup(String[] fieldValues, String fieldName, ERROR error, List<FieldError> errors ) {
		boolean all = true;
		boolean none = true;
		for( String fieldValue : fieldValues ) {
			if ( hasText(fieldValue)) {
				none = false;
			} else {
				all = false;
			}
		}
		if ( !all && !none ) {
			errors.add(new FieldError(fieldName, error));
		}
	}
	
	public static void optionalHostnameField(String hostname, String fieldName, int pos,  ERROR error, List<FieldError> errors ) {
		if ( hasText(hostname) && !isValidHostname(hostname)) {
			errors.add( new FieldError(fieldName,pos,error));
		}
	}
	
	public static void optionalHostnameField(String hostname, String fieldName, ERROR error, List<FieldError> errors ) {
		optionalHostnameField(hostname, fieldName, 0, error, errors );
	}
	
	public static void optionalEnumeratedTextField(String fieldValue, List<String> values, String fieldName, ERROR error, List<FieldError> errors ) {
		if ( hasText(fieldValue) && !values.contains(fieldValue)) {
			errors.add(new FieldError(fieldName, error));
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
	
}
