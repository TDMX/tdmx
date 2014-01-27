package org.tdmx.lib.control.domain;



public enum AuthorizationStatus {

	ACTIVE, 
	BLOCKED,
	UNKNOWN,
	CONFLICT, // fingerprint conflict.
	ERROR, // certificate processing error
	;
	
	public static final int MAX_AUTHORIZATIONSTATUS_LEN = 12;
}
