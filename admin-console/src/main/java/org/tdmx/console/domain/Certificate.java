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
import java.util.Date;

import org.tdmx.core.system.lang.CalendarUtils;

public class Certificate implements Serializable {

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String id;
	private final String text;
	private final Date validFrom;
	private final Date validTo;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public Certificate(org.tdmx.console.application.domain.X509CertificateDO p) {
		this.id = p.getId();
		this.text = p.getCertificate().getInfo();
		this.validFrom = CalendarUtils.getDateTime(p.getCertificate().getNotBefore());
		this.validTo = CalendarUtils.getDateTime(p.getCertificate().getNotAfter());
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

}
