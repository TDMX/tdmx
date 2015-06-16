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
 * The SearchCriteria for Flows ( part of a ChannelFlowTarget part of a Channel ).
 * 
 * @author Peter Klauser
 * 
 */
public class ChannelFlowSearchCriteria extends ChannelSearchCriteria {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	/**
	 * The SHA-256 Fingerprint of the Target Agent.
	 */
	private String targetFingerprint;

	/**
	 * The SHA-256 Fingerprint of the Source Agent.
	 */
	private String sourceFingerprint;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public ChannelFlowSearchCriteria(PageSpecifier pageSpecifier) {
		super(pageSpecifier);
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

	public String getTargetFingerprint() {
		return targetFingerprint;
	}

	public void setTargetFingerprint(String targetFingerprint) {
		this.targetFingerprint = targetFingerprint;
	}

	public String getSourceFingerprint() {
		return sourceFingerprint;
	}

	public void setSourceFingerprint(String sourceFingerprint) {
		this.sourceFingerprint = sourceFingerprint;
	}

}
