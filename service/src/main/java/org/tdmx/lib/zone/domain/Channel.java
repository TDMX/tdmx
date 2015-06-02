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
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.core.api.v01.zas.ws.ZAS;

/**
 * An Channel (within a Domain of a Zone) is used as a point to associate a ChannelAuthorization and any
 * ChannelFlowSessions.
 * 
 * The Channel is created on {@link ZAS#setChannelAuthorization(org.tdmx.core.api.v01.zas.SetChannelAuthorization)} by
 * own agents or relayed in via TODO
 * 
 * The Channel is deleted on
 * {@link ZAS#deleteChannelAuthorization(org.tdmx.core.api.v01.zas.DeleteChannelAuthorization)}.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "Channel")
public class Channel implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	// TODO "Relay" Processingstatus, relay to include any existing destination FlowTargetSessions which are valid at
	// opening time.

	// TODO manage ChannelFlowSessions as cascaded entity.

	// TODO update processing status ChannelService after successful/failed relay of its changed state

	//

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelIdGen")
	@TableGenerator(name = "ChannelIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "channelObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Domain domain;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "localName", column = @Column(name = "originAddress", nullable = false)),
			@AttributeOverride(name = "domainName", column = @Column(name = "originDomain", nullable = false)),
			@AttributeOverride(name = "serviceProvider", column = @Column(name = "originSP", nullable = false)) })
	private ChannelOrigin origin;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "localName", column = @Column(name = "destAddress", nullable = false)),
			@AttributeOverride(name = "domainName", column = @Column(name = "destDomain", nullable = false)),
			@AttributeOverride(name = "serviceName", column = @Column(name = "destService", nullable = false)),
			@AttributeOverride(name = "serviceProvider", column = @Column(name = "destSP", nullable = false)) })
	private ChannelDestination destination;

	/**
	 * The authorization owned by this Channel.
	 */
	@OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private ChannelAuthorization authorization;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ChannelFlowTarget> channelFlowTargets;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	Channel() {
	}

	public Channel(Domain domain, ChannelOrigin origin, ChannelDestination destination) {
		setDomain(domain);
		setOrigin(origin);
		setDestination(destination);
	}

	public Channel(Domain domain, Channel other) {
		setDomain(domain);
		setOrigin(other.getOrigin());
		setDestination(other.getDestination());
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

	public boolean isOpen() {
		return authorization.getSendAuthorization() != null
				&& EndpointPermissionGrant.ALLOW == authorization.getSendAuthorization().getGrant()
				&& authorization.getRecvAuthorization() != null
				&& EndpointPermissionGrant.ALLOW == authorization.getRecvAuthorization().getGrant();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelAuthorization [id=");
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

	public ChannelAuthorization getAuthorization() {
		return authorization;
	}

	public void setAuthorization(ChannelAuthorization authorization) {
		this.authorization = authorization;
	}

	public Set<ChannelFlowTarget> getChannelFlowTargets() {
		if (channelFlowTargets == null) {
			channelFlowTargets = new HashSet<>();
		}
		return channelFlowTargets;
	}

}
