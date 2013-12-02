package org.tdmx.console.application.dao;

import java.util.List;

import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;


public interface SystemTrustStore {

	public List<TrustStoreEntry> getAllTrustedCAs() throws CryptoCertificateException;
	public List<TrustStoreEntry> getAllDistrustedTrustedCAs() throws CryptoCertificateException;

}
