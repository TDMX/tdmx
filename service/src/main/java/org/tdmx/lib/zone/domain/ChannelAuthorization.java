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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.lib.common.domain.ZoneReference;

/**
 * An ChannelAuthorization (within a Zone) managed by a ServiceProvider
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ChannelAuthorization")
public class ChannelAuthorization implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelAuthorizationIdGen")
	@TableGenerator(name = "ChannelAuthorizationIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "channelauthObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	/**
	 * The tenantId is the entityID of the AccountZone in ControlDB.
	 */
	@Column(nullable = false)
	private Long tenantId;

	@Column(length = Zone.MAX_NAME_LEN, nullable = false)
	private String zoneApex;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "localName", column = @Column(name = "originAddress")),
			@AttributeOverride(name = "domainName", column = @Column(name = "originDomain")),
			@AttributeOverride(name = "serviceProvider", column = @Column(name = "originSP")) })
	private ChannelOrigin origin;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "localName", column = @Column(name = "destAddress")),
			@AttributeOverride(name = "domainName", column = @Column(name = "destDomain")),
			@AttributeOverride(name = "serviceProvider", column = @Column(name = "destSP")) })
	private ChannelDestination destination;

	// TODO send EndpointAuthorization
	// TODO recv EndpointAuthorization
	// TODO requested send EndpointAuthorization
	// TODO requested recv EndpointAuthorization

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "highMarkBytes", column = @Column(name = "unsentHigh")),
			@AttributeOverride(name = "lowMarkBytes", column = @Column(name = "unsentLow")) })
	private FlowLimit unsentBuffer;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "highMarkBytes", column = @Column(name = "undeliveredHigh")),
			@AttributeOverride(name = "lowMarkBytes", column = @Column(name = "undeliveredLow")) })
	private FlowLimit undeliveredBuffer;

	@Embedded
	private AgentSignature signature;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	ChannelAuthorization() {
	}

	public ChannelAuthorization(ZoneReference zone) {
		this.tenantId = zone.getTenantId();
		this.zoneApex = zone.getZoneApex();
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelAuthorization [id=");
		builder.append(id);
		builder.append(" origin=").append(origin);
		builder.append(" destination=").append(destination);
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ZoneReference getZoneReference() {
		return new ZoneReference(this.tenantId, this.zoneApex);
	}

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

	public FlowLimit getUnsentBuffer() {
		return unsentBuffer;
	}

	public void setUnsentBuffer(FlowLimit unsentBuffer) {
		this.unsentBuffer = unsentBuffer;
	}

	public FlowLimit getUndeliveredBuffer() {
		return undeliveredBuffer;
	}

	public void setUndeliveredBuffer(FlowLimit undeliveredBuffer) {
		this.undeliveredBuffer = undeliveredBuffer;
	}

	public AgentSignature getSignature() {
		return signature;
	}

	public void setSignature(AgentSignature signature) {
		this.signature = signature;
	}

}
