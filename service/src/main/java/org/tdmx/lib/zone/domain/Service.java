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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.server.ws.zas.ZASImpl;

/**
 * An Service (within a Domain) managed by a ServiceProvider.
 * 
 * A Service is created by a DomainAdministrator or ZoneAdministrator for a Domain at any time, as long as the service
 * doesn't exist already. A Service is deleted by a DomainAdministrator or ZoneAdministrator only if there are no
 * existing Channel's ( with Authorizations ).
 * 
 * Destinations which exist on the Service are deleted prior to deleting the Service, see
 * {@link ZASImpl#deleteService(org.tdmx.core.api.v01.zas.DeleteService)}. Prior deletion of ChannelAuthorizations makes
 * sure Destinations and Messages are deleted cleanly before the service is removed.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "Service")
public class Service implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_NAME_LEN = 255;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ServiceIdGen")
	@TableGenerator(name = "ServiceIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "zoneObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Domain domain;

	/**
	 * The serviceName part.
	 */
	@Column(length = MAX_NAME_LEN, nullable = false)
	private String serviceName;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	Service() {
	}

	public Service(Domain domain, String serviceName) {
		setDomain(domain);
		setServiceName(serviceName);
	}

	public Service(Domain domain, Service other) {
		setDomain(domain);
		setServiceName(other.getServiceName());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Service [id=");
		builder.append(id);
		builder.append(", serviceName=").append(serviceName);
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

	private void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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

	public String getServiceName() {
		return serviceName;
	}

}
