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
package org.tdmx.console.application.domain;

import org.tdmx.console.application.domain.X509CertificateDO.X509CertificateSO;
import org.tdmx.console.application.search.FieldDescriptor;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;

/**
 * A ServiceProvider.
 * 
 * @author Peter
 * 
 */
public class ServiceProviderDO extends AbstractDO {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final class ServiceProviderSO {
		public static final FieldDescriptor SUBJECT = new FieldDescriptor(DomainObjectType.ServiceProvider, "subject",
				FieldType.Text);
		public static final FieldDescriptor MAS_HOSTNAME = new FieldDescriptor(DomainObjectType.ServiceProvider,
				"mas.hostname", FieldType.String);
		public static final FieldDescriptor MAS_PORT = new FieldDescriptor(DomainObjectType.ServiceProvider,
				"mas.port", FieldType.Number);
		public static final FieldDescriptor MAS_PROXY = new FieldDescriptor(DomainObjectType.ServiceProvider,
				"mas.proxy", FieldType.String);
	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private String subjectIdentifier; // initially unidentified - after connection test we have it.
	private Integer version;

	// TODO rethink
	private String masHostname;
	private Integer masPort;
	private ConnectionTestResultVO masStatus;

	private String mrsHostname;
	private Integer mrsPort;
	private ConnectionTestResultVO mrsStatus;

	private String mosHostname;
	private Integer mosPort;
	private ConnectionTestResultVO mosStatus;

	private String mdsHostname;
	private Integer mdsPort;
	private ConnectionTestResultVO mdsStatus;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ServiceProviderDO() {
		super();
	}

	private ServiceProviderDO(ServiceProviderDO original) {
		this.setId(original.getId());
		this.setSubjectIdentifier(original.getSubjectIdentifier());
		// TODO
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public DomainObjectType getType() {
		return DomainObjectType.ServiceProvider;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		ctx.sof(this, X509CertificateSO.FINGERPRINT, getId());
		ctx.sof(this, ServiceProviderSO.SUBJECT, getSubjectIdentifier());
		ctx.sof(this, ServiceProviderSO.MAS_HOSTNAME, getMasHostname());
		ctx.sof(this, ServiceProviderSO.MAS_PORT, getMasPort());
		setSearchFields(ctx.getSearchFields());
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private <E extends DomainObject> ServiceProviderDO narrow(E other) {
		return (ServiceProviderDO) other;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}

	public void setSubjectIdentifier(String subjectIdentifier) {
		this.subjectIdentifier = subjectIdentifier;
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

	public ConnectionTestResultVO getMrsStatus() {
		return mrsStatus;
	}

	public void setMrsStatus(ConnectionTestResultVO mrsStatus) {
		this.mrsStatus = mrsStatus;
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

	public ConnectionTestResultVO getMasStatus() {
		return masStatus;
	}

	public void setMasStatus(ConnectionTestResultVO masStatus) {
		this.masStatus = masStatus;
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

	public ConnectionTestResultVO getMosStatus() {
		return mosStatus;
	}

	public void setMosStatus(ConnectionTestResultVO mosStatus) {
		this.mosStatus = mosStatus;
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

	public ConnectionTestResultVO getMdsStatus() {
		return mdsStatus;
	}

	public void setMdsStatus(ConnectionTestResultVO mdsStatus) {
		this.mdsStatus = mdsStatus;
	}

}
