package org.tdmx.client.crypto.certificate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Calendar;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.scheme.CryptoException;


public class CertificateAuthorityUtils {

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

	/**
	 * 
	 * @param req
	 * @return
	 * @throws CryptoCertificateException 
	 */
	public static PKIXCredential createCertificateAuthority( PKIXCertificateAuthorityRequest req ) throws CryptoCertificateException {
		 //TODO - PKIXCertificate to indicate which algorithm + key strength
		KeyPair kp = null;
		try {
			kp = req.getKeyAlgorithm().generateNewKeyPair();
		} catch (CryptoException e1) {
			// TODO 
		}
		PublicKey publicKey = kp.getPublic();
		PrivateKey privateKey = kp.getPrivate();
		
		Calendar now = Calendar.getInstance();
		Calendar future = Calendar.getInstance();
		future.add(Calendar.YEAR, 10);
		
		String subject = ""; //TODO
		String issuer = subject; //TODO 
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
				new X500Name(issuer), 
				new BigInteger("1"), 
				now.getTime(),
				future.getTime(), 
				new X500Name(subject), 
				publicKey);

		BasicConstraints cA = new BasicConstraints(1);
		try {
			JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
			certBuilder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(publicKey));
			certBuilder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(publicKey));

			certBuilder.addExtension(Extension.basicConstraints, true, cA);
			JCESigner signer = new JCESigner(privateKey, "SHA256withRSA"); //TODO link with crypto.alg.
			byte[] certBytes = certBuilder.build(signer).getEncoded();
			
			PKIXCertificate c = CertificateIOUtils.decodeCertificate(certBytes);
			
			return new PKIXCredential(c, privateKey);
		} catch (CertIOException e) {
			//TODO
		} catch (NoSuchAlgorithmException e) {
			//TODO
		} catch (IOException e) {
			//TODO
		}
		
		return null;
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------
	private static class JCESigner implements ContentSigner {

		//TODO bring into crypto.MessageDigest enum
	    private static final AlgorithmIdentifier PKCS1_SHA256_WITH_RSA_OID = new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"));

	    private SignatureAlgorithm signature;
	    private ByteArrayOutputStream outputStream;

	    public JCESigner(PrivateKey privateKey, SignatureAlgorithm signatureAlgorithm) {
	        if (!"SHA256withRSA".equals(signatureAlgorithm)) {
	            throw new IllegalArgumentException("Signature algorithm \"" + signatureAlgorithm + "\" not yet supported");
	        }
	        try {
	            this.outputStream = new ByteArrayOutputStream();
	            this.signature = Signature.getInstance(signatureAlgorithm);
	            this.signature.initSign(privateKey);
	        } catch (GeneralSecurityException gse) {
	            throw new IllegalArgumentException(gse.getMessage());
	        }
	    }

	    @Override
	    public AlgorithmIdentifier getAlgorithmIdentifier() {
	        if (signature.getAlgorithm().equals("SHA256withRSA")) {
	            return PKCS1_SHA256_WITH_RSA_OID;
	        } else {
	            return null;
	        }
	    }

	    @Override
	    public OutputStream getOutputStream() {
	        return outputStream;
	    }

	    @Override
	    public byte[] getSignature() {
	        try {
	            signature.update(outputStream.toByteArray());
	            return signature.sign();
	        } catch (GeneralSecurityException gse) {
	            gse.printStackTrace();
	            return null;
	        }
	    }
	}
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

}
