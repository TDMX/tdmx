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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.zas.ws.ZAS;

/**
 * An Destination is the combination of Address and Service at the receiving end's ServiceProvider.
 * 
 * Destinations are created by {@link MDS#setDestinationSession(org.tdmx.core.api.v01.mds.SetDestinationSession)} and
 * deleted by {@link ZAS#deleteService(org.tdmx.core.api.v01.zas.DeleteService) and
 * 
 * @link ZAS#deleteAddress(org.tdmx.core.api.v01.zas.DeleteAddress)}
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "Destination")
public class Destination implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Address target;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Service service;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "encryptionContextId", column = @Column(name = "dsIdentifier", length = DestinationSession.MAX_IDENTIFIER_LEN) ),
			@AttributeOverride(name = "scheme", column = @Column(name = "dsScheme", length = DestinationSession.MAX_SCHEME_LEN) ),
			@AttributeOverride(name = "sessionKey", column = @Column(name = "dsSession", length = DestinationSession.MAX_SESSION_KEY_LEN) ),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "dsSignatureDate") ),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "dsTargetPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN) ),
			@AttributeOverride(name = "signature.value", column = @Column(name = "dsSignature", length = AgentSignature.MAX_SIGNATURE_LEN) ),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "dsSignatureAlgorithm", length = AgentSignature.MAX_SIG_ALG_LEN) ) })
	private DestinationSession destinationSession;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	Destination() {
	}

	public Destination(Address target, Service service) {
		setTarget(target);
		setService(service);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Destination [id=");
		builder.append(id);
		if (destinationSession != null) {
			builder.append(", ds=").append(destinationSession);
		}
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setTarget(Address target) {
		this.target = target;
	}

	private void setService(Service service) {
		this.service = service;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Address getTarget() {
		return target;
	}

	public Service getService() {
		return service;
	}

	public DestinationSession getDestinationSession() {
		return destinationSession;
	}

	public void setDestinationSession(DestinationSession destinationSession) {
		this.destinationSession = destinationSession;
	}

}
