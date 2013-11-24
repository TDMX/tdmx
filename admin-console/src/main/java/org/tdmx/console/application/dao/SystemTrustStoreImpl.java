package org.tdmx.console.application.dao;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SystemTrustStoreImpl implements SystemTrustStore {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SystemTrustStoreImpl() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	@Override
	public synchronized X509Certificate[] getAllTrustedCAs() throws NoSuchAlgorithmException {
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("TLS");
		TrustManager[] tmgs = tmf.getTrustManagers();
		TrustManager tm = tmgs[0];
		if ( tm instanceof X509TrustManager) {
			X509TrustManager t = (X509TrustManager)tm;
			return t.getAcceptedIssuers();
		}
		return null;
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

}
