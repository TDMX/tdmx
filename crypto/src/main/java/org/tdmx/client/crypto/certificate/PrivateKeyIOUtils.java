package org.tdmx.client.crypto.certificate;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;


public class PrivateKeyIOUtils {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final String ALGORITHM  = "RSA";
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	public static KeyPair pemToRSAPrivateKeyPair( String input ) throws CryptoCertificateException {
		StringReader sr = new StringReader(input);
		PEMParser pp = new PEMParser(sr);
		
		Object o = null;
        try {
    		while( (o = pp.readObject()) != null ) {
    			if ( o instanceof PEMKeyPair ) {
    				PEMKeyPair ch = (PEMKeyPair)o;
    				
    				byte[] pkbytes = ch.getPublicKeyInfo().getEncoded(ASN1Encoding.DER);
    				KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
    				EncodedKeySpec eks = new X509EncodedKeySpec(pkbytes);
    				PublicKey publicKey  = kf.generatePublic(eks);
    				
    				byte[] privbytes = ch.getPrivateKeyInfo().getEncoded(ASN1Encoding.DER);
    				EncodedKeySpec epks = new PKCS8EncodedKeySpec(privbytes);
    				PrivateKey privateKey  = kf.generatePrivate(epks);
    				
    				KeyPair kp = new KeyPair(publicKey,privateKey);
    				return kp;
    			}
    		}
		} catch (IOException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_IO, e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_MISSING_ALGORITHM, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_INVALID_KEY_SPEC, e);
		} finally {
            try {
				pp.close();
			} catch (IOException e) {
			}
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
