package org.tdmx.console.application.dao;

import java.security.cert.X509Certificate;
import java.util.List;

import org.tdmx.client.crypto.certificate.CryptoCertificateException;


public interface SystemTrustStore {

	public List<X509Certificate> getAllTrustedCAs() throws CryptoCertificateException;

}
