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
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.DomainService;
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

	private int batchSize = 1000;

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
		ZoneReference zone = az.getZoneReference();
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
		checkNoZoneDataInTarget(zone, newPartitionId);

		log.info("Transferring " + zone + " from " + oldPartitionId + " to " + newPartitionId);

		// 1) TODO disables access to the AccountZone
		// 2) wait for some quarantine time / distribute cache clear instruction for the access revocation. TODO

		// 3) transfer the Zone
		transferZone(az.getZoneReference(), az.getZonePartitionId(), task.getZoneDbPartitionId());

		// 4) transfer the Domains
		task.setNumDomains(transferDomains(az.getZoneReference(), az.getZonePartitionId(), task.getZoneDbPartitionId()));
		// 5) transfer the Addresses
		task.setNumAddresses(transferAddresses(az.getZoneReference(), az.getZonePartitionId(),
				task.getZoneDbPartitionId()));

		// 6) transfer the Services
		task.setNumServices(transferServices(az.getZoneReference(), az.getZonePartitionId(),
				task.getZoneDbPartitionId()));

		// 7) transfer the AgentCredentials
		task.setNumAgentCredentials(transferAgentCredentials(az.getZoneReference(), az.getZonePartitionId(),
				task.getZoneDbPartitionId()));

		// update the AccountZone to state the new partition is active.
		az.setZonePartitionId(newPartitionId);
		az.setJobId(null);
		// TODO updates the AccountZone status to allow access again
		getAccountZoneService().createOrUpdate(az);

		// delete the old data
		deleteZoneDataInSource(zone, oldPartitionId);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void checkNoZoneDataInTarget(ZoneReference zone, String newPartitionId) {
		zonePartitionIdProvider.setPartitionId(newPartitionId);
		try {
			Zone z = zoneService.findByZoneApex(zone);
			if (z != null) {
				throw new IllegalStateException("Zone exists in new partition.");
			}

			DomainSearchCriteria dsc = new DomainSearchCriteria(new PageSpecifier(0, 1));
			List<Domain> domains = domainService.search(zone, dsc);
			if (!domains.isEmpty()) {
				throw new IllegalStateException("Domain exists in new partition.");
			}

			AddressSearchCriteria asc = new AddressSearchCriteria(new PageSpecifier(0, 1));
			List<Address> addresses = addressService.search(zone, asc);
			if (!addresses.isEmpty()) {
				throw new IllegalStateException("Address exists in new partition.");
			}

			ServiceSearchCriteria ssc = new ServiceSearchCriteria(new PageSpecifier(0, 1));
			List<Service> services = serviceService.search(zone, ssc);
			if (!services.isEmpty()) {
				throw new IllegalStateException("Service exists in new partition.");
			}

			AgentCredentialSearchCriteria acsc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
			List<AgentCredential> credentials = agentCredentialService.search(zone, acsc);
			if (!credentials.isEmpty()) {
				throw new IllegalStateException("AgentCredential exists in new partition.");
			}
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	private void deleteZoneDataInSource(ZoneReference zone, String oldPartitionId) {
		zonePartitionIdProvider.setPartitionId(oldPartitionId);
		boolean more = true;
		try {
			Zone z = zoneService.findByZoneApex(zone);
			zoneService.delete(z);

			more = true;
			while (more) {
				DomainSearchCriteria dsc = new DomainSearchCriteria(new PageSpecifier(0, getBatchSize()));
				List<Domain> domains = domainService.search(zone, dsc);
				if (domains.isEmpty()) {
					more = false;
				} else {
					for (Domain d : domains) {
						domainService.delete(d);
					}
				}
			}

			more = true;
			while (more) {
				AddressSearchCriteria asc = new AddressSearchCriteria(new PageSpecifier(0, getBatchSize()));
				List<Address> addresses = addressService.search(zone, asc);
				if (addresses.isEmpty()) {
					more = false;
				} else {
					for (Address a : addresses) {
						addressService.delete(a);
					}
				}
			}

			more = true;
			while (more) {
				ServiceSearchCriteria ssc = new ServiceSearchCriteria(new PageSpecifier(0, getBatchSize()));
				List<Service> services = serviceService.search(zone, ssc);
				if (services.isEmpty()) {
					more = false;
				} else {
					for (Service s : services) {
						serviceService.delete(s);
					}
				}
			}

			more = true;
			while (more) {
				AgentCredentialSearchCriteria acsc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 1));
				List<AgentCredential> credentials = agentCredentialService.search(zone, acsc);
				if (credentials.isEmpty()) {
					more = false;
				} else {
					for (AgentCredential s : credentials) {
						agentCredentialService.delete(s);
					}
				}
			}
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	private void transferZone(ZoneReference zone, String oldPartitionId, String newPartitionId) {
		Zone z = null;
		zonePartitionIdProvider.setPartitionId(oldPartitionId);
		try {
			z = zoneService.findByZoneApex(zone);
			if (z == null) {
				throw new IllegalStateException("No installed Zone for " + zone);
			}
			// clear the Id so we can use the detached object to create an entity in the new partition.
			z.setId(null);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		// add the zone to the new partition.
		zonePartitionIdProvider.setPartitionId(newPartitionId);
		try {
			zoneService.createOrUpdate(z);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
	}

	private int transferDomains(ZoneReference zone, String oldPartitionId, String newPartitionId) {
		int numTransfer = 0;
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			DomainSearchCriteria sc = new DomainSearchCriteria(new PageSpecifier(pageNo, getBatchSize()));
			List<Domain> domains = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				domains = domainService.search(zone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			zonePartitionIdProvider.setPartitionId(newPartitionId);
			try {
				if (domains.isEmpty()) {
					more = false;
				} else {
					for (Domain d : domains) {
						// clear the Id so we can use the detached object to create an entity in the new partition.
						d.setId(null);

						// add the zone to the new partition.
						domainService.createOrUpdate(d);
						numTransfer++;
					}
				}
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
		}
		return numTransfer;
	}

	private int transferAddresses(ZoneReference zone, String oldPartitionId, String newPartitionId) {
		int numTransfer = 0;
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			AddressSearchCriteria sc = new AddressSearchCriteria(new PageSpecifier(pageNo, getBatchSize()));
			List<Address> addresses = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				addresses = addressService.search(zone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			zonePartitionIdProvider.setPartitionId(newPartitionId);
			try {
				if (addresses.isEmpty()) {
					more = false;
				} else {
					for (Address a : addresses) {
						// clear the Id so we can use the detached object to create an entity in the new partition.
						a.setId(null);

						// add the zone to the new partition.
						addressService.createOrUpdate(a);
						numTransfer++;
					}
				}
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
		}
		return numTransfer;
	}

	private int transferServices(ZoneReference zone, String oldPartitionId, String newPartitionId) {
		int numTransfer = 0;
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			ServiceSearchCriteria sc = new ServiceSearchCriteria(new PageSpecifier(pageNo, getBatchSize()));
			List<Service> services = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				services = serviceService.search(zone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			zonePartitionIdProvider.setPartitionId(newPartitionId);
			try {
				if (services.isEmpty()) {
					more = false;
				} else {
					for (Service s : services) {
						// clear the Id so we can use the detached object to create an entity in the new partition.
						s.setId(null);

						// add the zone to the new partition.
						serviceService.createOrUpdate(s);
						numTransfer++;
					}
				}
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
		}
		return numTransfer;
	}

	private int transferAgentCredentials(ZoneReference zone, String oldPartitionId, String newPartitionId) {
		int numTransfer = 0;
		boolean more = true;
		for (int pageNo = 0; more; pageNo++) {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(new PageSpecifier(pageNo,
					getBatchSize()));
			List<AgentCredential> credentials = null;
			zonePartitionIdProvider.setPartitionId(oldPartitionId);
			try {
				credentials = agentCredentialService.search(zone, sc);
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
			zonePartitionIdProvider.setPartitionId(newPartitionId);
			try {
				if (credentials.isEmpty()) {
					more = false;
				} else {
					for (AgentCredential a : credentials) {
						// clear the Id so we can use the detached object to create an entity in the new partition.
						a.setId(null);

						// add the zone to the new partition.
						agentCredentialService.createOrUpdate(a);
						numTransfer++;
					}
				}
			} finally {
				zonePartitionIdProvider.clearPartitionId();
			}
		}
		return numTransfer;
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

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
