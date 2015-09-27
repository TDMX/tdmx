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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.tdmx.core.system.lang.StringUtils;

/**
 * A DnsResolverGroup is a sorted list of IP addresses of Dns servers given a name.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "DnsResolverGroup")
public class DnsResolverGroup implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final int MAX_DNSRESOLVERGROUP_LEN = 255;
	public static final int MAX_IPADDRESSLIST_LEN = 255;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "DnsResolverGroupIdGen")
	@TableGenerator(name = "DnsResolverGroupIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "dnsResolverGroupObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@Column(length = MAX_DNSRESOLVERGROUP_LEN, nullable = false)
	private String groupName;

	@Column(length = MAX_IPADDRESSLIST_LEN, nullable = false)
	/**
	 * A CSV of up to 4 IP addresses (v4 or v6) which belong to the same resolver group.
	 */
	private String ipAddressList;

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
		builder.append(", groupName=");
		builder.append(groupName);
		builder.append(", ipAddressList=");
		builder.append(ipAddressList);
		builder.append("]");
		return builder.toString();
	}

	public List<String> getIpAddresses() {
		return StringUtils.convertCsvToStringList(ipAddressList);
	}

	public void setIpAddresses(List<String> ipAddresses) {
		ipAddressList = StringUtils.convertStringListToCsv(ipAddresses);
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

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}
