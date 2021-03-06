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
package org.tdmx.server.rs.sas.resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.DnsResolverGroup;

@CliRepresentation(name = "DnsResolverGroup")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dnsResolverGroup")
@XmlType(name = "DnsResolverGroup")
public class DnsResolverGroupResource {

	public enum FIELD {
		ID("id"),
		GROUPNAME("groupName"),
		IPADDRESSLIST("ipAddressList"),;

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	@CliAttribute(order = 0, verbose = true)
	private Long id;
	@CliAttribute(order = 1)
	private String groupName;
	@CliAttribute(order = 2)
	private String ipAddressList;

	public static DnsResolverGroup mapTo(DnsResolverGroupResource controlJob) {
		if (controlJob == null) {
			return null;
		}

		DnsResolverGroup j = new DnsResolverGroup();
		j.setId(controlJob.getId());
		j.setGroupName(controlJob.getGroupName());
		j.setIpAddresses(StringUtils.convertCsvToStringList(controlJob.getIpAddressList()));

		return j;
	}

	public static DnsResolverGroupResource mapTo(DnsResolverGroup dnsResolverGroup) {
		if (dnsResolverGroup == null) {
			return null;
		}
		DnsResolverGroupResource dnsRG = new DnsResolverGroupResource();
		dnsRG.setId(dnsResolverGroup.getId());
		dnsRG.setGroupName(dnsResolverGroup.getGroupName());
		dnsRG.setIpAddressList(StringUtils.convertStringListToCsv(dnsResolverGroup.getIpAddresses()));
		return dnsRG;
	}

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

	public String getIpAddressList() {
		return ipAddressList;
	}

	public void setIpAddressList(String ipAddressList) {
		this.ipAddressList = ipAddressList;
	}

}
