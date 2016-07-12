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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.dao.AccountZoneAdministrationCredentialDao;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialSearchCriteria;
import org.tdmx.lib.control.domain.DomainZoneApexInfo;
import org.tdmx.server.runtime.DomainZoneResolutionService;

/**
 * Transactional CRUD Services for AccountZoneAdministrationCredential Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class AccountZoneAdministrationCredentialServiceRepositoryImpl
		implements AccountZoneAdministrationCredentialService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory
			.getLogger(AccountZoneAdministrationCredentialServiceRepositoryImpl.class);

	private AccountZoneAdministrationCredentialDao accountCredentialDao;
	private DomainZoneResolutionService domainZoneResolutionService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public PublicKeyCheckResultHolder check(String certificatePEM) {

		PublicKeyCheckResultHolder result = new PublicKeyCheckResultHolder();
		PKIXCertificate[] certChain = CertificateIOUtils.safePemToX509certs(certificatePEM);
		if (certChain == null || certChain.length == 0) {
			result.status = PublicKeyCheckStatus.CORRUPT_PEM;
			return result;
		}
		PKIXCertificate publicKey = certChain[0];
		if (publicKey.getTdmxZoneInfo() == null) {
			result.status = PublicKeyCheckStatus.NOT_TDMX;
			return result;
		}
		if (!publicKey.isTdmxZoneAdminCertificate()) {
			result.status = PublicKeyCheckStatus.NOT_ZAC;
			return result;
		}
		// after this point in the validation we always have the description of the ZAC, but there stil could be
		// problems with the time validity and the DNS trust in the cert.
		ZoneAdministrationCredentialSpecifier spec = CredentialUtils.describeZoneAdministratorCertificate(publicKey);
		result.spec = spec;

		if (spec.getNotBefore().getTimeInMillis() > System.currentTimeMillis()) {
			result.status = PublicKeyCheckStatus.NOT_YET_VALID;
		}
		if (spec.getNotAfter().getTimeInMillis() < System.currentTimeMillis()) {
			result.status = PublicKeyCheckStatus.EXPIRED;
		}
		if (result.status == null) {
			try {
				CredentialUtils.isValidZoneAdministratorCertificate(publicKey);
			} catch (CryptoCertificateException e) {
				result.status = PublicKeyCheckStatus.INVALID_SIGNATURE;
			}
		}
		// Check the DNS info matches
		DomainZoneApexInfo dnsInfo = domainZoneResolutionService.resolveDomain(spec.getZoneInfo().getZoneRoot());
		if (dnsInfo == null) {
			result.status = PublicKeyCheckStatus.DNS_TXT_RECORD_MISSING;

		} else {
			if (!spec.getZoneInfo().getZoneRoot().equals(dnsInfo.getZoneApex())) {
				result.status = PublicKeyCheckStatus.DNS_ZONEAPEX_WRONG;
			}
			if (!StringUtils.hasText(dnsInfo.getZacFingerprint())) {
				result.status = PublicKeyCheckStatus.DNS_ZAC_FINGERPRINT_MISSING;
			} else if (!publicKey.getFingerprint().equals(dnsInfo.getZacFingerprint())) {
				result.status = PublicKeyCheckStatus.DNS_ZAC_FINGERPRINT_WRONG;
			}
			result.status = PublicKeyCheckStatus.OK;

		}
		return result;
	}

	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(AccountZoneAdministrationCredential accountCredential) {
		if (accountCredential.getId() != null) {
			AccountZoneAdministrationCredential storedCredential = getAccountCredentialDao()
					.loadById(accountCredential.getId());
			if (storedCredential != null) {
				getAccountCredentialDao().merge(accountCredential);
			} else {
				log.warn("Unable to find AccountZoneAdministrationCredential with id " + accountCredential.getId());
			}
		} else {
			getAccountCredentialDao().persist(accountCredential);
		}
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(AccountZoneAdministrationCredential accountCredential) {
		AccountZoneAdministrationCredential storedAccountCredential = getAccountCredentialDao()
				.loadById(accountCredential.getId());
		if (storedAccountCredential != null) {
			getAccountCredentialDao().delete(storedAccountCredential);
		} else {
			log.warn("Unable to find AccountZoneAdministrationCredential to delete with id "
					+ accountCredential.getId());
		}
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public AccountZoneAdministrationCredential findById(Long id) {
		return getAccountCredentialDao().loadById(id);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<AccountZoneAdministrationCredential> search(
			AccountZoneAdministrationCredentialSearchCriteria criteria) {
		return getAccountCredentialDao().search(criteria);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public AccountZoneAdministrationCredential findByFingerprint(String fingerprint) {
		if (!StringUtils.hasText(fingerprint)) {
			throw new IllegalArgumentException("missing fingerprint");
		}
		AccountZoneAdministrationCredentialSearchCriteria sc = new AccountZoneAdministrationCredentialSearchCriteria(
				new PageSpecifier(0, 1));
		sc.setFingerprint(fingerprint);
		List<AccountZoneAdministrationCredential> zacs = getAccountCredentialDao().search(sc);

		return zacs.isEmpty() ? null : zacs.get(0);
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

	public AccountZoneAdministrationCredentialDao getAccountCredentialDao() {
		return accountCredentialDao;
	}

	public void setAccountCredentialDao(AccountZoneAdministrationCredentialDao accountCredentialDao) {
		this.accountCredentialDao = accountCredentialDao;
	}

	public DomainZoneResolutionService getDomainZoneResolutionService() {
		return domainZoneResolutionService;
	}

	public void setDomainZoneResolutionService(DomainZoneResolutionService domainZoneResolutionService) {
		this.domainZoneResolutionService = domainZoneResolutionService;
	}

}
