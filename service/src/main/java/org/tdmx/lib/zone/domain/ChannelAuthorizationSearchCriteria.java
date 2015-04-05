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
 * The SearchCriteria for an ChannelAuthorization.
 * 
 * @author Peter Klauser
 * 
 */
public class ChannelAuthorizationSearchCriteria {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final PageSpecifier pageSpecifier;

	/**
	 * Specify the each individual field of of the ChannelOrigin to search for.
	 */
	private final ChannelOrigin origin = new ChannelOrigin();

	/**
	 * Specify the each individual field of of the ChannelDestination to search for.
	 */
	private final ChannelDestination destination = new ChannelDestination();

	/**
	 * A ChannelAuthorization belongs to a Domain. Provide this to limit the results to the ChannelAuthorizations of the
	 * Domain, or leave null to retrieve ChannelAuthorizations for all the Zone's Domains.
	 */
	private String domainName;

	/**
	 * If Boolean.TRUE then search only unconfirmed ChannelAuthorizations, else if Boolean.FALSE then only search for
	 * confirmed ChannelAuthorizations, else if null find confirmed OR unconfirmed.
	 */
	private Boolean unconfirmed;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public ChannelAuthorizationSearchCriteria(PageSpecifier pageSpecifier) {
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

	public ChannelOrigin getOrigin() {
		return origin;
	}

	public ChannelDestination getDestination() {
		return destination;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public Boolean getUnconfirmed() {
		return unconfirmed;
	}

	public void setUnconfirmed(Boolean unconfirmed) {
		this.unconfirmed = unconfirmed;
	}

}
