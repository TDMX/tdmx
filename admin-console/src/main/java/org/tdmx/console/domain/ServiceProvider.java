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
package org.tdmx.console.domain;

import java.io.Serializable;

public class ServiceProvider implements Serializable {

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String subjectIdentifier;
	private Integer version;

	private String mrsHostname;
	private Integer mrsPort;
	private ConnectionTestResult mrsStatus;
	private String mrsProxyId;

	private String masHostname;
	private Integer masPort;
	private ConnectionTestResult masStatus;
	private String masProxyId;

	private String mosHostname;
	private Integer mosPort;
	private ConnectionTestResult mosStatus;
	private String mosProxyId;

	private String mdsHostname;
	private Integer mdsPort;
	private ConnectionTestResult mdsStatus;
	private String mdsProxyId;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ServiceProvider(org.tdmx.console.application.domain.ServiceProviderDO o) {
		this.id = o.getId();

	}

	public ServiceProvider() {
	}

	public org.tdmx.console.application.domain.ServiceProviderDO domain() {
		org.tdmx.console.application.domain.ServiceProviderDO o = new org.tdmx.console.application.domain.ServiceProviderDO();
		o.setId(getId());
		return o;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getMrsHostname() {
		return mrsHostname;
	}

	public void setMrsHostname(String mrsHostname) {
		this.mrsHostname = mrsHostname;
	}

	public Integer getMrsPort() {
		return mrsPort;
	}

	public void setMrsPort(Integer mrsPort) {
		this.mrsPort = mrsPort;
	}

	public String getMrsProxyId() {
		return mrsProxyId;
	}

	public void setMrsProxyId(String mrsProxyId) {
		this.mrsProxyId = mrsProxyId;
	}

	public String getMasHostname() {
		return masHostname;
	}

	public void setMasHostname(String masHostname) {
		this.masHostname = masHostname;
	}

	public Integer getMasPort() {
		return masPort;
	}

	public void setMasPort(Integer masPort) {
		this.masPort = masPort;
	}

	public String getMasProxyId() {
		return masProxyId;
	}

	public void setMasProxyId(String masProxyId) {
		this.masProxyId = masProxyId;
	}

	public String getMosHostname() {
		return mosHostname;
	}

	public void setMosHostname(String mosHostname) {
		this.mosHostname = mosHostname;
	}

	public Integer getMosPort() {
		return mosPort;
	}

	public void setMosPort(Integer mosPort) {
		this.mosPort = mosPort;
	}

	public String getMosProxyId() {
		return mosProxyId;
	}

	public void setMosProxyId(String mosProxyId) {
		this.mosProxyId = mosProxyId;
	}

	public String getMdsHostname() {
		return mdsHostname;
	}

	public void setMdsHostname(String mdsHostname) {
		this.mdsHostname = mdsHostname;
	}

	public Integer getMdsPort() {
		return mdsPort;
	}

	public void setMdsPort(Integer mdsPort) {
		this.mdsPort = mdsPort;
	}

	public String getMdsProxyId() {
		return mdsProxyId;
	}

	public void setMdsProxyId(String mdsProxyId) {
		this.mdsProxyId = mdsProxyId;
	}

	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}

	public ConnectionTestResult getMrsStatus() {
		return mrsStatus;
	}

	public ConnectionTestResult getMasStatus() {
		return masStatus;
	}

	public ConnectionTestResult getMosStatus() {
		return mosStatus;
	}

	public ConnectionTestResult getMdsStatus() {
		return mdsStatus;
	}

}
