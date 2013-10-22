package org.tdmx.console.service.command;

import java.io.Serializable;

/**
 * A Command is executed by the UIService.
 * 
 * The results of the exeuction contains an errorCode and
 * possibly output fields in a concrete sub-Class.
 * 
 * @author Peter
 *
 */
public class AbstractCommand implements Serializable {

	//-------------------------------------------------------------------------
	//OUTPUT FIELDS
	//-------------------------------------------------------------------------
	
	protected int errorCode;

	public int getErrorCode() {
		return errorCode;
	}

}
