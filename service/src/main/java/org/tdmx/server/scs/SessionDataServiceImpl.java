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
package org.tdmx.server.scs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;

/**
 * Handles data for SessionControlService.
 * 
 * @author Peter
 *
 */
public class SessionDataServiceImpl implements SessionDataService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SessionDataServiceImpl.class);

	private AccountZoneService accountZoneService;
	private ThreadLocalPartitionIdProvider partitionIdProvider;

	private ZoneService zoneService;
	private DomainService domainService;
	private ChannelService channelService;
	private ServiceService serviceService;
	private AgentCredentialService credentialService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public AccountZone getAccountZone(String zoneApex) {
		return accountZoneService.findByZoneApex(zoneApex);
	}

	@Override
	public AgentCredential getAgentCredential(AccountZone az, PKIXCertificate cert) {
		// check the credential used exists and is active.
		associateZoneDB(az.getZonePartitionId());
		try {
			return credentialService.findByFingerprint(cert.getFingerprint());
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public Service getService(AccountZone az, Domain domain, String serviceName) {
		// check the credential used exists and is active.
		associateZoneDB(az.getZonePartitionId());
		try {
			return serviceService.findByName(domain, serviceName);
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public Zone getZone(AccountZone az) {
		associateZoneDB(az.getZonePartitionId());
		try {
			return zoneService.findByZoneApex(az.getZoneApex());
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public Domain getDomain(AccountZone az, Zone zone, String domainName) {
		partitionIdProvider.setPartitionId(az.getZonePartitionId());
		try {
			Domain domain = domainService.findByName(zone, domainName);
			return domain;
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public ChannelAuthorization findChannelAuthorization(AccountZone az, Zone zone, Domain domain, ChannelOrigin co,
			ChannelDestination cd) {
		associateZoneDB(az.getZonePartitionId());
		try {
			return channelService.findByChannel(zone, domain, co, cd);

		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public TemporaryChannel findTemporaryChannel(AccountZone az, Zone zone, Domain domain, ChannelOrigin co,
			ChannelDestination cd) {
		associateZoneDB(az.getZonePartitionId());
		try {
			return channelService.findByTemporaryChannel(zone, domain, co, cd);

		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public TemporaryChannel createTemporaryChannel(AccountZone az, Domain domain, ChannelOrigin co,
			ChannelDestination cd) {
		associateZoneDB(az.getZonePartitionId());
		TemporaryChannel tc = new TemporaryChannel(domain, co, cd);
		try {
			// we create a TemporaryChannel
			channelService.create(tc);

		} finally {
			disassociateZoneDB();
		}
		return tc;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------
	protected void associateZoneDB(String zoneDbPartitionId) {
		partitionIdProvider.setPartitionId(zoneDbPartitionId);
	}

	protected void disassociateZoneDB() {
		partitionIdProvider.clearPartitionId();
	}

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

	public ThreadLocalPartitionIdProvider getPartitionIdProvider() {
		return partitionIdProvider;
	}

	public void setPartitionIdProvider(ThreadLocalPartitionIdProvider partitionIdProvider) {
		this.partitionIdProvider = partitionIdProvider;
	}

	public ZoneService getZoneService() {
		return zoneService;
	}

	public void setZoneService(ZoneService zoneService) {
		this.zoneService = zoneService;
	}

	public DomainService getDomainService() {
		return domainService;
	}

	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	public ServiceService getServiceService() {
		return serviceService;
	}

	public void setServiceService(ServiceService serviceService) {
		this.serviceService = serviceService;
	}

	public AgentCredentialService getCredentialService() {
		return credentialService;
	}

	public void setCredentialService(AgentCredentialService credentialService) {
		this.credentialService = credentialService;
	}

}
