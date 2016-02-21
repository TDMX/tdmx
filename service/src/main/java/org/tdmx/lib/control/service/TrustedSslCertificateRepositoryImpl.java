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

import static org.tdmx.core.system.lang.AssertionUtils.assertSame;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.control.dao.TrustedSslCertificateDao;
import org.tdmx.lib.control.domain.TrustedSslCertificate;
import org.tdmx.server.pcs.CacheInvalidationNotifier;

/**
 * A transactional service managing the TrustedSslCertificate information.
 * 
 * @author Peter Klauser
 * 
 */
public class TrustedSslCertificateRepositoryImpl implements TrustedSslCertificateService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(TrustedSslCertificateRepositoryImpl.class);

	private TrustedSslCertificateDao certificateDao;
	private CacheInvalidationNotifier cacheInvalidationNotifier;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	@Transactional(value = "ControlDB")
	public void createOrUpdate(TrustedSslCertificate trustStoreEntry) {
		if (trustStoreEntry.getId() != null) {
			TrustedSslCertificate storedEntry = getCertificateDao().loadById(trustStoreEntry.getId());
			if (storedEntry != null) {
				// derived fields cannot change
				assertSame("certificatePem", storedEntry.getCertificatePem(), trustStoreEntry.getCertificatePem());
				assertSame("fingerprint", storedEntry.getFingerprint(), trustStoreEntry.getFingerprint());
				assertSame("description", storedEntry.getDescription(), trustStoreEntry.getDescription());
				assertSame("validFrom", storedEntry.getValidFrom(), trustStoreEntry.getValidFrom());
				assertSame("validTo", storedEntry.getValidTo(), trustStoreEntry.getValidTo());

				getCertificateDao().merge(trustStoreEntry);
			} else {
				log.warn("Unable to find TrustedSslCertificate with id " + trustStoreEntry.getId());
			}
		} else {
			getCertificateDao().persist(trustStoreEntry);
		}
		notifyTrustStoreChanged();
	}

	@Override
	@Transactional(value = "ControlDB")
	public void delete(TrustedSslCertificate trustStoreEntry) {
		TrustedSslCertificate storedEntry = getCertificateDao().loadById(trustStoreEntry.getId());
		if (storedEntry != null) {
			getCertificateDao().delete(storedEntry);
		} else {
			log.warn("Unable to find TrustedSslCertificate to delete with id " + trustStoreEntry.getId());
		}
		notifyTrustStoreChanged();
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public TrustedSslCertificate findByFingerprint(String fingerprint) {
		return getCertificateDao().loadByFingerprint(fingerprint);
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public List<TrustedSslCertificate> findAll() {
		return getCertificateDao().loadAll();
	}

	@Override
	@Transactional(value = "ControlDB", readOnly = true)
	public TrustedSslCertificate findById(Long id) {
		return getCertificateDao().loadById(id);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	private void notifyTrustStoreChanged() {
		if (cacheInvalidationNotifier != null) {
			cacheInvalidationNotifier.cacheInvalidated(CACHE_KEY);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public TrustedSslCertificateDao getCertificateDao() {
		return certificateDao;
	}

	public void setCertificateDao(TrustedSslCertificateDao certificateDao) {
		this.certificateDao = certificateDao;
	}

	public CacheInvalidationNotifier getCacheInvalidationNotifier() {
		return cacheInvalidationNotifier;
	}

	public void setCacheInvalidationNotifier(CacheInvalidationNotifier cacheInvalidationNotifier) {
		this.cacheInvalidationNotifier = cacheInvalidationNotifier;
	}

}
