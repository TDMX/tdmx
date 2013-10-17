package org.tdmx.console.application.domain;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * These are severe Problems that indicate that the Application is not
 * working as expected.
 * 
 * @author Peter
 *
 */
public class Problem {

	public static AtomicInteger ID = new AtomicInteger(0);
	
	public static enum ProblemCode {
		CONFIGURATION_FILE_IO,
		CONFIGURATION_FILE_PARSE,
		;
	}
	
	private int id;
	private ProblemCode code;
	private Calendar timestamp;
	private Throwable throwable;
	private String msg;
	
	public Problem( ProblemCode pc, Throwable t ) {
		this.timestamp = Calendar.getInstance();
		this.code = pc;
		this.throwable = t;
	}

	public Problem( ProblemCode pc, String msg ) {
		this.timestamp = Calendar.getInstance();
		this.code = pc;
		this.msg = msg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Problem other = (Problem) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
	public int getId() {
		return id;
	}

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
