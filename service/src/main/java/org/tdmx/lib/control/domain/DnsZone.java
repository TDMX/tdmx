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
package org.tdmx.lib.control.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.core.system.lang.StringUtils;

/**
 * A DnsZone provides information about the TDMX zone root in DNS.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "DnsZone")
public class DnsZone implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final int MAX_URL_LEN = 255;
	public static final int MAX_NAMESERVERS_LEN = 1000;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "DnsZoneIdGen")
	@TableGenerator(name = "DnsZoneIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "dnsZoneObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@Column(length = AccountZone.MAX_ZONEAPEX_LEN, nullable = false)
	private String zoneApex;

	/**
	 * The TDMX version, should be 1.
	 */
	@Column(nullable = false)
	private int version;

	/**
	 * The SHA256 fingerprint of the ZAC administrating certificate.
	 */
	@Column(length = AccountZoneAdministrationCredential.MAX_SHA256FINGERPRINT_LEN, nullable = false)
	private String zacFingerprint;

	/**
	 * The SHA256 fingerprint of the ZAC administrating certificate.
	 */
	@Column(length = MAX_URL_LEN, nullable = false)
	private String scsUrl;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date validUntilTime;

	/**
	 * The authoritative DNS NameServers of the zoneApex domain.
	 */
	@Column(length = MAX_NAMESERVERS_LEN, nullable = false)
	private String nameServerList;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DnsResolverGroup [id=");
		builder.append(id);
		builder.append(", version=");
		builder.append(version);
		builder.append(", zacFingerprint=");
		builder.append(zacFingerprint);
		builder.append(", scsUrl=");
		builder.append(scsUrl);
		builder.append(", validUntilTime=");
		builder.append(validUntilTime);
		builder.append(", nameServerList=");
		builder.append(nameServerList);
		builder.append("]");
		return builder.toString();
	}

	public List<String> getNameServerAddresses() {
		return StringUtils.convertCsvToStringList(nameServerList);
	}

	public void setNameServerAddresses(List<String> nsAddresses) {
		nameServerList = StringUtils.convertStringListToCsv(nsAddresses);
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

}
