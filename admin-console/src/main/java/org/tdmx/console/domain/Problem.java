/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tdmx.console.application.domain.ProblemDO.ProblemCode;

public class Problem implements Serializable {

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String id;
	private final ProblemCode code;
	private final Date timestamp;
	private String text;
	private String[] causes;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public Problem(org.tdmx.console.application.domain.ProblemDO p) {
		this.id = p.getId();
		this.code = p.getCode();
		this.timestamp = p.getTimestamp().getTime();
		Throwable t = p.getCause();
		if (t != null) {
			this.text = t.getLocalizedMessage();
			this.causes = getCauseList(t);
		} else {
			this.text = p.getMsg();
			this.causes = new String[0];
		}
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private String[] getCauseList(Throwable t) {
		List<String> causeList = new ArrayList<>();
		Throwable cause = t.getCause();
		while (cause != null) {
			causeList.add(t.getLocalizedMessage());
			cause = cause.getCause();
			if (causeList.size() > 20) {
				break; // Just incase of some unfortunate cyclic thingy.
			}
		}
		return causeList.toArray(new String[0]);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getId() {
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
