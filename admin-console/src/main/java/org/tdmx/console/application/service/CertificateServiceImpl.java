package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.application.domain.DnsResolverListDO;
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


public class CertificateServiceImpl implements CertificateService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(CertificateServiceImpl.class);

	private ObjectRegistry objectRegistry;
	private SearchService searchService;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public X509CertificateDO lookup(String id) {
		return objectRegistry.getX509Certificate(id);
	}

	@Override
	public List<X509CertificateDO> search(String criteria) {
		if ( StringUtils.hasText(criteria)) {
			List<X509CertificateDO> result = new ArrayList<>();
			Set<X509CertificateDO> found = searchService.search(DomainObjectType.X509Certificate, criteria);
			for( DomainObject o : found ) {
				result.add((X509CertificateDO)o );
			}
			return result;
		}
		return objectRegistry.getX509Certificates();
	}

	@Override
	public OperationError create(X509CertificateDO certificate) {
		//check it doesn't exist already ( SHA1 hashCode clashes theoretically possible but unlikely )
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		List<FieldError> validation = certificate.validate();
		if ( !validation.isEmpty() ) {
			return new OperationError(validation);
		}
		X509CertificateDO existing = objectRegistry.getX509Certificate(certificate.getId());
		if ( existing == null ) {
			objectRegistry.notifyAdd(certificate, holder);
			searchService.update(holder);
		} else {
			return new OperationError(ERROR.PRESENT);
		}
		return null;
	}

	@Override
	public OperationError delete(String id) {
		CertificateAuthorityDO existing = objectRegistry.getCertificateAuthority(id);
		if ( existing == null ) {
			return new OperationError(ERROR.MISSING);
		}
		// TODO deleting a X509Certificate is only possible if it is not
		// anymore referenced in Root-CA lists or CA credentials
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		objectRegistry.notifyRemove(existing, holder);
		searchService.update(holder);
		return null;
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

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

}
