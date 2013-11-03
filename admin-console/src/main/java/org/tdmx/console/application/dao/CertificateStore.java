package org.tdmx.console.application.dao;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;


public interface CertificateStore {

	public static enum EntryType {
		PRIVATE_KEY, TRUSTED_CERTIFICATE
	}
	
	public Map<String, EntryType> getAliases() throws KeyStoreException;
	
	public String getAlias( X509Certificate certificate ) throws KeyStoreException;
	
	public X509Certificate[] getPrivateKeyCertificate(String alias) throws KeyStoreException;
	
	public PrivateKey getPrivateKey(String alias) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException;

	public void setPrivateKey(String alias, X509Certificate[] chain, PrivateKey privateKey ) throws KeyStoreException;

	
	public X509Certificate getTrustedCA( String alias ) throws KeyStoreException;

	public X509Certificate[] getAllTrustedCAs() throws KeyStoreException;
	
	public void setTrustedCertificate(String alias, X509Certificate cert) throws KeyStoreException;
	
	public void delete(String alias) throws KeyStoreException;

	
	public void save() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException;

	public void load() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException;
	
}
