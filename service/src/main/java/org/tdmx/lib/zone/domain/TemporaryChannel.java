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
import javax.persistence.TableGenerator;

import org.tdmx.core.api.v01.mrs.ws.MRS;

/**
 * An TemporaryChannel (within a Domain of a Zone) is used as a way of allowing a first time authorization of a channel
 * through the MRS.
 * 
 * The TemporaryChannel is created on by SCS createMRSSession ( TODO ) on a channel which doesn't exist.
 * 
 * The Channel is deleted on the first time a channel authorization is relayed in with
 * {@link MRS#relay(org.tdmx.core.api.v01.mrs.Relay)}.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "TemporaryChannel")
public class TemporaryChannel implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128854626282L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TempChannelIdGen")
	@TableGenerator(name = "TempChannelIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "tempChannelObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Domain domain;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "localName", column = @Column(name = "originAddress", nullable = false)),
			@AttributeOverride(name = "domainName", column = @Column(name = "originDomain", nullable = false)) })
	private ChannelOrigin origin;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "localName", column = @Column(name = "destAddress", nullable = false)),
			@AttributeOverride(name = "domainName", column = @Column(name = "destDomain", nullable = false)),
			@AttributeOverride(name = "serviceName", column = @Column(name = "destService", nullable = false)) })
	private ChannelDestination destination;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	TemporaryChannel() {
	}

	public TemporaryChannel(Domain domain, ChannelOrigin origin, ChannelDestination destination) {
		setDomain(domain);
		setOrigin(origin);
		setDestination(destination);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public boolean isSend() {
		return domain.getDomainName().equals(origin.getDomainName());
	}

	public boolean isRecv() {
		return domain.getDomainName().equals(destination.getDomainName());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TempChannel [id=");
		builder.append(id);
		builder.append(" origin=").append(origin);
		builder.append(", destination=").append(destination);
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setDomain(Domain domain) {
		this.domain = domain;
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

	public Domain getDomain() {
		return domain;
	}

	public ChannelOrigin getOrigin() {
		return origin;
	}

	private void setOrigin(ChannelOrigin origin) {
		this.origin = origin;
	}

	public ChannelDestination getDestination() {
		return destination;
	}

	private void setDestination(ChannelDestination destination) {
		this.destination = destination;
	}

}
