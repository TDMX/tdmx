package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateAuthoritySpecifier;
import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.domain.validation.OperationError;

public interface CertificateAuthorityService {

	public CertificateAuthorityDO lookup( String id );
	public List<CertificateAuthorityDO> search( String criteria );
	
	public List<AsymmetricEncryptionAlgorithm> getKeyTypes();
	public List<SignatureAlgorithm> getSignatureTypes( AsymmetricEncryptionAlgorithm keyType );
	
	public OperationError create( CertificateAuthoritySpecifier request );
	public OperationError modify( CertificateAuthorityDO ca );
	public OperationError delete( String id );
}
