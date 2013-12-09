package org.tdmx.console.application.domain;

import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.console.application.util.CalendarUtils;


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
	
	public String getInfo() {
		return certificate.toString();
	}
	
	public Calendar getValidFrom() {
		return CalendarUtils.getDate(certificate.getNotBefore());
	}
	
	public Calendar getValidTo() {
		return CalendarUtils.getDate(certificate.getNotAfter());
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

	public X509Certificate getCertificate() {
		return certificate; 
	}

}
