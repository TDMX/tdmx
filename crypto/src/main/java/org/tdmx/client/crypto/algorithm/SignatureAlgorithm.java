package org.tdmx.client.crypto.algorithm;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum SignatureAlgorithm {

	SHA_1_RSA("SHA1withRSA", DigestAlgorithm.SHA_1, "RSA"), 
	SHA_256_RSA("SHA256withRSA", DigestAlgorithm.SHA_256, "RSA"), 
	SHA_384_RSA("SHA384withRSA", DigestAlgorithm.SHA_384, "RSA"),
	SHA_512_RSA("SHA512withRSA", DigestAlgorithm.SHA_512, "RSA"),
	;

	private String algorithm;
	private DigestAlgorithm hashAlgorithm;
	private String keyType;
	private String asn1ObjectId;
	
	private SignatureAlgorithm( String algorithm, DigestAlgorithm hashAlgorithm, String keyType ) {
		this.algorithm = algorithm;
		this.hashAlgorithm = hashAlgorithm;
		this.keyType = keyType;
	}
	
	public String getAlgorithm() {
		return this.algorithm;
	}
	
	public DigestAlgorithm getHashAlgorithm() {
		return hashAlgorithm;
	}

	public String getKeyType() {
		return keyType;
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

	/**
	 * Find a SignatureAlgorithm suitable for a given PrivateKey and hashAlgorithm.
	 * 
	 * @param privateKey
	 * @param hashAlgorithm
	 * @return
	 */
	public static SignatureAlgorithm getAlgorithmForKey( PrivateKey privateKey, DigestAlgorithm hashAlgorithm ) {
		if ( privateKey == null ) {
			return null;
		}
		String keyType = null;
		if ( privateKey instanceof RSAPrivateKey ) {
			keyType = "RSA";
		}
		if ( keyType == null ) {
			return null;
		}
		for( SignatureAlgorithm sa : SignatureAlgorithm.values() ) {
			if ( keyType.equals(sa.keyType) && hashAlgorithm == sa.hashAlgorithm ) {
				return sa;
			}
		}
		return null;
	}
}
