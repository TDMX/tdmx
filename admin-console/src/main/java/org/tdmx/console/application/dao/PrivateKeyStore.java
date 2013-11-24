package org.tdmx.console.application.dao;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public interface PrivateKeyStore {

	public X509Certificate[] getPrivateKeyCertificate(String certId) throws KeyStoreException;
	
	public PrivateKey getPrivateKey(String certId) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException;

	public void setPrivateKey(String certId, X509Certificate[] chain, PrivateKey privateKey ) throws KeyStoreException;

	public void delete(String certId) throws KeyStoreException;

	public void save() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException;

	public void load() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException;
	
}
