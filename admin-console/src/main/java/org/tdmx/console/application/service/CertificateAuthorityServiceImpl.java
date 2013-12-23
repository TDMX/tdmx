package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateAuthoritySpecifier;
import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.search.SearchService;
import org.tdmx.console.application.util.StringUtils;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError;
import org.tdmx.console.domain.validation.OperationError.ERROR;


public class CertificateAuthorityServiceImpl implements CertificateAuthorityService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	public static final String SYSTEM_ROOTCA_TRUSTED_LIST_ID = "tdmx-ca-trusted";
	public static final String SYSTEM_ROOTCA_DISTRUSTED_LIST_ID = "tdmx-ca-revoked";
	
	private ObjectRegistry objectRegistry;
	private SearchService searchService;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public CertificateAuthorityDO lookup(String id) {
		return objectRegistry.getCertificateAuthority(id);
	}

	@Override
	public List<CertificateAuthorityDO> search(String criteria) {
		if ( StringUtils.hasText(criteria)) {
			List<CertificateAuthorityDO> result = new ArrayList<>();
			Set<CertificateAuthorityDO> found = searchService.search(DomainObjectType.CertificateAuthority, criteria);
			for( DomainObject o : found ) {
				result.add((CertificateAuthorityDO)o );
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
	public OperationError create(CertificateAuthoritySpecifier request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OperationError update(CertificateAuthorityDO ca) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		List<FieldError> validation = ca.validate();
		if ( !validation.isEmpty() ) {
			return new OperationError(validation);
		}
		CertificateAuthorityDO existing = objectRegistry.getCertificateAuthority(ca.getId());
		if ( existing == null ) {
			return new OperationError(ERROR.INVALID);
		} else {
			DomainObjectFieldChanges changes = existing.merge(ca);
			if ( !changes.isEmpty() ) {
				objectRegistry.notifyModify(changes, holder);
				searchService.update(holder);

				//TODO switch tdmx-ca-trusted/distrusted ROOTCA lists
				
				//TODO audit
			}
		}
		return null;
	}

	@Override
	public OperationError delete(String id) {
		CertificateAuthorityDO existing = objectRegistry.getCertificateAuthority(id);
		if ( existing == null ) {
			return new OperationError(ERROR.MISSING);
		}
		// TODO not allowed to delete the root ca if there are any domain certificates not deleted
		// still issued using it.
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
