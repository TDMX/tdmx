package org.tdmx.server.rs.sas;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.lib.control.domain.Account;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "account")
@XmlType(name = "Account")
public class AccountResource {

	private Long id;
	private String accountId;
	private String firstname;
	private String lastname;
	private String email;

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
		return a;
	}

	public static AccountResource mapTo(Account account) {
		if (account == null) {
			return null;
		}
		AccountResource a = new AccountResource();
		a.setId(account.getId());
		a.setAccountId(account.getAccountId());

		a.setEmail(account.getEmail());
		a.setFirstname(account.getFirstName());
		a.setLastname(account.getLastName());
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

}
