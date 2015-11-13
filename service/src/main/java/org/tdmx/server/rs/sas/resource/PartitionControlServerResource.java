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

import org.tdmx.lib.control.domain.PartitionControlServer;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "partitionControlServer")
@XmlType(name = "PartitionControlServer")
public class PartitionControlServerResource {

	public enum FIELD {
		ID("id"),
		SEGMENT("segment"),
		IPADDRESS("ipaddress"),
		PORT("port"),
		MODULO("modulo");

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
	private String segment;
	private String ipaddress;
	private int port;
	private int modulo;

	public String getCliRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("PartitionControlServer");
		buf.append("; ").append(id);
		buf.append("; ").append(segment);
		buf.append("; ").append(ipaddress);
		buf.append("; ").append(port);
		buf.append("; ").append(modulo);
		return buf.toString();
	}

	public static PartitionControlServer mapTo(PartitionControlServerResource pcs) {
		if (pcs == null) {
			return null;
		}

		PartitionControlServer p = new PartitionControlServer();
		p.setId(pcs.getId());

		p.setSegment(pcs.getSegment());
		p.setIpAddress(pcs.getIpaddress());
		p.setPort(pcs.getPort());
		p.setServerModulo(pcs.getModulo());
		return p;
	}

	public static PartitionControlServerResource mapFrom(PartitionControlServer pcs) {
		if (pcs == null) {
			return null;
		}
		PartitionControlServerResource p = new PartitionControlServerResource();
		p.setId(pcs.getId());

		p.setSegment(pcs.getSegment());
		p.setIpaddress(pcs.getIpAddress());
		p.setPort(pcs.getPort());
		p.setModulo(pcs.getServerModulo());
		return p;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getModulo() {
		return modulo;
	}

	public void setModulo(int modulo) {
		this.modulo = modulo;
	}

}
