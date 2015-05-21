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
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
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
 * 
 * @link ZAS#deleteUser(org.tdmx.core.api.v01.zas.DeleteUser)}
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
			@AttributeOverride(name = "scheme", column = @Column(name = "primaryScheme", length = FlowSession.MAX_SCHEME_LEN)),
			@AttributeOverride(name = "validFrom", column = @Column(name = "primaryValidFrom")),
			@AttributeOverride(name = "sessionKey", column = @Column(name = "primarySession", length = FlowSession.MAX_SESSION_KEY_LEN)) })
	private FlowSession primary;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "scheme", column = @Column(name = "secondaryScheme", length = FlowSession.MAX_SCHEME_LEN)),
			@AttributeOverride(name = "validFrom", column = @Column(name = "secondaryValidFrom")),
			@AttributeOverride(name = "sessionKey", column = @Column(name = "secondarySession", length = FlowSession.MAX_SESSION_KEY_LEN)) })
	private FlowSession secondary;

	@Temporal(TemporalType.TIMESTAMP)
	private Date signatureDate;

	/**
	 * The hex representation of the signature.
	 */
	@Column(length = AgentSignature.MAX_SIGNATURE_LEN)
	private String signatureValue;

	/**
	 * The signature algorithm.
	 */
	@Enumerated(EnumType.STRING)
	private SignatureAlgorithm signatureAlgorithm;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	FlowTarget() {
	}

	public FlowTarget(AgentCredential target, Service service) {
		setTarget(target);
		setService(service);
		setConcurrency(new FlowTargetConcurrency(this, service));
	}

	public FlowTarget(AgentCredential target, Service service, FlowTarget other) {
		setTarget(target);
		setService(service);
		setConcurrency(new FlowTargetConcurrency(this, other.getConcurrency()));
		setPrimary(other.getPrimary());
		setSecondary(other.getSecondary());
		setSignatureAlgorithm(other.getSignatureAlgorithm());
		setSignatureDate(other.getSignatureDate());
		setSignatureValue(other.getSignatureValue());
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
		if (primary != null) {
			builder.append(", session1=").append(primary);
		}
		if (secondary != null) {
			builder.append(", session2=").append(secondary);
		}
		if (signatureDate != null) {
			builder.append(", signatureDate=").append(signatureDate);
		}
		if (signatureAlgorithm != null) {
			builder.append(", signatureAlgorithm=").append(signatureAlgorithm);
		}
		if (signatureValue != null) {
			builder.append(", signatureValue=").append(signatureValue);
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

	public FlowTargetConcurrency getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(FlowTargetConcurrency concurrency) {
		this.concurrency = concurrency;
	}

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

	public Date getSignatureDate() {
		return signatureDate;
	}

	public void setSignatureDate(Date signatureDate) {
		this.signatureDate = signatureDate;
	}

	public String getSignatureValue() {
		return signatureValue;
	}

	public void setSignatureValue(String signatureValue) {
		this.signatureValue = signatureValue;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

}
