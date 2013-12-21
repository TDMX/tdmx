package org.tdmx.client.crypto.certificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;


public class CertificateIOUtils {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final String ALGORITHM  = "X.509";
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	public static List<X509Certificate> convert( List<PKIXCertificate> certs ) {
		List<X509Certificate> xs = new ArrayList<>();
		for( PKIXCertificate p : certs ) {
			xs.add(p.getCertificate());
		}
		return xs;
	}
	
	public static String x509certToPem( PKIXCertificate cert ) throws CryptoCertificateException {
    	StringWriter writer = new StringWriter();
        PEMWriter pemWrtCer = new PEMWriter(writer);
        try {
            pemWrtCer.writeObject(cert.getCertificate());
            pemWrtCer.close();
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
        
        return writer.toString();
	}

	public static PKIXCertificate pemToX509cert( String input ) throws CryptoCertificateException {
		PKIXCertificate[] certs = pemToX509certs( input );
		if( certs == null ) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_CERTS);
		}
		if ( certs.length != 1 ) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_TOO_MANY_CERTS);
		}
		return certs[0];
	}
	
	public static PKIXCertificate[] pemToX509certs( String input ) throws CryptoCertificateException {
		StringReader sr = new StringReader(input);
		PEMParser pp = new PEMParser(sr);
		
		List<PKIXCertificate> certList = new ArrayList<>();
		Object o = null;
        try {
    		while( (o = pp.readObject()) != null ) {
    			if ( o instanceof X509CertificateHolder ) {
    				X509CertificateHolder ch = (X509CertificateHolder)o;
    				PKIXCertificate c = decodeCertificate(ch.getEncoded());
    				certList.add(c);
    			}
    		}
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		} finally {
            try {
				pp.close();
			} catch (IOException e) {
			}
		}
		return certList.toArray(new PKIXCertificate[0]);
	}
	
	public static PKIXCertificate decodeCertificate( byte[] x509encodedValue ) throws CryptoCertificateException {
		CertificateFactory certFactory;
		try {
			certFactory = CertificateFactory.getInstance(ALGORITHM);
			X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(x509encodedValue));
			return new PKIXCertificate(cert);
		} catch (CertificateException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		}
	}
	
	public static byte[] encodeCertificate( PKIXCertificate cert ) throws CryptoCertificateException {
		try {
			if ( cert != null && cert.getCertificate() != null ) {
				return cert.getCertificate().getEncoded();
			}
		} catch (CertificateException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
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
