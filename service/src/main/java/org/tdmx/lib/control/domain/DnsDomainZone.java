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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.core.system.lang.StringUtils;

/**
 * A DnsDomainZone provides information (past and present) about the TDMX zone root in DNS.
 * 
 * We keep a record of the TDMX zone information along the time axis. Only maximum one record should be valid at any one
 * time.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "DnsDomainZone")
public class DnsDomainZone implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final int MAX_DOMAINNAME_LEN = 255;
	public static final int MAX_URL_LEN = 255;
	public static final int MAX_NAMESERVERS_LEN = 1000;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// TODO DB: non unique index on domainName
	@Column(length = MAX_DOMAINNAME_LEN, nullable = false)
	private String domainName;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date validFromTime;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date validUntilTime;

	@Column(length = MAX_DOMAINNAME_LEN, nullable = false)
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
		builder.append("DnsDomainZone [id=");
		builder.append(id);
		builder.append(", domainName=");
		builder.append(domainName);
		builder.append(", validFromTime=");
		builder.append(validFromTime);
		builder.append(", validUntilTime=");
		builder.append(validUntilTime);
		builder.append(", zoneApex=");
		builder.append(zoneApex);
		builder.append(", version=");
		builder.append(version);
		builder.append(", zacFingerprint=");
		builder.append(zacFingerprint);
		builder.append(", scsUrl=");
		builder.append(scsUrl);
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

	public URL getScsUrl() {
		try {
			return new URL(scsUrl);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void setScsUrl(URL scsUrl) {
		this.scsUrl = scsUrl.toString();
	}

	public String getScsHostname() {
		URL u = getScsUrl();
		if (u != null) {
			return u.getHost();
		}
		return null;
	}

	public boolean matches(DnsDomainZone other) {
		if (domainName == null) {
			if (other.domainName != null)
				return false;
		} else if (!domainName.equals(other.domainName))
			return false;
		if (nameServerList == null) {
			if (other.nameServerList != null)
				return false;
		} else if (!nameServerList.equals(other.nameServerList))
			return false;
		if (scsUrl == null) {
			if (other.scsUrl != null)
				return false;
		} else if (!scsUrl.equals(other.scsUrl))
			return false;
		if (version != other.version)
			return false;
		if (zacFingerprint == null) {
			if (other.zacFingerprint != null)
				return false;
		} else if (!zacFingerprint.equals(other.zacFingerprint))
			return false;
		if (zoneApex == null) {
			if (other.zoneApex != null)
				return false;
		} else if (!zoneApex.equals(other.zoneApex))
			return false;
		return true;
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

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getZacFingerprint() {
		return zacFingerprint;
	}

	public void setZacFingerprint(String zacFingerprint) {
		this.zacFingerprint = zacFingerprint;
	}

	public Date getValidFromTime() {
		return validFromTime;
	}

	public void setValidFromTime(Date validFromTime) {
		this.validFromTime = validFromTime;
	}

	public Date getValidUntilTime() {
		return validUntilTime;
	}

	public void setValidUntilTime(Date validUntilTime) {
		this.validUntilTime = validUntilTime;
	}

}
