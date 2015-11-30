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
package org.tdmx.lib.control.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialStatus;
import org.tdmx.lib.control.service.AccountZoneAdministrationCredentialService;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.AgentCredentialValidator;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.service.control.task.dao.PropertyType;
import org.tdmx.service.control.task.dao.ZACInstallTask;

public class ZACInstallJobExecutorImpl implements JobExecutor<ZACInstallTask> {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZACInstallJobExecutorImpl.class);

	private AccountZoneService accountZoneService;
	private AccountZoneAdministrationCredentialService accountZoneAdministrationCredentialService;
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	private ZoneService zoneService;
	private AgentCredentialFactory credentialFactory;
	private AgentCredentialService credentialService;
	private AgentCredentialValidator credentialValidator;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ZACInstallJobExecutorImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String getType() {
		return ZACInstallTask.class.getName();
	}

	@Override
	public void execute(Long id, ZACInstallTask task) {
		AccountZone az = getAccountZoneService().findById(task.getAccountZoneId());
		if (az == null) {
			throw new IllegalArgumentException("AccountZone not found.");
		}
		String partitionId = az.getZonePartitionId();
		if (!StringUtils.hasText(partitionId)) {
			throw new IllegalStateException("AccountZone#zonePartitionId missing.");
		}

		if (!StringUtils.hasText(task.getFingerprint())) {
			throw new IllegalArgumentException("Fingerprint missing.");
		}
		AccountZoneAdministrationCredential azac = getAccountZoneAdministrationCredentialService().findByFingerprint(
				task.getFingerprint());
		if (azac == null) {
			throw new IllegalArgumentException("AccountZoneAdministrationCredential not found.");
		}
		if (!id.equals(azac.getJobId())) {
			throw new IllegalStateException("AccountZoneAdministrationCredential#jobId mismatch.");
		}

		// make sure the Zone is present in the ZoneDB
		zonePartitionIdProvider.setPartitionId(partitionId);
		Zone zone = null;
		try {
			zone = getZoneService().findByZoneApex(az.getZoneApex());
			if (zone == null) {
				throw new IllegalStateException("Zone missing.");
			}
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		AgentCredentialDescriptor ac = credentialFactory.createAgentCredential(azac.getCertificateChain());
		if (ac != null && AgentCredentialType.ZAC.equals(ac.getCredentialType())) {
			if (!credentialValidator.isValid(ac)) {
				azac.setCredentialStatus(AccountZoneAdministrationCredentialStatus.INVALID_ZAC);
			} else {
				// TODO DNS trust check
				boolean isDNSTrusted = true;

				if (isDNSTrusted) {
					zonePartitionIdProvider.setPartitionId(partitionId);
					try {
						AgentCredential zac = new AgentCredential(zone, ac);
						credentialService.createOrUpdate(zac);
					} finally {
						zonePartitionIdProvider.clearPartitionId();
					}
					azac.setCredentialStatus(AccountZoneAdministrationCredentialStatus.INSTALLED);
				} else {
					azac.setCredentialStatus(AccountZoneAdministrationCredentialStatus.NO_DNS_TRUST);
				}
			}

		} else {

			azac.setCredentialStatus(AccountZoneAdministrationCredentialStatus.NON_ZAC);
		}
		azac.setJobId(null);
		getAccountZoneAdministrationCredentialService().createOrUpdate(azac);

		//result property
		PropertyType status = new PropertyType();
		status.setName(JobPropertyName.ZAC_CERTIFICATE_STATUS );
		status.setValue(azac.getCredentialStatus().toString());
		task.getProperty().add(status);
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

	public AccountZoneService getAccountZoneService() {
		return accountZoneService;
	}

	public void setAccountZoneService(AccountZoneService accountZoneService) {
		this.accountZoneService = accountZoneService;
	}

	public AccountZoneAdministrationCredentialService getAccountZoneAdministrationCredentialService() {
		return accountZoneAdministrationCredentialService;
	}

	public void setAccountZoneAdministrationCredentialService(
			AccountZoneAdministrationCredentialService accountZoneAdministrationCredentialService) {
		this.accountZoneAdministrationCredentialService = accountZoneAdministrationCredentialService;
	}

	public ThreadLocalPartitionIdProvider getZonePartitionIdProvider() {
		return zonePartitionIdProvider;
	}

	public void setZonePartitionIdProvider(ThreadLocalPartitionIdProvider zonePartitionIdProvider) {
		this.zonePartitionIdProvider = zonePartitionIdProvider;
	}

	public ZoneService getZoneService() {
		return zoneService;
	}

	public void setZoneService(ZoneService zoneService) {
		this.zoneService = zoneService;
	}

	public AgentCredentialFactory getCredentialFactory() {
		return credentialFactory;
	}

	public void setCredentialFactory(AgentCredentialFactory credentialFactory) {
		this.credentialFactory = credentialFactory;
	}

	public AgentCredentialService getCredentialService() {
		return credentialService;
	}

	public void setCredentialService(AgentCredentialService credentialService) {
		this.credentialService = credentialService;
	}

	public AgentCredentialValidator getCredentialValidator() {
		return credentialValidator;
	}

	public void setCredentialValidator(AgentCredentialValidator credentialValidator) {
		this.credentialValidator = credentialValidator;
	}

}
