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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;

@CliRepresentation(name = "AccountZoneCredential")
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

		// denormalized from PEM
		TDMX_VERSION_NR("tdmxVersionNumber"),
		SERIAL_NR("serialNumber"),
		CN("cn"),
		TEL_NR("telephoneNumber"),
		EMAIL("emailAddress"),
		ORG_UNIT("orgUnit"),
		ORG("org"),
		LOCATION("location"),
		COUNTRY("country"),
		NOT_BEFORE("notBefore"),
		NOT_AFTER("notAfter"),
		KEY_ALG("keyAlgorithm"),
		SIGNATURE_ALG("signatureAlgorithm"),

		;

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
	private String zoneApex;
	@CliAttribute(order = 3)
	private String fingerprint;

	@CliAttribute(order = 5, verbose = true)
	private int tdmxVersionNumber;
	@CliAttribute(order = 6, verbose = true)
	private int serialNumber;
	@CliAttribute(order = 7)
	private String cn;
	@CliAttribute(order = 8)
	private String telephoneNumber;
	@CliAttribute(order = 9)
	private String emailAddress;
	@CliAttribute(order = 10)
	private String orgUnit;
	@CliAttribute(order = 11)
	private String org;
	@CliAttribute(order = 12)
	private String location;
	@CliAttribute(order = 13)
	private String country;
	@CliAttribute(order = 14)
	private Date notBefore;
	@CliAttribute(order = 15, verbose = true)
	private Date notAfter;
	@CliAttribute(order = 16, verbose = true)
	private String keyAlgorithm;
	@CliAttribute(order = 17, verbose = true)
	private String signatureAlgorithm;

	@CliAttribute(order = 18, verbose = true)
	private String certificatePem;

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

		a.setTdmxVersionNumber(zac.getTdmxVersionNumber());
		a.setSerialNumber(zac.getSerialNumber());
		a.setCn(zac.getCn());
		a.setTelephoneNumber(zac.getTelephoneNumber());
		a.setEmailAddress(zac.getEmailAddress());
		a.setOrgUnit(zac.getOrgUnit());
		a.setOrg(zac.getOrg());
		a.setLocation(zac.getLocation());
		a.setCountry(zac.getCountry());
		a.setNotBefore(zac.getNotBefore());
		a.setNotAfter(zac.getNotAfter());
		a.setKeyAlgorithm(EnumUtils.mapToString(zac.getKeyAlgorithm()));
		a.setSignatureAlgorithm(EnumUtils.mapToString(zac.getSignatureAlgorithm()));

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

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public int getTdmxVersionNumber() {
		return tdmxVersionNumber;
	}

	public void setTdmxVersionNumber(int tdmxVersionNumber) {
		this.tdmxVersionNumber = tdmxVersionNumber;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Date notBefore) {
		this.notBefore = notBefore;
	}

	public Date getNotAfter() {
		return notAfter;
	}

	public void setNotAfter(Date notAfter) {
		this.notAfter = notAfter;
	}

	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(String keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

}
