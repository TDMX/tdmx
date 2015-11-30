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

import org.tdmx.lib.zone.domain.Domain;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "domain")
@XmlType(name = "Domain")
public class DomainResource {

	public enum FIELD {
		ID("id"),
		ZONEREF("zoneRef"),
		DOMAINNAME("domainName"),
		;

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	private Long id;
	private ZoneReference zoneRef;
	private String domainName;

	public String getCliRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("Domain");
		buf.append("; ").append(id);
		buf.append("; ").append(zoneRef);
		buf.append("; ").append(domainName);
		return buf.toString();
	}

	public static DomainResource mapFrom(Domain other) {
		if (other == null) {
			return null;
		}
		DomainResource r = new DomainResource();
		r.setId(other.getId());
		r.setZoneRef(ZoneReference.referenceFrom(other.getZone()));
		r.setDomainName(other.getDomainName());
		return r;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ZoneReference getZoneRef() {
		return zoneRef;
	}

	public void setZoneRef(ZoneReference zoneRef) {
		this.zoneRef = zoneRef;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}


}
