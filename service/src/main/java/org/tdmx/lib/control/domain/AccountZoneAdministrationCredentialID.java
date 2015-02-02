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

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * A AccountZoneAdministrationCredentialID is the primary key of the AccountZoneAdministrationCredential in the
 * Control-DB.
 * 
 * @author Peter Klauser
 * 
 */
@Embeddable
public class AccountZoneAdministrationCredentialID implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_SHA1FINGERPRINT_LEN = 64;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Column(length = Account.MAX_ACCOUNTID_LEN, nullable = false)
	private String accountId;

	@Column(length = MAX_SHA1FINGERPRINT_LEN, nullable = false)
	private String sha1fingerprint;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public AccountZoneAdministrationCredentialID() {
	}

	public AccountZoneAdministrationCredentialID(String accountId, String sha1fingerprint) {
		this.accountId = accountId;
		this.sha1fingerprint = sha1fingerprint;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return Objects.hash(accountId, sha1fingerprint);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AccountZoneAdministrationCredentialID) {
			AccountZoneAdministrationCredentialID other = (AccountZoneAdministrationCredentialID) obj;
			return Objects.equals(accountId, other.getAccountId())
					&& Objects.equals(sha1fingerprint, other.getSha1fingerprint());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentCredentialID [accountId=");
		builder.append(accountId);
		builder.append(", sha1fingerprint=");
		builder.append(sha1fingerprint);
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getSha1fingerprint() {
		return sha1fingerprint;
	}

	public void setSha1fingerprint(String sha1fingerprint) {
		this.sha1fingerprint = sha1fingerprint;
	}

}
