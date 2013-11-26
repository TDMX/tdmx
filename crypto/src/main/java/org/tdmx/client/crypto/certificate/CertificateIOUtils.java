package org.tdmx.client.crypto.certificate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.openssl.PEMWriter;


public class CertificateIOUtils {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	public static String x509certToPem( X509Certificate cert ) throws CertificateEncodingException, IOException {
    	byte[] certificate = cert.getEncoded();
    	
    	StringWriter writer = new StringWriter();
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PEMWriter pemWrtCer = new PEMWriter(writer);//new OutputStreamWriter(baos));
        pemWrtCer.writeObject(cert);
        pemWrtCer.close();
        
        return writer.toString();
	}

	public boolean validate(X509Certificate[] certs, KeyStore trustStore) throws CertificateException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, CertPathValidatorException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		List<X509Certificate> mylist = new ArrayList<X509Certificate>();
		for( X509Certificate cert : certs ) {
			mylist.add(cert);
		}
		CertPath cp = cf.generateCertPath(mylist);
		
		PKIXParameters params = new PKIXParameters(trustStore);
		params.setRevocationEnabled(false);
		CertPathValidator cpv =
		      CertPathValidator.getInstance(CertPathValidator.getDefaultType());
		PKIXCertPathValidatorResult pkixCertPathValidatorResult =
		      (PKIXCertPathValidatorResult) cpv.validate(cp, params);	
		return true; //
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
