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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An EndpointPermission specifies the send/recv/requestedSend/requestedRecv Authorization of a Channel.
 * 
 * @author Peter Klauser
 * 
 */
@Embeddable
public class EndpointPermission implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	@Column(name = "grant", length = EndpointPermissionGrant.MAX_PERMISSION_LEN)
	private EndpointPermissionGrant grant;

	@Column(name = "highMarkBytes")
	private BigInteger highMarkBytes;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "validUntil")
	private Date validUntil;

	@Embedded
	private AgentSignature signature;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	EndpointPermission() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelAuthorization [id=");
		builder.append("");
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AgentSignature getSignature() {
		return signature;
	}

	public void setSignature(AgentSignature signature) {
		this.signature = signature;
	}

	public EndpointPermissionGrant getGrant() {
		return grant;
	}

	public void setGrant(EndpointPermissionGrant grant) {
		this.grant = grant;
	}

}
