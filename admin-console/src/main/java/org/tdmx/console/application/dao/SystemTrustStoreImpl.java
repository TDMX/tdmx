package org.tdmx.console.application.dao;

import java.util.List;

import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.TrustStoreCertificateIOUtils;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;

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
	public synchronized List<TrustStoreEntry> getAllTrustedCAs() throws CryptoCertificateException {
		return TrustStoreCertificateIOUtils.getAllSystemTrustedCAs();
	}

	@Override
	public List<TrustStoreEntry> getAllDistrustedTrustedCAs()
			throws CryptoCertificateException {
		return TrustStoreCertificateIOUtils.getAllSystemDisrustedCAs();
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
