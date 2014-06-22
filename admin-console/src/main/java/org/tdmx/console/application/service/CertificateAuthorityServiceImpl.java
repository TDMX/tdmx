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
package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.domain.X509CertificateDO;
import org.tdmx.console.application.search.SearchService;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError;
import org.tdmx.console.domain.validation.OperationError.ERROR;
import org.tdmx.core.system.lang.StringUtils;

public class CertificateAuthorityServiceImpl implements CertificateAuthorityService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final String SYSTEM_ROOTCA_TRUSTED_LIST_ID = "tdmx-ca-trusted";
	public static final String SYSTEM_ROOTCA_DISTRUSTED_LIST_ID = "tdmx-ca-revoked";

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(CertificateAuthorityServiceImpl.class);

	private ObjectRegistry objectRegistry;
	private SearchService searchService;
	private CertificateService certificateService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public CertificateAuthorityDO lookup(String id) {
		return objectRegistry.getCertificateAuthority(id);
	}

	@Override
	public List<CertificateAuthorityDO> search(String criteria) {
		if (StringUtils.hasText(criteria)) {
			List<CertificateAuthorityDO> result = new ArrayList<>();
			Set<CertificateAuthorityDO> found = searchService.search(DomainObjectType.CertificateAuthority, criteria);
			for (DomainObject o : found) {
				result.add((CertificateAuthorityDO) o);
			}
			return result;
		}
		return objectRegistry.getCertificateAutorities();
	}

	@Override
	public List<AsymmetricEncryptionAlgorithm> getKeyTypes() {
		List<AsymmetricEncryptionAlgorithm> keyAlgTypes = new ArrayList<>();
		keyAlgTypes.add(AsymmetricEncryptionAlgorithm.RSA2048);
		keyAlgTypes.add(AsymmetricEncryptionAlgorithm.RSA4096);
		return keyAlgTypes;
	}

	@Override
	public List<SignatureAlgorithm> getSignatureTypes(AsymmetricEncryptionAlgorithm keyType) {
		List<SignatureAlgorithm> algs = new ArrayList<>();
		algs.add(SignatureAlgorithm.SHA_256_RSA);
		algs.add(SignatureAlgorithm.SHA_384_RSA);
		algs.add(SignatureAlgorithm.SHA_512_RSA);
		return algs;
	}

	@Override
	public void create(ZoneAdministrationCredentialSpecifier request, OperationResultHolder<String> result) {
		// create self signed CA credentials from CSR
		PKIXCredential credential = null;
		try {
			credential = CredentialUtils.createZoneAdministratorCredential(request);
		} catch (CryptoCertificateException e) {
			log.warn("Unable to create CA.", e);
			result.setError(new OperationError(ERROR.SYSTEM));
			return;
		}

		PKIXCertificate caPublicCert = credential.getCertificateChain()[0];
		X509CertificateDO publicCert = new X509CertificateDO(caPublicCert);

		// check if new fingerprint is "unique" under all known certs - if not error
		if (getCertificateService().lookup(publicCert.getId()) != null) {
			log.warn("Fingerprint clash " + publicCert);
			result.setError(new OperationError(ERROR.SYSTEM));
			return;
		}

		CertificateAuthorityDO ca = new CertificateAuthorityDO();
		ca.setX509certificateId(publicCert.getId());
		ca.setActive(true);

		List<FieldError> validation = ca.validate();
		if (!validation.isEmpty()) {
			result.setError(new OperationError(validation));
			return;
		}

		// store CA publicCert in certificates
		OperationError certError = getCertificateService().create(publicCert);
		if (certError != null) {
			result.setError(certError);
			return;
		}

		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		objectRegistry.notifyAdd(ca, holder);
		searchService.update(holder);

		// TODO store CA privateKey in privateKeystore

		// TODO add tdmx-ca-trusted ROOTCA lists

		// TODO audit

		result.setResult(ca.getId());
		return;
	}

	@Override
	public OperationError update(CertificateAuthorityDO ca) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		List<FieldError> validation = ca.validate();
		if (!validation.isEmpty()) {
			return new OperationError(validation);
		}
		CertificateAuthorityDO existing = objectRegistry.getCertificateAuthority(ca.getId());
		if (existing == null) {
			return new OperationError(ERROR.INVALID);
		} else {
			DomainObjectFieldChanges changes = existing.merge(ca);
			if (!changes.isEmpty()) {
				objectRegistry.notifyModify(changes, holder);
				searchService.update(holder);

				// TODO switch tdmx-ca-trusted/distrusted ROOTCA lists

				// TODO audit
			}
		}
		return null;
	}

	@Override
	public OperationError delete(String id) {
		CertificateAuthorityDO existing = objectRegistry.getCertificateAuthority(id);
		if (existing == null) {
			return new OperationError(ERROR.MISSING);
		}
		// TODO not allowed to delete the root ca if there are any domain certificates not deleted
		// still issued using it.
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		objectRegistry.notifyRemove(existing, holder);
		searchService.update(holder);
		return null;
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

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public CertificateService getCertificateService() {
		return certificateService;
	}

	public void setCertificateService(CertificateService certificateService) {
		this.certificateService = certificateService;
	}

}
