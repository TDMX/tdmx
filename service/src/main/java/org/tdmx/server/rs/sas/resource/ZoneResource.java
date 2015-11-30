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

import org.tdmx.lib.zone.domain.Zone;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "zone")
@XmlType(name = "Zone")
public class ZoneResource {

	public enum FIELD {
		ID("id"),
		ACCOUNTZONEID("accountZoneId"),
		ZONEAPEX("zoneApex"),
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
	private Long accountZoneId;
	private String zoneApex;

	public String getCliRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("Zone");
		buf.append("; ").append(id);
		buf.append("; ").append(accountZoneId);
		buf.append("; ").append(zoneApex);
		return buf.toString();
	}

	public static ZoneResource mapFrom(Zone other) {
		if (other == null) {
			return null;
		}
		ZoneResource r = new ZoneResource();
		r.setId(other.getId());
		r.setAccountZoneId(other.getAccountZoneId());

		r.setZoneApex(other.getZoneApex());
		return r;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAccountZoneId() {
		return accountZoneId;
	}

	public void setAccountZoneId(Long accountZoneId) {
		this.accountZoneId = accountZoneId;
	}

	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
	}

}
