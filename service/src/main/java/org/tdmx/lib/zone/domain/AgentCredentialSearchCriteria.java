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
package org.tdmx.lib.zone.domain;

import org.tdmx.lib.common.domain.PageSpecifier;

/**
 * The SearchCriteria for an AgentCredential.
 * 
 * @author Peter Klauser
 * 
 */
public class AgentCredentialSearchCriteria {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final PageSpecifier pageSpecifier;

	private AgentCredentialType type;

	private AgentCredentialStatus status;

	/**
	 * The fully qualified domain name ( includes the zoneApex ).
	 */
	private String domainName;

	/**
	 * The address's localName
	 */
	private String addressName;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public AgentCredentialSearchCriteria(PageSpecifier pageSpecifier) {
		if (pageSpecifier == null) {
			throw new IllegalArgumentException("Missing pageSpecifier");
		}
		this.pageSpecifier = pageSpecifier;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------
	public PageSpecifier getPageSpecifier() {
		return pageSpecifier;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}

	public AgentCredentialType getType() {
		return type;
	}

	public void setType(AgentCredentialType type) {
		this.type = type;
	}

	public AgentCredentialStatus getStatus() {
		return status;
	}

	public void setStatus(AgentCredentialStatus status) {
		this.status = status;
	}

}
