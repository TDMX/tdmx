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
import java.util.concurrent.atomic.AtomicInteger;

import org.tdmx.console.application.util.FileUtils;

public class PrivateKeyStoreImpl implements PrivateKeyStore {

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

	public PrivateKeyStoreImpl() {
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
	public synchronized X509Certificate[] getPrivateKeyCertificate(String certId) throws KeyStoreException {
		return (X509Certificate[]) keyStore.getCertificateChain(certId);
	}

	@Override
	public synchronized PrivateKey getPrivateKey(String certId) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		return (PrivateKey)keyStore.getKey(certId, getPassphrase().toCharArray());
	}

	@Override
	public synchronized void setPrivateKey(String certId, X509Certificate[] chain,
			PrivateKey privateKey) throws KeyStoreException {
		keyStore.setKeyEntry(certId, privateKey, getPassphrase().toCharArray(), chain);
		dirty = true;
	}
	
	@Override
	public synchronized void delete(String certId) throws KeyStoreException {
		keyStore.deleteEntry(certId);
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
