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

import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;

/**
 * An ChannelFlowTargetDescriptor a helper object to map between ChannelFlowTargets and FlowTargets and API classes.
 * 
 * @author Peter Klauser
 * 
 */
public class ChannelFlowTargetDescriptor {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private ChannelOrigin origin;

	private ChannelDestination destination;

	private AgentCredentialDescriptor target;

	private FlowTargetSession flowTargetSession;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ChannelFlowTargetDescriptor() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelFlowTargetDescriptor [");
		builder.append(" origin=").append(origin);
		builder.append(" destination=").append(destination);
		builder.append(" target=").append(target);
		builder.append(" fts=").append(flowTargetSession);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Get the PEM certificate chain in PKIXCertificate form, converting and caching on the first call.
	 * 
	 * @return
	 * @throws CryptoCertificateException
	 */
	public PKIXCertificate[] getTargetCertificateChain() {
		return flowTargetSession.getSignature().getCertificateChain();
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

	public ChannelOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(ChannelOrigin origin) {
		this.origin = origin;
	}

	public ChannelDestination getDestination() {
		return destination;
	}

	public void setDestination(ChannelDestination destination) {
		this.destination = destination;
	}

	public FlowTargetSession getFlowTargetSession() {
		return flowTargetSession;
	}

	public void setFlowTargetSession(FlowTargetSession flowTargetSession) {
		this.flowTargetSession = flowTargetSession;
	}

	public AgentCredentialDescriptor getTarget() {
		return target;
	}

	public void setTarget(AgentCredentialDescriptor target) {
		this.target = target;
	}

}
