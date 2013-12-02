package org.tdmx.client.crypto.certificate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;


public class TrustStoreCertificateIOUtils {

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
	public static String trustStoreEntryToPem( TrustStoreEntry entry ) throws CryptoCertificateException {
    	StringWriter writer = new StringWriter();
    	if ( entry.getFriendlyName() != null ) {
    		writer.write(TrustStoreEntry.FRIENDLY_NAME+entry.getSha1fingerprint()+" "+entry.getFriendlyName()+TrustStoreEntry.NL);
    	}
    	if ( entry.getComment() != null ) {
            BufferedReader br = new BufferedReader(new StringReader(entry.getComment()));
            String commentLine;
            try {
    			while((commentLine = br.readLine())!= null) {
    		    	writer.write(TrustStoreEntry.COMMENT_LINE+entry.getSha1fingerprint()+" "+commentLine+TrustStoreEntry.NL);
    			}
            } catch ( IOException e ) {
    			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
            }
            
    	}
        PEMWriter pemWrtCer = new PEMWriter(writer);
        try {
            pemWrtCer.writeObject(entry.getCertificate());
            pemWrtCer.close();
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
        
        return writer.toString();
	}

	public static List<TrustStoreEntry> pemToTrustStoreEntries( String input ) throws CryptoCertificateException {
		StringReader sr = new StringReader(input);
		PEMParser pp = new PEMParser(sr);
		
		List<TrustStoreEntry> certList = new ArrayList<>();
		Object o = null;
        try {
    		while( (o = pp.readObject()) != null ) {
    			if ( o instanceof X509CertificateHolder ) {
    				X509CertificateHolder ch = (X509CertificateHolder)o;
    				X509Certificate c = CertificateIOUtils.decodeCertificate(ch.getEncoded());
    				certList.add(new TrustStoreEntry(c));
    			}
    		}
            pp.close();
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
        
        BufferedReader br = new BufferedReader(new StringReader(input));
        String strLine;
        try {
			while((strLine = br.readLine())!= null) {
				if ( strLine.startsWith(TrustStoreEntry.FRIENDLY_NAME) ) {
					String restofLine = strLine.substring(TrustStoreEntry.FRIENDLY_NAME.length());
					int separator = restofLine.indexOf(" ");
					if ( separator != -1 ) {
						String fingerprint = restofLine.substring(0, separator);
						String text = restofLine.substring(separator+1);
						
						for( TrustStoreEntry e : certList ) {
							if ( e.getSha1fingerprint().equals(fingerprint)) {
								e.setFriendlyName(text);
							}
						}
					}
				}
				if ( strLine.startsWith(TrustStoreEntry.COMMENT_LINE) ) {
					String restofLine = strLine.substring(TrustStoreEntry.COMMENT_LINE.length());
					int separator = restofLine.indexOf(" ");
					if ( separator != -1 ) {
						String fingerprint = restofLine.substring(0, separator);
						String text = restofLine.substring(separator+1);
						
						for( TrustStoreEntry e : certList ) {
							if ( e.getSha1fingerprint().equals(fingerprint)) {
								e.addComment(text);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		}
       
		return certList;
	}
	
	public static List<TrustStoreEntry> getAllSystemTrustedCAs() throws CryptoCertificateException {
		List<TrustStoreEntry> caList = new ArrayList<>();
		
		TrustManagerFactory tmf;
		try {
			tmf = TrustManagerFactory.getInstance(CertificateIOUtils.ALGORITHM);
			tmf.init((KeyStore)null);
			TrustManager[] tmgs = tmf.getTrustManagers();
			if ( tmgs != null ) {
				for ( TrustManager tm : tmgs ) {
					if ( tm instanceof X509TrustManager) {
						X509TrustManager t = (X509TrustManager)tm;
						X509Certificate[] issuers = t.getAcceptedIssuers();
						if ( issuers != null ) {
							for( X509Certificate i : issuers ) {
								TrustStoreEntry e = new TrustStoreEntry(i);
								caList.add(e);
							}
						}
					}
				}
			}
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_ALGORITHM, e);
		} catch (KeyStoreException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_KEYSTORE, e);
		}
		return caList;
	}
	
	public static List<TrustStoreEntry> getAllSystemDisrustedCAs() throws CryptoCertificateException {
		List<TrustStoreEntry> distrustedCaList = new ArrayList<>();
		
		for( TrustStoreEntry e : UntrustedCertificates.untrustedCerts.values() ){
			distrustedCaList.add(e);
		}
		return distrustedCaList;
	}
	
	//TODO not used yet.
	public static boolean validate(X509Certificate[] certs, KeyStore trustStore) throws CertificateException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, CertPathValidatorException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		List<X509Certificate> mylist = new ArrayList<X509Certificate>();
		for( X509Certificate cert : certs ) {
			mylist.add(cert);
		}
		CertPath cp = cf.generateCertPath(mylist);
		
		PKIXParameters params = new PKIXParameters(trustStore);
		params.setRevocationEnabled(false);
		CertPathValidator cpv =
		      CertPathValidator.getInstance("PKIX");
		PKIXCertPathValidatorResult pkixCertPathValidatorResult =
		      (PKIXCertPathValidatorResult) cpv.validate(cp, params);	
		return true;
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
