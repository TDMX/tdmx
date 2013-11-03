package org.tdmx.console.application.service;

import java.security.cert.X509Certificate;

import org.tdmx.console.application.domain.CertificateDO;

public interface CertificateService {

	public CertificateDO lookup( X509Certificate[] certificateChain );

}
