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
package org.tdmx.lib.console.domain;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;

public class TestDataGeneratorOutput {

	private Account account;
	private AccountZone accountZone;
	private Zone zone;

	private final List<ZACHolder> zacs = new ArrayList<>();
	private final List<Domain> domains = new ArrayList<>();
	private final List<DACHolder> dacs = new ArrayList<>();
	private final List<Address> addresses = new ArrayList<>();
	private final List<UCHolder> ucs = new ArrayList<>();
	private final List<Service> services = new ArrayList<>();

	public static class ZACHolder {
		private final AccountZoneAdministrationCredential ac;

		private final AgentCredential ag;
		private final PKIXCredential credential;

		public ZACHolder(AccountZoneAdministrationCredential ac, AgentCredential ag, PKIXCredential credential) {
			this.ac = ac;
			this.ag = ag;
			this.credential = credential;
		}

		public AccountZoneAdministrationCredential getAc() {
			return ac;
		}

		public AgentCredential getAg() {
			return ag;
		}

		public PKIXCredential getCredential() {
			return credential;
		}

	}

	public static class DACHolder {
		private final String domainName;

		private final AgentCredential ag;
		private final PKIXCredential credential;

		public DACHolder(String domainName, AgentCredential ag, PKIXCredential credential) {
			this.domainName = domainName;
			this.ag = ag;
			this.credential = credential;
		}

		public String getDomainName() {
			return domainName;
		}

		public AgentCredential getAg() {
			return ag;
		}

		public PKIXCredential getCredential() {
			return credential;
		}

	}

	public static class UCHolder {
		private final String domainName;
		private final String localName;

		private final AgentCredential ag;
		private final PKIXCredential credential;

		public UCHolder(String domainName, String localName, AgentCredential ag, PKIXCredential credential) {
			this.domainName = domainName;
			this.localName = localName;
			this.ag = ag;
			this.credential = credential;
		}

		public String getDomainName() {
			return domainName;
		}

		public String getLocalName() {
			return localName;
		}

		public AgentCredential getAg() {
			return ag;
		}

		public PKIXCredential getCredential() {
			return credential;
		}

	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public AccountZone getAccountZone() {
		return accountZone;
	}

	public void setAccountZone(AccountZone accountZone) {
		this.accountZone = accountZone;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public List<ZACHolder> getZacs() {
		return zacs;
	}

	public List<Domain> getDomains() {
		return domains;
	}

	public List<DACHolder> getDacs() {
		return dacs;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public List<UCHolder> getUcs() {
		return ucs;
	}

	public List<Service> getServices() {
		return services;
	}

}
