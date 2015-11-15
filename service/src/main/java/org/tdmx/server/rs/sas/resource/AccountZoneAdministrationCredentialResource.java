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

import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "accountzonecredential")
@XmlType(name = "AccountZoneCredential")
public class AccountZoneAdministrationCredentialResource {

	public enum FIELD {
		ID("id"),
		ACCOUNTID("accountId"),
		ZONEAPEX("zoneApex"),
		FINGERPRINT("fingerprint"),
		CERTIFICATEPEM("certificatePem"),
		STATUS("status"),
		JOBID("jobId");

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	private Long id;
	private String accountId;
	private String zoneApex;
	private String fingerprint;
	private String certificatePem;
	private String status;
	private Long jobId;

	public String getCliRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("AccountZoneAdministrationCredential");
		buf.append("; ").append(id);
		buf.append("; ").append(accountId);
		buf.append("; ").append(zoneApex);
		buf.append("; ").append(fingerprint);
		buf.append("; ").append(status);
		buf.append("; ").append(jobId);
		buf.append("; ").append(certificatePem);
		return buf.toString();
	}

	public static AccountZoneAdministrationCredential mapTo(AccountZoneAdministrationCredentialResource zac) {
		if (zac == null) {
			return null;
		}
		AccountZoneAdministrationCredential a = new AccountZoneAdministrationCredential();
		a.setId(zac.getId());
		a.setAccountId(zac.getAccountId());

		a.setZoneApex(zac.getZoneApex());
		a.setFingerprint(zac.getFingerprint());
		a.setCertificateChainPem(zac.getCertificatePem());
		a.setCredentialStatus(EnumUtils.mapTo(AccountZoneAdministrationCredentialStatus.class, zac.getStatus()));
		a.setJobId(zac.getJobId());
		return a;
	}

	public static AccountZoneAdministrationCredentialResource mapFrom(AccountZoneAdministrationCredential zac) {
		if (zac == null) {
			return null;
		}
		AccountZoneAdministrationCredentialResource a = new AccountZoneAdministrationCredentialResource();
		a.setId(zac.getId());
		a.setAccountId(zac.getAccountId());

		a.setZoneApex(zac.getZoneApex());
		a.setFingerprint(zac.getFingerprint());
		a.setCertificatePem(zac.getCertificateChainPem());
		a.setStatus(EnumUtils.mapToString(zac.getCredentialStatus()));
		a.setJobId(zac.getJobId());
		return a;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long value) {
		this.id = value;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
	}

	public String getCertificatePem() {
		return certificatePem;
	}

	public void setCertificatePem(String certificatePem) {
		this.certificatePem = certificatePem;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

}
