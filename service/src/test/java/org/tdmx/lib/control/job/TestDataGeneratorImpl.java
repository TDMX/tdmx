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
import java.util.UUID;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialSearchCriteria;
import org.tdmx.lib.control.domain.AccountZoneSearchCriteria;
import org.tdmx.lib.control.domain.AccountZoneStatus;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput.DACHolder;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput.UCHolder;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput.ZACHolder;
import org.tdmx.lib.control.service.AccountService;
import org.tdmx.lib.control.service.AccountZoneAdministrationCredentialService;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.control.service.DatabasePartitionService;
import org.tdmx.lib.control.service.UniqueIdService;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
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

/**
 * Produces a complete set of test data for ControlDB and ZoneDB.
 * 
 * @author Peter
 * 
 */
public class TestDataGeneratorImpl implements TestDataGenerator {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private DatabasePartitionService databasePartitionService;
	private UniqueIdService accountIdService;
	private AccountService accountService;
	private AccountZoneService accountZoneService;
	private AccountZoneAdministrationCredentialService accountZoneAdminCredentialService;

	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	private ZoneService zoneService;
	private DomainService domainService;
	private ServiceService serviceService;
	private AddressService addressService;
	private AgentCredentialService agentCredentialService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public TestDataGeneratorImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public TestDataGeneratorOutput generate(TestDataGeneratorInput input) throws CryptoCertificateException {
		TestDataGeneratorOutput result = new TestDataGeneratorOutput();
		// create an Account in ControlDB if we are not provided one with the input data.
		if (input.getAccount() == null) {
			createAccount(result);
		} else {
			result.setAccount(input.getAccount());
		}

		// create the AccountZone in ControlDB
		createAccountZone(result.getAccount(), input.getZoneApex(), input.getZonePartitionId(), result);
		ZoneReference zone = result.getAccountZone().getZoneReference();
		try {
			zonePartitionIdProvider.setPartitionId(input.getZonePartitionId());

			Zone zE = createZone(zone);
			result.setZone(zE);

			// create ZACs
			PKIXCredential zac = null;
			for (int i = 0; i < input.getNumZACs(); i++) {
				// create the ZAC
				zac = TestCredentialGenerator.createZAC(input.getZoneApex(), 10);

				// create the AccountZoneAdministrationCredential in ControlDB
				AccountZoneAdministrationCredential zAC = createAccountZoneAdministrationCredential(result.getAccount()
						.getAccountId(), zac);

				// create the AgentCredential for the ZAC in ZoneDB
				AgentCredential zA = createAgentCredential(zone, zac);
				result.getZacs().add(new ZACHolder(zAC, zA, zac));
			}

			// domains
			for (int i = 0; i < input.getNumDomains(); i++) {
				Domain domain = createDomain(zone);
				result.getDomains().add(domain);

				PKIXCredential dac = null;
				// per domain DAC
				for (int di = 0; zac != null && di < input.getNumDACsPerDomain(); di++) {
					// create the DAC
					dac = TestCredentialGenerator.createDAC(zac, 5);

					// create the AgentCredential for the ZAC in ZoneDB
					AgentCredential ac = createAgentCredential(zone, dac);
					result.getDacs().add(new DACHolder(domain.getDomainName(), ac, dac));
				}

				// per domain Service
				for (int si = 0; si < input.getNumServicesPerDomain(); si++) {
					Service svc = createService(zone, domain.getDomainName());
					result.getServices().add(svc);

				}
				// per domain Address
				for (int si = 0; si < input.getNumAddressesPerDomain(); si++) {
					Address add = createAddress(zone, domain.getDomainName());
					result.getAddresses().add(add);

					// per domain Address UC
					for (int ui = 0; dac != null && ui < input.getNumUsersPerAddress(); ui++) {
						// create the DAC
						PKIXCredential uc = TestCredentialGenerator.createUC(dac, 2);

						// create the AgentCredential for the ZAC in ZoneDB
						AgentCredential ac = createAgentCredential(zone, uc);
						result.getUcs().add(new UCHolder(add.getDomainName(), add.getLocalName(), ac, uc));
					}
				}
			}

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		// create

		return result;
	}

	@Override
	public void tearDown(Account account) {
		boolean more = true;
		while (more) {
			AccountZoneSearchCriteria sc = new AccountZoneSearchCriteria(new PageSpecifier(0, 999));
			List<AccountZone> accountZones = accountZoneService.search(sc);
			if (!accountZones.isEmpty()) {
				for (AccountZone az : accountZones) {
					tearDown(az);
				}
			} else {
				more = false;
			}
		}

		// remove Account
		deleteAccount(account);
	}

	@Override
	public void tearDown(AccountZone accountZone) {
		if (accountZone.getId() == null) {
			throw new IllegalArgumentException("missing AccountZone#id");
		}
		if (!StringUtils.hasText(accountZone.getAccountId())) {
			throw new IllegalArgumentException("missing accountId");
		}
		if (!StringUtils.hasText(accountZone.getZoneApex())) {
			throw new IllegalArgumentException("missing zoneApex");
		}
		if (!StringUtils.hasText(accountZone.getZonePartitionId())) {
			throw new IllegalArgumentException("missing zonePartitionId");
		}
		ZoneReference zone = accountZone.getZoneReference();
		try {
			zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
			// delete AgentCredentials in ZoneDB
			deleteAgentCredentials(zone);

			// delete Addresses in ZoneDB
			deleteAddresses(zone);

			// delete Services in ZoneDB
			deleteServices(zone);

			// delete Domains in ZoneDB
			deleteDomains(zone);

			// delete Zone in ZoneDB
			deleteZone(zone);
		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}
		// delete AccountZoneAdministrationCredential
		deleteAccountZoneAdministrationCredentials(accountZone);

		// delete AccountZone
		deleteAccountZone(accountZone);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	private void deleteAccount(Account account) {
		Account a = accountService.findByAccountId(account.getAccountId());
		if (a != null) {
			accountService.delete(a);
		}
	}

	private void deleteAccountZone(AccountZone accountZone) {
		AccountZone az = accountZoneService.findByAccountIdZoneApex(accountZone.getAccountId(),
				accountZone.getZoneApex());
		if (az != null) {
			accountZoneService.delete(az);
		}
	}

	private void deleteZone(ZoneReference zone) {
		Zone z = zoneService.findByZoneApex(zone);
		if (z != null) {
			zoneService.delete(z);
		}
	}

	private void deleteDomains(ZoneReference zone) {
		boolean more = true;
		while (more) {
			DomainSearchCriteria sc = new DomainSearchCriteria(new PageSpecifier(0, 999));

			List<Domain> domains = domainService.search(zone, sc);
			if (domains.isEmpty()) {
				more = false;
			} else {
				for (Domain d : domains) {
					domainService.delete(d);
				}
			}
		}
	}

	private void deleteServices(ZoneReference zone) {
		boolean more = true;
		while (more) {
			ServiceSearchCriteria sc = new ServiceSearchCriteria(new PageSpecifier(0, 999));

			List<Service> services = serviceService.search(zone, sc);
			if (services.isEmpty()) {
				more = false;
			} else {
				for (Service s : services) {
					serviceService.delete(s);
				}
			}
		}
	}

	private void deleteAddresses(ZoneReference zone) {
		boolean more = true;
		while (more) {
			AddressSearchCriteria sc = new AddressSearchCriteria(new PageSpecifier(0, 999));

			List<Address> addresses = addressService.search(zone, sc);
			if (addresses.isEmpty()) {
				more = false;
			} else {
				for (Address a : addresses) {
					addressService.delete(a);
				}
			}
		}
	}

	private void deleteAgentCredentials(ZoneReference zone) {
		boolean more = true;
		while (more) {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 999));

			List<AgentCredential> agentcredentials = agentCredentialService.search(zone, sc);
			if (agentcredentials.isEmpty()) {
				more = false;
			} else {
				for (AgentCredential ac : agentcredentials) {
					agentCredentialService.delete(ac);
				}
			}
		}
	}

