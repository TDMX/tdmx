package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tdmx.console.application.domain.ProblemDO.ProblemCode;

public class Problem implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private int id;
	private ProblemCode code;
	private Date timestamp;
	private String text;
	private String[] causes;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public Problem( org.tdmx.console.application.domain.ProblemDO p ) {
		this.id = p.getId();
		this.code = p.getCode();
		this.timestamp = p.getTimestamp().getTime();
		Throwable t =  p.getThrowable();
		if ( t != null ) {
			this.text = t.getLocalizedMessage();
			this.causes = getCauseList(t);
		} else {
			this.text = p.getMsg();
			this.causes = new String[0];
		}
	}
	
	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private String[] getCauseList( Throwable t ) {
		List<String> causeList = new ArrayList<>();
		Throwable cause = t.getCause();
		while ( cause != null ) {
			causeList.add(t.getLocalizedMessage());
			cause = cause.getCause();
			if ( causeList.size() > 20 ) {
				break; // Just incase of some unfortunate cyclic thingy.
			}
		}
		return causeList.toArray(new String[0]);
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public int getId() {
		return id;
	}

	public ProblemCode getCode() {
		return code;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getText() {
		return text;
	}

	public String[] getCauses() {
		return causes;
	}

}
