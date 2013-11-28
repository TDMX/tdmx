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
	public static String x509certToPem( X509Certificate cert ) throws CryptoCertificateException {
    	StringWriter writer = new StringWriter();
        PEMWriter pemWrtCer = new PEMWriter(writer);
        try {
            pemWrtCer.writeObject(cert);
            pemWrtCer.close();
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
        
        return writer.toString();
	}

	public static X509Certificate[] pemToX509certs( String input ) throws CryptoCertificateException {
		StringReader sr = new StringReader(input);
		PEMParser pp = new PEMParser(sr);
		
		List<X509Certificate> certList = new ArrayList<>();
		Object o = null;
        try {
    		while( (o = pp.readObject()) != null ) {
    			if ( o instanceof X509CertificateHolder ) {
    				X509CertificateHolder ch = (X509CertificateHolder)o;
    				X509Certificate c = decodeCertificate(ch.getEncoded());
    				certList.add(c);
    			}
    		}
            pp.close();
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
		return certList.toArray(new X509Certificate[0]);
	}
	
	public static X509Certificate decodeCertificate( byte[] x509encodedValue ) throws CryptoCertificateException {
		CertificateFactory certFactory;
		try {
			certFactory = CertificateFactory.getInstance(ALGORITHM);
			X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(x509encodedValue));
			return cert;
		} catch (CertificateException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		}
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
