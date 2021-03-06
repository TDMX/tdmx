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
package org.tdmx.lib.control.service;

import java.util.List;

import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialSearchCriteria;

/**
 * Management Services for a AccountZoneAdministrationCredential.
 * 
 * @author Peter
 * 
 */
public interface AccountZoneAdministrationCredentialService {

	public enum PublicKeyCheckStatus {
		CORRUPT_PEM,
		NOT_TDMX,
		NOT_ZAC,
		NOT_YET_VALID, // describes ZAC
		EXPIRED, // describes ZAC
		INVALID_SIGNATURE, // describes ZAC
		DNS_TXT_RECORD_MISSING, // describes ZAC
		DNS_ZONEAPEX_WRONG, // describes ZAC
		DNS_ZAC_FINGERPRINT_MISSING, // describes ZAC
		DNS_ZAC_FINGERPRINT_WRONG, // describes ZAC
		OK; // describes ZAC
	}

	/**
	 * Holder for a description of the ZAC and it's validation problems.
	 */
	public class PublicKeyCheckResultHolder {
		public PublicKeyCheckStatus status;
		public ZoneAdministrationCredentialSpecifier spec;
	}

	/**
	 * Check the ZAC. In order to
	 * {@link AccountZoneAdministrationCredentialService#createOrUpdate(AccountZoneAdministrationCredential)} the check
	 * should not return any status (ie. be successful).
	 * 
	 * @param certificatePEM
	 * @return
	 */
	public PublicKeyCheckResultHolder check(String certificatePEM);

	public void createOrUpdate(AccountZoneAdministrationCredential accountCredential);

	public AccountZoneAdministrationCredential findById(Long id);

	public AccountZoneAdministrationCredential findByFingerprint(String fingerprint);

	public List<AccountZoneAdministrationCredential> search(AccountZoneAdministrationCredentialSearchCriteria criteria);

	public void delete(AccountZoneAdministrationCredential accountCredential);

}
