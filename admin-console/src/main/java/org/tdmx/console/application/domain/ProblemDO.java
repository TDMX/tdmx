package org.tdmx.console.application.domain;

import java.util.Calendar;

/**
 * These are severe Problems that indicate that the Application is not
 * working as expected.
 * 
 * @author Peter
 *
 */
public class ProblemDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public static enum ProblemCode {
		CONFIGURATION_FILE_READ_IO,
		CONFIGURATION_FILE_WRITE_IO,
		CONFIGURATION_FILE_PARSE,
		CONFIGURATION_FILE_MARSHAL,
		
		SYSTEM_TRUST_STORE_ALGORITHM,
		SYSTEM_TRUST_STORE_EXCEPTION,

		CERTIFICATE_STORE_ALGORITHM,
		CERTIFICATE_STORE_EXCEPTION,
		CERTIFICATE_STORE_KEYSTORE_EXCEPTION,
		CERTIFICATE_STORE_IO_EXCEPTION,
		;
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private ProblemCode code;
	private Calendar timestamp;
	private Throwable throwable;
	private String msg;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public ProblemDO( ProblemCode pc, Throwable t ) {
		this.timestamp = Calendar.getInstance();
		this.code = pc;
		this.throwable = t;
	}

	public ProblemDO( ProblemCode pc, String msg ) {
		this.timestamp = Calendar.getInstance();
		this.code = pc;
		this.msg = msg;
	}

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		return null;
	}

	@Override
	public <E extends DomainObject> E copy() {
		return null;
	}

   //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public ProblemCode getCode() {
		return code;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMsg() {
		return msg;
	}

}
