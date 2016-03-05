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
import org.tdmx.core.system.dns.DnsUtils;
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
import org.tdmx.lib.zone.domain.AgentCredentialDescriptor;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.AgentCredentialStatus;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.DestinationSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.TemporaryChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.domain.ZoneFacade;
import org.tdmx.lib.zone.service.AddressService;
import org.tdmx.lib.zone.service.AgentCredentialFactory;
import org.tdmx.lib.zone.service.AgentCredentialService;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DestinationService;
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
	private AgentCredentialFactory agentCredentialFactory;

	private ChannelService channelService;
	private DestinationService destinationService;

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
				zac = TestCredentialGenerator.createZAC(input.getZoneApex(), 10, i + 1);

				// create the AccountZoneAdministrationCredential in ControlDB
				AccountZoneAdministrationCredential zAC = createAccountZoneAdministrationCredential(
						result.getAccount().getAccountId(), zac);

				// create the AgentCredential for the ZAC in ZoneDB
				AgentCredential zA = createAgentCredential(zone, null, null, zac);
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
					String subdomain = DnsUtils.getSubdomain(domain.getDomainName(), zone.getZoneApex());
					dac = TestCredentialGenerator.createDAC(zac, subdomain, 5, di + 1);

					// create the AgentCredential for the ZAC in ZoneDB
					AgentCredential ac = createAgentCredential(zone, domain, null, dac);
					dh.getDacs().add(new DACHolder(ac, dac));
				}

				// per domain Service
				for (int si = 0; si < input.getNumServicesPerDomain(); si++) {
					Service svc = createService(domain);
					ServiceHolder sh = new ServiceHolder(svc);
					dh.getServices().add(sh);
				}

				// per domain Address
				for (int si = 0; si < input.getNumAddressesPerDomain(); si++) {
					Address add = createAddress(domain);
					AddressHolder ah = new AddressHolder(add);
					dh.getAddresses().add(ah);

					// per domain Address UC
					for (int ui = 0; dac != null && ui < input.getNumUsersPerAddress(); ui++) {
						// create the UC
						PKIXCredential uc = TestCredentialGenerator.createUC(dac, add.getLocalName(), 2, ui + 1);

						// create the AgentCredential for the ZAC in ZoneDB
						AgentCredential ac = createAgentCredential(zone, domain, add, uc);
						ah.getUcs().add(new UCHolder(add.getDomain().getDomainName(), add.getLocalName(), ac, uc));
					}
				}
			}
			createChannelAuthorizations(result);

		} finally {
			zonePartitionIdProvider.clearPartitionId();
		}

		// create

		return result;
	}

	@Override
	public void tearDown(TestDataGeneratorInput input, TestDataGeneratorOutput output) {
		if (output == null) {
			return;
		}
		tearDown(output.getAccountZone(), output.getZone());

		// remove Account
		if (input.getAccount() == null && output.getAccount() != null) {
			deleteAccount(output.getAccount());
		}
	}

	private void createChannelAuthorizations(TestDataGeneratorOutput result) {
		// we create channelauthorizations between all the addresses of the 1st domain and the 2nd domain if there
		// is more than
		// one domain, else between all the addresses of the only domain.
		DomainHolder fromDomain = result.getDomains().get(0);
		if (fromDomain.getDacs().isEmpty()) {
			return;
		}
		DACHolder fromDac = fromDomain.getDacs().get(0);

		DomainHolder toDomain = result.getDomains().size() > 1 ? result.getDomains().get(1) : fromDomain;
		DACHolder toDac = toDomain.getDacs().get(0);
		if (toDomain.getDacs().isEmpty()) {
			return;
		}
		for (AddressHolder fromAddressHolder : fromDomain.getAddresses()) {
			Address from = fromAddressHolder.getAddress();
			for (ServiceHolder serviceHolder : toDomain.getServices()) {
				Service service = serviceHolder.getService();
				for (AddressHolder toAddressHolder : toDomain.getAddresses()) {
					Address to = toAddressHolder.getAddress();

					ChannelOrigin co = ZoneFacade.createChannelOrigin(from.getLocalName(),
							from.getDomain().getDomainName(), "SP");
					ChannelDestination cd = ZoneFacade.createChannelDestination(to.getLocalName(),
							to.getDomain().getDomainName(), service.getServiceName());

					Channel sendChannel = null;
					Channel recvChannel = null;

					// if both domains are the same we merge the 2 auths into one.
					if (from.getDomain().getDomainName().equals(to.getDomain().getDomainName())) {
						ChannelAuthorization sendRecvCa = ZoneFacade.createSendRecvChannelAuthorization(
								fromDomain.getDomain(), fromDac.getCredential(), fromDac.getAg(), co, cd);
						channelService.create(sendRecvCa.getChannel());
						fromDomain.getAuths().add(sendRecvCa);
						sendChannel = sendRecvCa.getChannel();
						recvChannel = sendRecvCa.getChannel();
					} else {
						ChannelAuthorization sendCa = ZoneFacade.createSendChannelAuthorization(fromDomain.getDomain(),
								fromDac.getCredential(), fromDac.getAg(), co, cd);
						ChannelAuthorization recvCa = ZoneFacade.createRecvChannelAuthorization(toDomain.getDomain(),
								toDac.getCredential(), toDac.getAg(), co, cd);

						channelService.create(sendCa.getChannel());
						fromDomain.getAuths().add(sendCa);
						sendChannel = sendCa.getChannel();
						channelService.create(recvCa.getChannel());
						toDomain.getAuths().add(recvCa);
						recvChannel = recvCa.getChannel();
					}

					for (UCHolder targetUser : toAddressHolder.getUcs()) {
						Destination ft = ZoneFacade.createDestination(targetUser.getCredential(), to, service);

						destinationService.createOrUpdate(ft);

						// create the CFT equivalent of the FT in the Channel, for both receiving and sending sides
						// which will also create the Flows

						channelService.setChannelDestinationSession(result.getZone(), recvChannel.getId(),
								ft.getDestinationSession());
						channelService.relayChannelDestinationSession(result.getZone(), sendChannel.getId(),
								ft.getDestinationSession());
					}

				}
			}
		}

	}

	private void tearDown(AccountZone accountZone, Zone zone) {
		if (accountZone == null) {
			throw new IllegalArgumentException("missing AccountZone");
		}
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
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

			// delete Messages
			deleteMessages(zone);

			// delete FlowTargets, depends on AgentCredentials & Services
			deleteDestinations(zone);

			// delete Channels with their ChannelAuthorizations in ZoneDB
			deleteChannels(zone);

			// delete AgentCredentials in ZoneDB
			deleteAgentCredentials(zone);

			// delete Addresses in ZoneDB
			deleteAddresses(zone);

			// delete Services in ZoneDB
			deleteServices(zone);

			// delete TemporaryChannels depends on Domains
			deleteTemporaryChannels(zone);

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
		Account a = accountService.findById(account.getId());
		if (a != null) {
			accountService.delete(a);
		}
	}

	private void deleteAccountZone(AccountZone accountZone) {
		AccountZone az = accountZoneService.findById(accountZone.getId());
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

	private void deleteDestinations(Zone zone) {
		boolean more = true;
		while (more) {
			DestinationSearchCriteria sc = new DestinationSearchCriteria(new PageSpecifier(0, 999));

			List<Destination> flowTargets = destinationService.search(zone, sc);
			for (Destination ft : flowTargets) {
				destinationService.delete(ft);
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

	private void deleteTemporaryChannels(Zone zone) {
		boolean more = true;
		while (more) {
			TemporaryChannelSearchCriteria sc = new TemporaryChannelSearchCriteria(new PageSpecifier(0, 999));

			List<TemporaryChannel> channels = channelService.search(zone, sc);
			for (TemporaryChannel c : channels) {
				channelService.delete(c);
			}
			if (channels.isEmpty()) {
				more = false;
			}
		}
	}

	private void deleteMessages(Zone zone) {
		boolean more = true;
		while (more) {
			ChannelMessageSearchCriteria sc = new ChannelMessageSearchCriteria(new PageSpecifier(0, 999));

			List<ChannelMessage> messages = channelService.search(zone, sc);
			for (ChannelMessage m : messages) {
				channelService.delete(m);
			}
			if (messages.isEmpty()) {
				more = false;
			}
		}
	}

	private void deleteChannels(Zone zone) {
		boolean more = true;
		while (more) {
			ChannelAuthorizationSearchCriteria sc = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));

			List<Channel> channels = channelService.search(zone, sc);
			for (Channel c : channels) {
				channelService.delete(c);
			}
			if (channels.isEmpty()) {
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

	private AgentCredential createAgentCredential(Zone zone, Domain domain, Address address, PKIXCredential credential)
			throws CryptoCertificateException {
		AgentCredentialDescriptor d = agentCredentialFactory.createAgentCredential(credential.getCertificateChain());
		AgentCredential ac = new AgentCredential(zone, domain, address, d);
		ac.setCredentialStatus(AgentCredentialStatus.ACTIVE);
		agentCredentialService.createOrUpdate(ac);

		return agentCredentialService.findByFingerprint(ac.getFingerprint());
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

		result.setAccountZone(az);
	}

	private Zone createZone(Long accountZoneId, String zoneApex) {
		Zone z = new Zone(accountZoneId, zoneApex);

		zoneService.createOrUpdate(z);

		return z;
	}

	private Domain createDomain(Zone zone) {
		Domain d = new Domain(zone, "domain" + getUniqueID(10) + "." + zone.getZoneApex());

		domainService.createOrUpdate(d);

		d = domainService.findByName(zone, d.getDomainName());

		return d;
	}

	private Service createService(Domain domain) {
		Service svc = new Service(domain, "svc" + getUniqueID(10));

		serviceService.createOrUpdate(svc);

		svc = serviceService.findByName(domain, svc.getServiceName());

		return svc;
	}

	private Address createAddress(Domain domain) {
		Address add = new Address(domain, "u" + getUniqueID(10));

		addressService.createOrUpdate(add);

		add = addressService.findByName(domain, add.getLocalName());

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

	public AgentCredentialFactory getAgentCredentialFactory() {
		return agentCredentialFactory;
	}

	public void setAgentCredentialFactory(AgentCredentialFactory agentCredentialFactory) {
		this.agentCredentialFactory = agentCredentialFactory;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	public DestinationService getDestinationService() {
		return destinationService;
	}

	public void setDestinationService(DestinationService destinationService) {
		this.destinationService = destinationService;
	}

}
