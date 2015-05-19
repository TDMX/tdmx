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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.core.api.v01.mds.ws.MDS;
import org.tdmx.core.api.v01.zas.ws.ZAS;

/**
 * An FlowTarget is the receiver end of a Flow which stores FlowTargetSession information at the ServiceProvider.
 * 
 * The Service of the Flow defines the default and initial concurrency limit for the receiver. The concurrency limit of
 * the flow can be individually modified by the administrator at any time later.
 * 
 * FlowTargets are created by {@link MDS#setFlowTargetSession(org.tdmx.core.api.v01.mds.SetFlowTargetSession)} and
 * deleted by {@link ZAS#deleteService(org.tdmx.core.api.v01.zas.DeleteService) and
 * {@link ZAS#deleteUser(org.tdmx.core.api.v01.zas.DeleteUser)}
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "FlowTarget")
public class FlowTarget implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -1L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "FlowTargetIdGen")
	@TableGenerator(name = "FlowTargetIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "zoneObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private AgentCredential target;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Service service;

	/**
	 * The concurrency association is "owned" ie. managed through this FlowTarget
	 */
	@OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private FlowTargetConcurrency concurrency;

	// TODO encrypted "memory" for multi recv sharing state

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "primary.scheme", column = @Column(name = "primaryScheme", length = FlowSession.MAX_SCHEME_LEN)),
			@AttributeOverride(name = "primary.validFrom", column = @Column(name = "primaryValidFrom")),
			@AttributeOverride(name = "primary.sessionKey", column = @Column(name = "primarySession", length = FlowSession.MAX_SESSION_KEY_LEN)),
			@AttributeOverride(name = "secondary.scheme", column = @Column(name = "secondaryScheme", length = FlowSession.MAX_SCHEME_LEN)),
			@AttributeOverride(name = "secondary.validFrom", column = @Column(name = "secondaryValidFrom")),
			@AttributeOverride(name = "secondary.sessionKey", column = @Column(name = "secondarySession", length = FlowSession.MAX_SESSION_KEY_LEN)),
			@AttributeOverride(name = "signature.signatureDate", column = @Column(name = "signatureDate")),
			@AttributeOverride(name = "signature.certificateChainPem", column = @Column(name = "signerPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN)),
			@AttributeOverride(name = "signature.value", column = @Column(name = "signature", length = AgentSignature.MAX_SIGNATURE_LEN)),
			@AttributeOverride(name = "signature.algorithm", column = @Column(name = "signatureAlgorithm", length = AgentSignature.MAX_SIG_ALG_LEN)) })
	private FlowTargetSession fts;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	FlowTarget() {
	}

	public FlowTarget(AgentCredential target, Service service) {
		setTarget(target);
		setService(service);
		setConcurrency(new FlowTargetConcurrency(this, service));
		setFts(null);
	}

	public FlowTarget(AgentCredential target, Service service, FlowTarget other) {
		setTarget(target);
		setService(service);
		setConcurrency(new FlowTargetConcurrency(this, other.getConcurrency()));
		setFts(other.getFts());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FlowTarget [id=");
		builder.append(id);
		builder.append(", concurrency=").append(concurrency); // one2one
		if (fts != null) {
			builder.append(", fts=").append(fts);
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

	private void setTarget(AgentCredential target) {
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

	public AgentCredential getTarget() {
		return target;
	}

	public Service getService() {
		return service;
	}

	public FlowTargetSession getFts() {
		return fts;
	}

	public void setFts(FlowTargetSession fts) {
		this.fts = fts;
	}

	public FlowTargetConcurrency getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(FlowTargetConcurrency concurrency) {
		this.concurrency = concurrency;
	}
}
