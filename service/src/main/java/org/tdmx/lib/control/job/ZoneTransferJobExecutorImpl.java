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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredentialType;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelAuthorizationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.FlowTargetService;
import org.tdmx.lib.zone.service.ServiceService;
import org.tdmx.lib.zone.service.ZoneService;
import org.tdmx.service.control.task.dao.ZoneTransferTask;

public class ZoneTransferJobExecutorImpl implements JobExecutor<ZoneTransferTask> {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ZoneTransferJobExecutorImpl.class);

	private AccountZoneService accountZoneService;
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	private ZoneService zoneService;
	private DomainService domainService;
	private ServiceService serviceService;
	private AddressService addressService;
	private AgentCredentialService agentCredentialService;
	private ChannelAuthorizationService channelAuthorizationService;
	private FlowTargetService flowTargetService;

	private int batchSize = 1000;

	// TODO FlowTarget transfer

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ZoneTransferJobExecutorImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String getType() {
		return ZoneTransferTask.class.getName();
	}

	@Override
	public void execute(Long id, ZoneTransferTask task) {
		AccountZone az = getAccountZoneService().findByAccountIdZoneApex(task.getAccountId(), task.getZoneApex());
		if (az == null) {
			throw new IllegalArgumentException("AccountZone not found.");
		}
		if (!id.equals(az.getJobId())) {
			throw new IllegalStateException("AccountZone#jobId mismatch.");
		}
		String oldPartitionId = az.getZonePartitionId();
		if (!StringUtils.hasText(oldPartitionId)) {
			throw new IllegalStateException("AccountZone#zonePartitionId missing.");
		}
		String newPartitionId = task.getZoneDbPartitionId();
		if (!StringUtils.hasText(newPartitionId)) {
			throw new IllegalArgumentException("zonePartitionId not provided.");
		}
		if (newPartitionId.equals(oldPartitionId)) {
			throw new IllegalArgumentException("transfer to same zone partition.");
		}
		// check that data relating to the AccountZone doesn't exist in the new partition.
		checkNoZoneExistsInTarget(az.getId(), az.getZoneApex(), newPartitionId);

		// find the oldZone to transfer to the new partitionId
		Zone oldZone = getOldZone(az.getId(), az.getZoneApex(), oldPartitionId);

		log.info("Transferring " + oldZone + " from " + oldPartitionId + " to " + newPartitionId);

		// 1) TODO disables access to the AccountZone
		// 2) wait for some quarantine time / distribute cache clear instruction for the access revocation. TODO

		// 3) transfer the Zone
		transferZone(oldZone, oldPartitionId, newPartitionId);

		// update the AccountZone to state the new partition is active.
		az.setZonePartitionId(newPartitionId);
		az.setJobId(null);
		// TODO updates the AccountZone status to allow access again
		getAccountZoneService().createOrUpdate(az);

		// delete the old data
		deleteZoneDataInSource(oldZone, oldPartitionId);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void checkNoZoneExistsInTarget(Long accountId, String zoneApex, String newPartitionId) {
		zonePartitionIdProvider.setPartitionId(newPartitionId);
		try {
			Zone z = zoneService.findByZoneApex(accountId, zoneApex);
			if (z != null) {
				throw new IllegalStateException("Zone exists in new partition.");
			}
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	private void deleteZoneDataInSource(Zone zone, String oldPartitionId) {
		zonePartitionIdProvider.setPartitionId(oldPartitionId);
		boolean more = true;
		try {
			more = true;
			while (more) {
				FlowTargetSearchCriteria casc = new FlowTargetSearchCriteria(new PageSpecifier(0, getBatchSize()));
				List<FlowTarget> flowTargets = flowTargetService.search(zone, casc);
				for (FlowTarget ft : flowTargets) {
					flowTargetService.delete(ft);
				}
				if (flowTargets.isEmpty()) {
					more = false;
				}
			}

			more = true;
			while (more) {
				ChannelAuthorizationSearchCriteria casc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0,
						getBatchSize()));
				List<ChannelAuthorization> authorizations = channelAuthorizationService.search(zone, casc);
				for (ChannelAuthorization ca : authorizations) {
					channelAuthorizationService.delete(ca);
				}
				if (authorizations.isEmpty()) {
					more = false;
				}
			}

			more = true;
			while (more) {
				AgentCredentialSearchCriteria acsc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
				List<AgentCredential> credentials = agentCredentialService.search(zone, acsc);
				for (AgentCredential s : credentials) {
					agentCredentialService.delete(s);
				}
				if (credentials.isEmpty()) {
					more = false;
				}
			}

			more = true;
			while (more) {
				AddressSearchCriteria asc = new AddressSearchCriteria(new PageSpecifier(0, getBatchSize()));
				List<Address> addresses = addressService.search(zone, asc);
				for (Address a : addresses) {
					addressService.delete(a);
				}
				if (addresses.isEmpty()) {
					more = false;
				}
			}

			more = true;
			while (more) {
				ServiceSearchCriteria ssc = new ServiceSearchCriteria(new PageSpecifier(0, getBatchSize()));
				List<Service> services = serviceService.search(zone, ssc);
				for (Service s : services) {
					serviceService.delete(s);
				}
				if (services.isEmpty()) {
					more = false;
				}
			}

			more = true;
			while (more) {
				DomainSearchCriteria dsc = new DomainSearchCriteria(new PageSpecifier(0, getBatchSize()));
				List<Domain> domains = domainService.search(zone, dsc);
				for (Domain d : domains) {
					domainService.delete(d);
				}
				if (domains.isEmpty()) {
					more = false;
				}
			}

			zoneService.delete(zone);

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	private Zone getOldZone(Long accountId, String zoneApex, String oldPartitionId) {
		Zone z = null;
		zonePartitionIdProvider.setPartitionId(oldPartitionId);
		try {
			z = zoneService.findByZoneApex(accountId, zoneApex);
			if (z == null) {
				throw new IllegalStateException("No installed Zone for " + zoneApex + " with accountId " + accountId);
			}
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
		return z;
	}

	private Zone transferZone(Zone oldZone, String oldPartitionId, String newPartitionId) {
		Zone newZone = new Zone(oldZone.getAccountZoneId(), oldZone.getZoneApex());
		// add the zone to the new partition.
		zonePartitionIdProvider.setPartitionId(newPartitionId);
		try {
			zoneService.createOrUpdate(newZone);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		transferZoneAdminAgentCredentials(oldZone, oldPartitionId, newZone, newPartitionId);

		transferDomains(oldZone, oldPartitionId, newZone, newPartitionId);
		return newZone;
	}

	private void transferChannelAuthorizations(Zone oldZone, Domain oldDomain, String oldPartitionId, Zone newZone,
			Domain newDomain, String newPartitionId) {
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(pageNo,
					getBatchSize()));
			sc.setDomainName(oldDomain.getDomainName());

			List<ChannelAuthorization> channelAuths = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				channelAuths = channelAuthorizationService.search(oldZone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			for (ChannelAuthorization oldCa : channelAuths) {
				ChannelAuthorization newCa = new ChannelAuthorization(newDomain, oldCa);

				zonePartitionIdProvider.setPartitionId(newPartitionId);
				try {

					// add the zone to the new partition.
					channelAuthorizationService.createOrUpdate(newCa);
				} finally {
					zonePartitionIdProvider.clearPartitionId();
				}
			}
			if (channelAuths.isEmpty()) {
				more = false;
			}
		}
	}

	private void transferDomains(Zone oldZone, String oldPartitionId, Zone newZone, String newPartitionId) {
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			DomainSearchCriteria sc = new DomainSearchCriteria(new PageSpecifier(pageNo, getBatchSize()));
			List<Domain> domains = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				domains = domainService.search(oldZone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			for (Domain oldDomain : domains) {
				Domain newDomain = new Domain(newZone, oldDomain);

				zonePartitionIdProvider.setPartitionId(newPartitionId);
				try {
					// add the zone to the new partition.
					domainService.createOrUpdate(newDomain);
				} finally {
					zonePartitionIdProvider.clearPartitionId();
				}

				// dacs
				transferDomainAdminAgentCredentials(oldZone, oldPartitionId, newZone, newDomain, newPartitionId);

				// services
				transferServices(oldZone, oldPartitionId, newZone, newDomain, newPartitionId);

				// addresses, nested UCs, flowtargets
				transferAddresses(oldZone, oldPartitionId, newZone, newDomain, newPartitionId);

				transferChannelAuthorizations(oldZone, oldDomain, oldPartitionId, newZone, newDomain, newPartitionId);
			}
			if (domains.isEmpty()) {
				more = false;
			}
		}
	}

	private int transferAddresses(Zone oldZone, String oldPartitionId, Zone newZone, Domain newDomain,
			String newPartitionId) {
		int numTransfer = 0;
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			AddressSearchCriteria sc = new AddressSearchCriteria(new PageSpecifier(pageNo, getBatchSize()));
			List<Address> addresses = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				addresses = addressService.search(oldZone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			for (Address a : addresses) {
				// clear the Id so we can use the detached object to create an entity in the new partition.
				Address newAddress = new Address(newDomain, a);

				zonePartitionIdProvider.setPartitionId(newPartitionId);
				try {
					// add the zone to the new partition.
					addressService.createOrUpdate(newAddress);
					numTransfer++;
				} finally {
					zonePartitionIdProvider.clearPartitionId();
				}

				transferUserAgentCredentials(oldZone, oldPartitionId, newZone, newDomain, newAddress, newPartitionId);
			}
			if (addresses.isEmpty()) {
				more = false;
			}
		}

		return numTransfer;
	}

	private void transferServices(Zone oldZone, String oldPartitionId, Zone newZone, Domain newDomain,
			String newPartitionId) {
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			ServiceSearchCriteria sc = new ServiceSearchCriteria(new PageSpecifier(pageNo, getBatchSize()));
			List<Service> services = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				services = serviceService.search(oldZone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			for (Service s : services) {
				Service newService = new Service(newDomain, s);

				zonePartitionIdProvider.setPartitionId(newPartitionId);
				try {
					// clear the Id so we can use the detached object to create an entity in the new partition.

					// add the zone to the new partition.
					serviceService.createOrUpdate(newService);
				} finally {
					zonePartitionIdProvider.clearPartitionId();
				}
			}
			if (services.isEmpty()) {
				more = false;
			}
		}
	}

	private void transferZoneAdminAgentCredentials(Zone oldZone, String oldPartitionId, Zone newZone,
			String newPartitionId) {
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(new PageSpecifier(pageNo,
					getBatchSize()));
			sc.setType(AgentCredentialType.ZAC);
			List<AgentCredential> credentials = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				credentials = agentCredentialService.search(oldZone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			for (AgentCredential a : credentials) {
				// clear the Id so we can use the detached object to create an entity in the new partition.

				AgentCredential newCredential = new AgentCredential(newZone, a);

				zonePartitionIdProvider.setPartitionId(newPartitionId);
				try {
					// add the zone to the new partition.
					agentCredentialService.createOrUpdate(newCredential);
				} finally {
					zonePartitionIdProvider.clearPartitionId();
				}
			}
			if (credentials.isEmpty()) {
				more = false;
			}
		}
	}

	private void transferDomainAdminAgentCredentials(Zone oldZone, String oldPartitionId, Zone newZone,
			Domain newDomain, String newPartitionId) {
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(new PageSpecifier(pageNo,
					getBatchSize()));
			sc.setType(AgentCredentialType.DAC);
			sc.setDomainName(newDomain.getDomainName());
			List<AgentCredential> credentials = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				credentials = agentCredentialService.search(oldZone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			for (AgentCredential a : credentials) {
				AgentCredential newCredential = new AgentCredential(newZone, newDomain, a);

				zonePartitionIdProvider.setPartitionId(newPartitionId);
				try {
					// add the zone to the new partition.
					agentCredentialService.createOrUpdate(newCredential);
				} finally {
					zonePartitionIdProvider.clearPartitionId();
				}
			}
			if (credentials.isEmpty()) {
				more = false;
			}
		}
	}

	private void transferUserAgentCredentials(Zone oldZone, String oldPartitionId, Zone newZone, Domain newDomain,
			Address newAddress, String newPartitionId) {
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(new PageSpecifier(pageNo,
					getBatchSize()));
			sc.setType(AgentCredentialType.UC);
			sc.setDomainName(newDomain.getDomainName());
			sc.setAddressName(newAddress.getLocalName());

			List<AgentCredential> credentials = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				credentials = agentCredentialService.search(oldZone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			for (AgentCredential a : credentials) {
				AgentCredential newCredential = new AgentCredential(newZone, newDomain, newAddress, a);

				zonePartitionIdProvider.setPartitionId(newPartitionId);
				try {
					// add the zone to the new partition.
					agentCredentialService.createOrUpdate(newCredential);
				} finally {
					zonePartitionIdProvider.clearPartitionId();
				}

				// transfer flow targets
				transferFlowTargets(oldZone, oldPartitionId, a, newDomain, newCredential, newPartitionId);
			}
			if (credentials.isEmpty()) {
				more = false;
			}
		}
	}

	private void transferFlowTargets(Zone oldZone, String oldPartitionId, AgentCredential oldTarget, Domain newDomain,
			AgentCredential newTarget, String newPartitionId) {
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			FlowTargetSearchCriteria sc = new FlowTargetSearchCriteria(new PageSpecifier(pageNo, getBatchSize()));
			sc.getTarget().setAgent(oldTarget);

			List<FlowTarget> flowTargets = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				flowTargets = flowTargetService.search(oldZone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			zonePartitionIdProvider.setPartitionId(newPartitionId);
			try {
				for (FlowTarget ft : flowTargets) {
					Service newService = serviceService.findByName(newDomain, ft.getService().getServiceName());

					FlowTarget newFlowTarget = new FlowTarget(newTarget, newService, ft);
					flowTargetService.createOrUpdate(newFlowTarget);
				}
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			if (flowTargets.isEmpty()) {
				more = false;
			}
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AccountZoneService getAccountZoneService() {
		return accountZoneService;
	}

	public void setAccountZoneService(AccountZoneService accountZoneService) {
		this.accountZoneService = accountZoneService;
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

	public DomainService getDomainService() {
		return domainService;
	}

	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}

	public AddressService getAddressService() {
		return addressService;
	}

	public void setAddressService(AddressService addressService) {
		this.addressService = addressService;
	}

	public ServiceService getServiceService() {
		return serviceService;
	}

	public void setServiceService(ServiceService serviceService) {
		this.serviceService = serviceService;
	}

	public AgentCredentialService getAgentCredentialService() {
		return agentCredentialService;
	}

	public void setAgentCredentialService(AgentCredentialService agentCredentialService) {
		this.agentCredentialService = agentCredentialService;
	}

	public ChannelAuthorizationService getChannelAuthorizationService() {
		return channelAuthorizationService;
	}

	public void setChannelAuthorizationService(ChannelAuthorizationService channelAuthorizationService) {
		this.channelAuthorizationService = channelAuthorizationService;
	}

	public FlowTargetService getFlowTargetService() {
		return flowTargetService;
	}

	public void setFlowTargetService(FlowTargetService flowTargetService) {
		this.flowTargetService = flowTargetService;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
