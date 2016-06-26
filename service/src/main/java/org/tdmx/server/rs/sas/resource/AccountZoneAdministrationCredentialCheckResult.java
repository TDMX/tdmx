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
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.service.AccountZoneAdministrationCredentialService.PublicKeyCheckResultHolder;

@CliRepresentation(name = "AccountZoneCredentialCheckResult")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "accountzonecredentialcheckresult")
@XmlType(name = "AccountZoneCredentialCheckResult")
public class AccountZoneAdministrationCredentialCheckResult {

	public enum FIELD {
		STATUS("status"),
		ZAC("zac"),;

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	@CliAttribute(order = 0)
	private String status;
	@CliAttribute(order = 1)
	private AccountZoneAdministrationCredentialResource zac;

	public static AccountZoneAdministrationCredentialCheckResult mapFrom(PublicKeyCheckResultHolder result,
			String accountId, String certificatePEM) {
		if (result == null) {
			return null;
		}
		AccountZoneAdministrationCredentialCheckResult a = new AccountZoneAdministrationCredentialCheckResult();
		a.setStatus(EnumUtils.mapToString(result.status));
		if (result.spec != null) {
			AccountZoneAdministrationCredential cred = new AccountZoneAdministrationCredential(accountId,
					certificatePEM);
			a.setZac(AccountZoneAdministrationCredentialResource.mapFrom(cred));
		}
		return a;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AccountZoneAdministrationCredentialResource getZac() {
		return zac;
	}

	public void setZac(AccountZoneAdministrationCredentialResource zac) {
		this.zac = zac;
	}

}
