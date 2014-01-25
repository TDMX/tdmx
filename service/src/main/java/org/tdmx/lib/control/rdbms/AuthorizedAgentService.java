package org.tdmx.lib.control.rdbms;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AuthorizationStatus;


public interface AuthorizedAgentService {
	
	public void createOrUpdate( PKIXCertificate certificate, AuthorizationStatus status );
	
	public AuthorizationStatus checkAuthorization(PKIXCertificate certificate);
		
	public void delete( PKIXCertificate certificate );
	
}
