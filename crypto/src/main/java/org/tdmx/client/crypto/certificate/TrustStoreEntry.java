package org.tdmx.client.crypto.certificate;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.scheme.CryptoException;


public class TrustStoreEntry {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final String NL = System.getProperty("line.separator");
	public static final String COMMENT_LINE = "-- TDMX-TRUSTSTORE-COMMENT ";
	public static final String FRIENDLY_NAME = "-- TDMX-TRUSTSTORE-FRIENDY_NAME ";
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String comment;
	private String friendlyName;
	private PKIXCertificate certificate;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public TrustStoreEntry( PKIXCertificate cert ) {
		certificate = cert;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	public void addComment( String commentLine ) {
		if ( comment != null ) {
			comment = comment + NL + commentLine;
		} else {
			comment = commentLine;
		}
	}
	
	public void clearComment(){
		comment = null;
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

	public String getComment() {
		return comment;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	public PKIXCertificate getCertificate() {
		return certificate;
	}
}
