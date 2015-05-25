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

import javax.persistence.Embeddable;

/**
 * A FlowTargetSession is an encryption public key information with validity information.
 * 
 * A FlowTargetSession provides for 2 simultaneous FlowSessions to allow seamless replacement of old session keys with
 * newer ones. The 2nd FlowSession must have a {@link #validFrom} Date greater than the 1st.
 * 
 * A sender must always choose the FlowSession which is valid at the time of the sending. This could be the secondary
 * session if the primary has been superceeded by the secondary. The receiver must know the private pendent to the
 * public sessionKeys and manage multiple of these. The message contains the FlowTargetSession's signature timestamp.
 * The receiver must identify the private key to use to decrypt the message by locating the used public key given the
 * message's sent timestamp and the flow target session given the signature in the message.
 * 
 * Since the ServiceProvider only stores the current FlowTargetSession and doesn't keep a history, the ServiceProvider
 * of the receiver cannot check if a message's session signature is known ( if it doesn't match the current one ). The
 * receiver anyway has to "remember" the private session information and it's signatures to perform the validation. So
 * ServiceProviders just check for valid channel authorizations on messages relayed, but not flow target session.
 * 
 * @author Peter Klauser
 * 
 */
@Embeddable
public class FlowTargetSession implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -1L;

	private FlowSession primary;

	private FlowSession secondary;

	private AgentSignature signature;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public FlowTargetSession() {

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FlowTargetSession [");
		builder.append("primary=").append(primary);
		builder.append(", secondary=").append(secondary);
		builder.append(", signature=").append(signature);
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

	public FlowSession getPrimary() {
		return primary;
	}

	public void setPrimary(FlowSession primary) {
		this.primary = primary;
	}

	public FlowSession getSecondary() {
		return secondary;
	}

	public void setSecondary(FlowSession secondary) {
		this.secondary = secondary;
	}

	public AgentSignature getSignature() {
		return signature;
	}

	public void setSignature(AgentSignature signature) {
		this.signature = signature;
	}
}
