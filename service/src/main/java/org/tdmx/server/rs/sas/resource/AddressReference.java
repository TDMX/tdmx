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

import org.tdmx.lib.zone.domain.Address;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "addressreference")
@XmlType(name = "AddressReference")
public class AddressReference {

	public enum FIELD {
		ID("id"),
		DOMAINREF("domainRef"),
		LOCALNAME("localName"),;

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
	private DomainReference domainRef;
	private String localName;

	@Override
	public String toString() {
		return localName + "@" + domainRef.getDomainName();
	}

	public static AddressReference referenceFrom(Address other) {
		if (other == null) {
			return null;
		}
		AddressReference r = new AddressReference();
		r.setId(other.getId());
		r.setDomainRef(DomainReference.referenceFrom(other.getDomain()));
		r.setLocalName(other.getLocalName());
		return r;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DomainReference getDomainRef() {
		return domainRef;
	}

	public void setDomainRef(DomainReference domainRef) {
		this.domainRef = domainRef;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

}
