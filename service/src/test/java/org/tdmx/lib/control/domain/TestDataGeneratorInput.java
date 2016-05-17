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
package org.tdmx.lib.control.domain;

import org.tdmx.lib.control.domain.Account;

public class TestDataGeneratorInput {

	private final String zoneApex;
	private final String zonePartitionId;

	/**
	 * If no account is set, one will be generated, otherwise the zone is added to the account.
	 */
	private Account account;
	private int numZACs = 1;
	private int numDomains = 1;
	private int numDACsPerDomain = 1;
	private int numAddressesPerDomain = 1;
	private int numServicesPerDomain = 1;
	private int numUsersPerAddress = 1;

	public TestDataGeneratorInput(String zoneApex, String zonePartitionId) {
		this.zoneApex = zoneApex;
		this.zonePartitionId = zonePartitionId;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getZoneApex() {
		return zoneApex;
	}

	public String getZonePartitionId() {
		return zonePartitionId;
	}

	public int getNumZACs() {
		return numZACs;
	}

	public void setNumZACs(int numZACs) {
		this.numZACs = numZACs;
	}

	public int getNumDomains() {
		return numDomains;
	}

	public void setNumDomains(int numDomains) {
		this.numDomains = numDomains;
	}

	public int getNumDACsPerDomain() {
		return numDACsPerDomain;
	}

	public void setNumDACsPerDomain(int numDACsPerDomain) {
		this.numDACsPerDomain = numDACsPerDomain;
	}

	public int getNumAddressesPerDomain() {
		return numAddressesPerDomain;
	}

	public void setNumAddressesPerDomain(int numAddressesPerDomain) {
		this.numAddressesPerDomain = numAddressesPerDomain;
	}

	public int getNumServicesPerDomain() {
		return numServicesPerDomain;
	}

	public void setNumServicesPerDomain(int numServicesPerDomain) {
		this.numServicesPerDomain = numServicesPerDomain;
	}

	public int getNumUsersPerAddress() {
		return numUsersPerAddress;
	}

	public void setNumUsersPerAddress(int numUsersPerAddress) {
		this.numUsersPerAddress = numUsersPerAddress;
	}

}
