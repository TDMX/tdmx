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
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialSearchCriteria;
import org.tdmx.lib.control.domain.AccountZoneStatus;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput.AddressHolder;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput.DACHolder;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput.DomainHolder;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput.ServiceHolder;
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
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelAuthorizationService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.FlowTargetService;
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
	private ChannelAuthorizationService channelAuthorizationService;
	private FlowTargetService flowTargetService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public TestDataGeneratorImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public TestDataGeneratorOutput setUp(TestDataGeneratorInput input) throws CryptoCertificateException {
		TestDataGeneratorOutput result = new TestDataGeneratorOutput();
		// create an Account in ControlDB if we are not provided one with the input data.
		if (input.getAccount() == null) {
			createAccount(result);
		} else {
			result.setAccount(input.getAccount());
		}

		// create the AccountZone in ControlDB
		createAccountZone(result.getAccount(), input.getZoneApex(), input.getZonePartitionId(), result);
		try {
			zonePartitionIdProvider.setPartitionId(input.getZonePartitionId());

			Zone zone = createZone(result.getAccountZone().getId(), result.getAccountZone().getZoneApex());
			result.setZone(zone);

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
				DomainHolder dh = new DomainHolder(domain);
				result.getDomains().add(dh);

				PKIXCredential dac = null;
				// per domain DAC
				for (int di = 0; zac != null && di < input.getNumDACsPerDomain(); di++) {
					// create the DAC
					dac = TestCredentialGenerator.createDAC(zac, 5);

					// create the AgentCredential for the ZAC in ZoneDB
					AgentCredential ac = createAgentCredential(zone, dac);
					dh.getDacs().add(new DACHolder(ac, dac));
				}

				// per domain Service
				for (int si = 0; si < input.getNumServicesPerDomain(); si++) {
					Service svc = createService(zone, domain.getDomainName());
					ServiceHolder sh = new ServiceHolder(svc);
					dh.getServices().add(sh);
				}

				// per domain Address
				for (int si = 0; si < input.getNumAddressesPerDomain(); si++) {
					Address add = createAddress(zone, domain.getDomainName());
					AddressHolder ah = new AddressHolder(add);
					dh.getAddresses().add(ah);

					// per domain Address UC
					for (int ui = 0; dac != null && ui < input.getNumUsersPerAddress(); ui++) {
						// create the UC
						PKIXCredential uc = TestCredentialGenerator.createUC(dac, 2);

						// create the AgentCredential for the ZAC in ZoneDB
						AgentCredential ac = createAgentCredential(zone, uc);
						ah.getUcs().add(new UCHolder(add.getDomainName(), add.getLocalName(), ac, uc));
					}
				}
			}

			// we create channelauthorizations between all the addresses of the 1st domain and the 2nd domain if there
			// is more than
			// one domain, else between all the addresses of the only domain.
			DomainHolder fromDomain = result.getDomains().get(0);
			DACHolder fromDac = fromDomain.getDacs().get(0);
			DomainHolder toDomain = result.getDomains().size() > 1 ? result.getDomains().get(1) : fromDomain;
			DACHolder toDac = toDomain.getDacs().get(0);
			for (AddressHolder fromAddressHolder : fromDomain.getAddresses()) {
				Address from = fromAddressHolder.getAddress();
				for (ServiceHolder serviceHolder : toDomain.getServices()) {
					Service service = serviceHolder.getService();
					for (AddressHolder toAddressHolder : toDomain.getAddresses()) {
						Address to = toAddressHolder.getAddress();

						// TODO serviceprovider url from AccountZone to Zone
						ChannelOrigin co = ZoneFacade.createChannelOrigin(from.getLocalName(), from.getDomainName(),
								"SP");
						ChannelDestination cd = ZoneFacade.createChannelDestination(to.getLocalName(),
								to.getDomainName(), service.getServiceName(), "SP");

						// if both domains are the same we merge the 2 auths into one.
						if (from.getDomainName().equals(to.getDomainName())) {
							ChannelAuthorization sendRecvCa = ZoneFacade.createSendRecvChannelAuthorization(zone,
									fromDomain.getDomain(), fromDac.getCredential(), fromDac.getAg(), co, cd);
							channelAuthorizationService.createOrUpdate(sendRecvCa);
							fromDomain.getAuths().add(sendRecvCa);
						} else {
							ChannelAuthorization sendCa = ZoneFacade.createSendChannelAuthorization(zone,
									fromDomain.getDomain(), fromDac.getCredential(), fromDac.getAg(), co, cd);
							ChannelAuthorization recvCa = ZoneFacade.createRecvChannelAuthorization(zone,
									toDomain.getDomain(), toDac.getCredential(), toDac.getAg(), co, cd);

							channelAuthorizationService.createOrUpdate(sendCa);
							fromDomain.getAuths().add(sendCa);
							channelAuthorizationService.createOrUpdate(recvCa);
							toDomain.getAuths().add(recvCa);
						}

						for (UCHolder targetUser : toAddressHolder.getUcs()) {
							AgentCredential target = targetUser.getAg();

							FlowTarget ft = ZoneFacade.createFlowTarget(targetUser.getCredential(), target, service);

							flowTargetService.createOrUpdate(ft);
						}

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
	public void tearDown(TestDataGeneratorInput input, TestDataGeneratorOutput output) {
		tearDown(output.getAccountZone(), output.getZone());

		// remove Account
		if (input.getAccount() == null && output.getAccount() != null) {
			deleteAccount(output.getAccount());
		}
	}

	private void tearDown(AccountZone accountZone, Zone zone) {
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
		zonePartitionIdProvider.setPartitionId(accountZone.getZonePartitionId());
		try {

			// delete FlowTargets, depends on AgentCredentials & Services
			deleteFlowTargets(zone);

			// delete ChannelAuthorizations in ZoneDB
			deleteChannelAuthorizations(zone);

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

	private void deleteZone(Zone zone) {
		Zone z = zoneService.findById(zone.getId());
		if (z != null) {
			zoneService.delete(z);
		}
	}

	private void deleteDomains(Zone zone) {
		boolean more = true;
		while (more) {
			DomainSearchCriteria sc = new DomainSearchCriteria(new PageSpecifier(0, 999));

			List<Domain> domains = domainService.search(zone, sc);
			for (Domain d : domains) {
				domainService.delete(d);
			}
			if (domains.isEmpty()) {
				more = false;
			}
		}
	}

	private void deleteServices(Zone zone) {
		boolean more = true;
		while (more) {
			ServiceSearchCriteria sc = new ServiceSearchCriteria(new PageSpecifier(0, 999));

			List<Service> services = serviceService.search(zone, sc);
			for (Service s : services) {
				serviceService.delete(s);
			}
			if (services.isEmpty()) {
				more = false;
			}
		}
	}

	private void deleteAddresses(Zone zone) {
		boolean more = true;
		while (more) {
			AddressSearchCriteria sc = new AddressSearchCriteria(new PageSpecifier(0, 999));

			List<Address> addresses = addressService.search(zone, sc);
			for (Address a : addresses) {
				addressService.delete(a);
			}
			if (addresses.isEmpty()) {
				more = false;
			}
		}
	}

	private void deleteFlowTargets(Zone zone) {
		boolean more = true;
		while (more) {
			FlowTargetSearchCriteria sc = new FlowTargetSearchCriteria(new PageSpecifier(0, 999));

			List<FlowTarget> flowTargets = flowTargetService.search(zone, sc);
			for (FlowTarget ft : flowTargets) {
				flowTargetService.delete(ft);
			}
			if (flowTargets.isEmpty()) {
				more = false;
			}
		}
	}

	private void deleteAgentCredentials(Zone zone) {
		boolean more = true;
		while (more) {
			AgentCredentialSearchCriteria sc = new AgentCredentialSearchCriteria(new PageSpecifier(0, 999));

			List<AgentCredential> agentcredentials = agentCredentialService.search(zone, sc);
			for (AgentCredential ac : agentcredentials) {
				agentCredentialService.delete(ac);
			}
			if (agentcredentials.isEmpty()) {
				more = false;
			}
		}
	}

	private void deleteChannelAuthorizations(Zone zone) {
		boolean more = true;
		while (more) {
			ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));

			List<ChannelAuthorization> channelAuths = channelAuthorizationService.search(zone, sc);
			for (ChannelAuthorization ca : channelAuths) {
				channelAuthorizationService.delete(ca);
			}
			if (channelAuths.isEmpty()) {
				more = false;
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

	private AgentCredential createAgentCredential(Zone zone, PKIXCredential credential)
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

	private Zone createZone(Long accountZoneId, String zoneApex) {
		Zone z = new Zone(accountZoneId, zoneApex);

		zoneService.createOrUpdate(z);

		return z;
	}

	private Domain createDomain(Zone zone) {
		Domain d = new Domain(zone);
		d.setDomainName("domain" + getUniqueID(10) + "." + zone.getZoneApex());

		domainService.createOrUpdate(d);

		d = domainService.findByDomainName(zone, d.getDomainName());

		return d;
	}

	private Service createService(Zone zone, String domainName) {
		Service svc = new Service(zone);
		svc.setDomainName(domainName);
		svc.setServiceName("svc" + getUniqueID(10));

		serviceService.createOrUpdate(svc);

		svc = serviceService.findByName(zone, svc.getDomainName(), svc.getServiceName());

		return svc;
	}

	private Address createAddress(Zone zone, String domainName) {
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

}
