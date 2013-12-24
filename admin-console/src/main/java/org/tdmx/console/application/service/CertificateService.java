package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.X509CertificateDO;
import org.tdmx.console.domain.validation.OperationError;

public interface CertificateService {

	public X509CertificateDO lookup( String id );
	public List<X509CertificateDO> search( String criteria );
	
	public OperationError create(X509CertificateDO object);
	public OperationError delete( String id );
	
}
