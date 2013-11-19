package org.tdmx.client.crypto.algorithm;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;

import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum SignatureAlgorithm {

	SHA_1_RSA("SHA1withRSA"), 
	SHA_256_RSA("SHA256withRSA"), 
	SHA_384_RSA("SHA384withRSA"),
	SHA_512_RSA("SHA512withRSA"),
	;

	private String algorithm;
	
	private SignatureAlgorithm( String algorithm ) {
		this.algorithm = algorithm;
	}
	
	public String getAlgorithm() {
		return this.algorithm;
	}
	
	public Signature getSignature( PrivateKey privateKey ) throws CryptoException {
		try {
			Signature sign = Signature.getInstance(getAlgorithm());
			sign.initSign(privateKey,EntropySource.getSecureRandom());
			return sign;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SIGNATURE_ALGORITHM_MISSING, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SIGNATURE_KEY_INVALID, e);
		}
	}
	
	public Signature getVerifier( PublicKey publicKey ) throws CryptoException {
		try {
			Signature sign = Signature.getInstance(getAlgorithm());
			sign.initVerify(publicKey);
			return sign;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SIGNATURE_ALGORITHM_MISSING, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_SIGNATURE_VALIDATION_KEY_INVALID, e);
		}
	}

	public static int signatureSizeBytes( Signature s, PublicKey publicKey ) throws CryptoException {
		// RSA signatures are as long as the RSA public key
		if ( publicKey instanceof RSAPublicKey ) {
			return ((RSAPublicKey)publicKey).getModulus().bitLength() / 8;
		}
		throw new CryptoException(CryptoResultCode.ERROR_PK_KEY_INVALID);
	}
}
