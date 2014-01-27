package org.tdmx.lib.control.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An Account describes a legal entity which has a commercial relationship with the
 * ServiceProvider.
 * 
 * @author Peter Klauser
 *
 */
@Entity
@Table(name="Account")
public class Account implements Serializable {

	public static final int MAX_ACCOUNTID_LEN = 16;
	
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@Column(length = MAX_ACCOUNTID_LEN)
	private String accountId;
	
	//TODO account fields - address / company etc.
	
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

}
