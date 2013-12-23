package org.tdmx.console.application.domain;

import java.util.Calendar;

import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;

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

		OBJECT_REGISTRY_STALE,
		OBJECT_REGISTRY_LOAD,
		OBJECT_REGISTRY_WRITE,

		SYSTEM_TRUST_STORE_EXCEPTION,

		KEY_STORE_ALGORITHM,
		KEY_STORE_EXCEPTION,
		KEY_STORE_KEYSTORE_EXCEPTION,
		KEY_STORE_IO_EXCEPTION,
		;
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private ProblemCode code;
	private Calendar timestamp;
	private Throwable cause;
	private String msg;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public ProblemDO( ProblemCode pc, Throwable t ) {
		this.timestamp = Calendar.getInstance();
		this.code = pc;
		this.cause = t;
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
	public DomainObjectType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		setSearchFields(ctx.getSearchFields());
		//TODO
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

	public Throwable getCause() {
		return cause;
	}

	public String getMsg() {
		return msg;
	}

}
