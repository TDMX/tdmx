package org.tdmx.console.application.domain;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.domain.validation.FieldError.ERROR;
import org.tdmx.console.application.search.SearchServiceImpl;
import org.tdmx.console.application.util.ValidationUtils;


/**
 * An X509Certificate.
 * 
 * @author Peter
 *
 */
public class X509CertificateDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final DomainObjectField F_CERTIFICATE	= new DomainObjectField("certificate", X509CertificateDO.class.getName());

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

	private X509Certificate certificate;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public X509CertificateDO( X509Certificate certificate ) throws CryptoCertificateException {
		setId(CertificateIOUtils.getSha1FingerprintAsHex(certificate));
		this.certificate = certificate;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public X509Certificate getCertificate() {
		return certificate;
	}

}
