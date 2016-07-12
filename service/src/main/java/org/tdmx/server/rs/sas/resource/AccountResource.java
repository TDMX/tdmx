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
package org.tdmx.server.rs.sas.resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountStatus;

@CliRepresentation(name = "Account")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "account")
@XmlType(name = "Account")
public class AccountResource {

	public enum FIELD {
		ID("id"),
		ACCOUNTID("accountId"),
		FIRSTNAME("firstname"),
		LASTNAME("lastname"),
		EMAIL("email"),
		STATUS("status");

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	@CliAttribute(order = 0, verbose = true)
	private Long id;
	@CliAttribute(order = 1)
	private String accountId;
	@CliAttribute(order = 2)
	private String firstname;
	@CliAttribute(order = 3)
	private String lastname;
	@CliAttribute(order = 4)
	private String email;
	@CliAttribute(order = 5)
	private String status;

	public static Account mapTo(AccountResource account) {
		if (account == null) {
			return null;
		}
		Account a = new Account();
		a.setId(account.getId());
		a.setAccountId(account.getAccountId());

		a.setEmail(account.getEmail());
		a.setFirstName(account.getFirstname());
		a.setLastName(account.getLastname());
		a.setStatus(EnumUtils.mapTo(AccountStatus.class, account.getStatus()));
		return a;
	}

	public static AccountResource mapFrom(Account account) {
		if (account == null) {
			return null;
		}
		AccountResource a = new AccountResource();
		a.setId(account.getId());
		a.setAccountId(account.getAccountId());

		a.setEmail(account.getEmail());
		a.setFirstname(account.getFirstName());
		a.setLastname(account.getLastName());
		a.setStatus(EnumUtils.mapToString(account.getStatus()));
		return a;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String value) {
		this.firstname = value;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String value) {
		this.lastname = value;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String value) {
		this.email = value;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
