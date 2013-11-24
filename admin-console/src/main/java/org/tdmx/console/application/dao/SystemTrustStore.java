package org.tdmx.console.application.dao;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;


public interface SystemTrustStore {

	public X509Certificate[] getAllTrustedCAs() throws NoSuchAlgorithmException;

}