	private void deleteAccountZoneAdministrationCredentials(AccountZone az) {
		if (!StringUtils.hasText(az.getAccountId())) {
			throw new IllegalArgumentException("missing accountId");
		}
		if (!StringUtils.hasText(az.getZoneApex())) {
			throw new IllegalArgumentException("missing zoneApex");
		}
		boolean more = true;
		while (more) {
			AccountZoneAdministrationCredentialSearchCriteria sc = new AccountZoneAdministrationCredentialSearchCriteria(
					new PageSpecifier(0, 999));
			sc.setAccountId(az.getAccountId());
			List<AccountZoneAdministrationCredential> accountZoneCredentials = accountZoneAdminCredentialService
					.search(sc);
			if (accountZoneCredentials.isEmpty()) {
				more = false;
			} else {
				for (AccountZoneAdministrationCredential zac : accountZoneCredentials) {
					accountZoneAdminCredentialService.delete(zac);
				}
			}
		}
	}

	private void createAccount(TestDataGeneratorOutput result) {
		// create an account
		Account a = new Account();
		a.setAccountId(accountIdService.getNextId());
		a.setFirstName("firstName");
		a.setLastName("lastName");
		a.setEmail("email@address.com");

		accountService.createOrUpdate(a);

		a = accountService.findByAccountId(a.getAccountId());
		result.setAccount(a);
	}

