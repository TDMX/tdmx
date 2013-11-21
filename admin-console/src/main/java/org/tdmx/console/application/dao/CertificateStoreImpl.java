package org.tdmx.console.application.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.tdmx.console.application.util.FileUtils;

public class CertificateStoreImpl implements CertificateStore {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String filename;
	private String passphrase;
	private String keystoreType;
	
	private KeyStore keyStore;
	private boolean dirty = false;
	private AtomicInteger suffixId = new AtomicInteger();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public CertificateStoreImpl() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	@Override
	public synchronized void load() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
		byte[] contents = FileUtils.getFileContents(getFilename());
		if  ( contents == null ) {
			keyStore = KeyStore.getInstance(getKeystoreType());
			keyStore.load(null, getPassphrase().toCharArray());
			dirty = true;
			save();
			contents = FileUtils.getFileContents(getFilename());
		}
		keyStore = KeyStore.getInstance(getKeystoreType());
		ByteArrayInputStream bais = new ByteArrayInputStream(contents);
		keyStore.load(bais, getPassphrase().toCharArray());
	}
	
	@Override
	public synchronized void save() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		if ( dirty ) {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
			keyStore.store(baos, getPassphrase().toCharArray());
			baos.close();
			FileUtils.storeFileContents(getFilename(), baos.toByteArray(), "."+suffixId.getAndIncrement());
		}
		dirty = false;
	}

	@Override
	public synchronized Map<String, EntryType> getAliases() throws KeyStoreException {
		Enumeration<String> aliases = keyStore.aliases();
		Map<String,EntryType> entryMap = new HashMap<>();
		while( aliases.hasMoreElements() ) {
			String alias = aliases.nextElement();
			if ( keyStore.isCertificateEntry(alias)){
				entryMap.put(alias, EntryType.TRUSTED_CERTIFICATE);
			} else if ( keyStore.isKeyEntry(alias)) {
				entryMap.put(alias, EntryType.PRIVATE_KEY);
			}
		}
		return entryMap;
	}

	@Override
	public String getAlias(X509Certificate certificate)
			throws KeyStoreException {
		return keyStore.getCertificateAlias(certificate);
	}

	@Override
	public synchronized X509Certificate[] getPrivateKeyCertificate(String alias) throws KeyStoreException {
		return (X509Certificate[]) keyStore.getCertificateChain(alias);
	}

	@Override
	public synchronized PrivateKey getPrivateKey(String alias) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		return (PrivateKey)keyStore.getKey(alias, getPassphrase().toCharArray());
	}

	@Override
	public synchronized void setPrivateKey(String alias, X509Certificate[] chain,
			PrivateKey privateKey) throws KeyStoreException {
		keyStore.setKeyEntry(alias, privateKey, getPassphrase().toCharArray(), chain);
		dirty = true;
	}
	
	@Override
	public synchronized void setTrustedCertificate(String alias, X509Certificate cert) throws KeyStoreException {
		keyStore.setCertificateEntry(alias, cert);
		dirty = true;
	}

	@Override
	public X509Certificate getTrustedCA(String alias) throws KeyStoreException {
		return (X509Certificate)keyStore.getCertificate(alias);
	}

	@Override
	public synchronized X509Certificate[] getAllTrustedCAs() throws KeyStoreException {
		Enumeration<String> aliases = keyStore.aliases();
		List<X509Certificate> list = new ArrayList<>();
		while( aliases.hasMoreElements() ) {
			String alias = aliases.nextElement();
			if ( keyStore.isCertificateEntry(alias)){
				list.add((X509Certificate)keyStore.getCertificate(alias));
			}
		}
		return list.toArray(new X509Certificate[0]);
	}

	@Override
	public synchronized void delete(String alias) throws KeyStoreException {
		keyStore.deleteEntry(alias);
		dirty = true;
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public String getKeystoreType() {
		return keystoreType;
	}

	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}

}
