package org.tdmx.console.application.dao;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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
	private String trustStoreAlgorithm = "X509";
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SystemTrustStoreImpl() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	@Override
	public synchronized List<X509Certificate> getAllTrustedCAs() throws NoSuchAlgorithmException, KeyStoreException {
		List<X509Certificate> caList = new ArrayList<>();
		
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(getTrustStoreAlgorithm());
		tmf.init((KeyStore)null);
		TrustManager[] tmgs = tmf.getTrustManagers();
		if ( tmgs != null ) {
			for ( TrustManager tm : tmgs ) {
				if ( tm instanceof X509TrustManager) {
					X509TrustManager t = (X509TrustManager)tm;
					X509Certificate[] issuers = t.getAcceptedIssuers();
					if ( issuers != null ) {
						for( X509Certificate i : issuers ) {
							caList.add(i);
						}
					}
				}
			}
		}
		return caList;
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

	public String getTrustStoreAlgorithm() {
		return trustStoreAlgorithm;
	}

	public void setTrustStoreAlgorithm(String trustStoreAlgorithm) {
		this.trustStoreAlgorithm = trustStoreAlgorithm;
	}

}