	private AccountZoneAdministrationCredential createAccountZoneAdministrationCredential(String accountId,
			PKIXCredential zac) throws CryptoCertificateException {
		String pem = CertificateIOUtils.x509certToPem(zac.getPublicCert());

		AccountZoneAdministrationCredential zoneAC = new AccountZoneAdministrationCredential(accountId, pem);
		accountZoneAdminCredentialService.createOrUpdate(zoneAC);

		return accountZoneAdminCredentialService.findByFingerprint(zoneAC.getFingerprint());
	}

	private AgentCredential createAgentCredential(ZoneReference zone, PKIXCredential credential)
			throws CryptoCertificateException {
		AgentCredential ac = new AgentCredential(zone, credential.getCertificateChain());
		ac.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		agentCredentialService.createOrUpdate(ac);

		return agentCredentialService.findByFingerprint(zone, ac.getFingerprint());
	}

	private void createAccountZone(Account a, String zoneApex, String partitionId, TestDataGeneratorOutput result) {
		DatabasePartition partition = databasePartitionService.findByPartitionId(partitionId);
		if (partition == null) {
			throw new IllegalArgumentException("zonePartitionId");
		}

		AccountZone az = new AccountZone();
		az.setAccountId(a.getAccountId());
		az.setStatus(AccountZoneStatus.ACTIVE);
		az.setZoneApex(zoneApex);
		az.setSegment(partition.getSegment());
		az.setZonePartitionId(partition.getPartitionId());

		accountZoneService.createOrUpdate(az);

		az = accountZoneService.findByAccountIdZoneApex(a.getAccountId(), zoneApex);

		result.setAccountZone(az);
	}

	private Zone createZone(ZoneReference zone) {
		Zone z = new Zone(zone);

		zoneService.createOrUpdate(z);

		z = zoneService.findByZoneApex(zone);
		return z;
	}

	private Domain createDomain(ZoneReference zone) {
		Domain d = new Domain(zone);
		d.setDomainName("domain" + getUniqueID(10) + "." + zone.getZoneApex());

		domainService.createOrUpdate(d);

		d = domainService.findByDomainName(zone, d.getDomainName());

		return d;
	}

	private Service createService(ZoneReference zone, String domainName) {
		Service svc = new Service(zone);
		svc.setDomainName(domainName);
		svc.setServiceName("svc" + getUniqueID(10));

		serviceService.createOrUpdate(svc);

		svc = serviceService.findByName(zone, svc.getDomainName(), svc.getServiceName());

		return svc;
	}

	private Address createAddress(ZoneReference zone, String domainName) {
		Address add = new Address(zone);
		add.setDomainName(domainName);
		add.setLocalName("u" + getUniqueID(10));

		addressService.createOrUpdate(add);

		add = addressService.findByName(zone, add.getDomainName(), add.getLocalName());

		return add;
	}

	private String getUniqueID(int len) {
		String id = UUID.randomUUID().toString();
		return id.substring(0, len).toLowerCase();
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DatabasePartitionService getDatabasePartitionService() {
		return databasePartitionService;
	}

	public void setDatabasePartitionService(DatabasePartitionService databasePartitionService) {
		this.databasePartitionService = databasePartitionService;
	}

	public UniqueIdService getAccountIdService() {
		return accountIdService;
	}

	public void setAccountIdService(UniqueIdService accountIdService) {
		this.accountIdService = accountIdService;
	}

	public AccountService getAccountService() {
		return accountService;
	}

	public void setAccountService(AccountService accountService) {
		this.accountService = accountService;
	}

	public AccountZoneService getAccountZoneService() {
		return accountZoneService;
	}

	public void setAccountZoneService(AccountZoneService accountZoneService) {
		this.accountZoneService = accountZoneService;
	}

	public AccountZoneAdministrationCredentialService getAccountZoneAdminCredentialService() {
		return accountZoneAdminCredentialService;
	}

	public void setAccountZoneAdminCredentialService(
			AccountZoneAdministrationCredentialService accountZoneAdminCredentialService) {
		this.accountZoneAdminCredentialService = accountZoneAdminCredentialService;
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

	public ServiceService getServiceService() {
		return serviceService;
	}

	public void setServiceService(ServiceService serviceService) {
		this.serviceService = serviceService;
	}

	public AddressService getAddressService() {
		return addressService;
	}

	public void setAddressService(AddressService addressService) {
		this.addressService = addressService;
	}

	public AgentCredentialService getAgentCredentialService() {
		return agentCredentialService;
	}

	public void setAgentCredentialService(AgentCredentialService agentCredentialService) {
		this.agentCredentialService = agentCredentialService;
	}

}
