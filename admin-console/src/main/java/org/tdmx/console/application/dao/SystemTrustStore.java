package org.tdmx.console.application.dao;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;


public interface SystemTrustStore {

	public List<X509Certificate> getAllTrustedCAs() throws NoSuchAlgorithmException, KeyStoreException;

}
