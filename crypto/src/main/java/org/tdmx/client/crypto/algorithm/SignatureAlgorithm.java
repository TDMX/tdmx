package org.tdmx.client.crypto.algorithm;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.operator.ContentSigner;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum SignatureAlgorithm {

	SHA_1_RSA("SHA1withRSA", DigestAlgorithm.SHA_1, "RSA", PKCSObjectIdentifiers.sha1WithRSAEncryption), 
	SHA_256_RSA("SHA256withRSA", DigestAlgorithm.SHA_256, "RSA", PKCSObjectIdentifiers.sha256WithRSAEncryption), 
	SHA_384_RSA("SHA384withRSA", DigestAlgorithm.SHA_384, "RSA", PKCSObjectIdentifiers.sha384WithRSAEncryption),
	SHA_512_RSA("SHA512withRSA", DigestAlgorithm.SHA_512, "RSA", PKCSObjectIdentifiers.sha512WithRSAEncryption),
	SHA_1_ECDSA("SHA1withECDSA", DigestAlgorithm.SHA_1, "EC", X9ObjectIdentifiers.ecdsa_with_SHA1),
	SHA_256_ECDSA("SHA256withECDSA", DigestAlgorithm.SHA_256, "EC", X9ObjectIdentifiers.ecdsa_with_SHA256),
	SHA_384_ECDSA("SHA384withECDSA", DigestAlgorithm.SHA_384, "EC", X9ObjectIdentifiers.ecdsa_with_SHA384),
	SHA_512_ECDSA("SHA512withECDSA", DigestAlgorithm.SHA_512, "EC", X9ObjectIdentifiers.ecdsa_with_SHA512),
	;

	private String algorithm;
	private DigestAlgorithm hashAlgorithm;
	private String keyType;
	private ASN1ObjectIdentifier asn1oid;
	
	private SignatureAlgorithm( String algorithm, DigestAlgorithm hashAlgorithm, String keyType, ASN1ObjectIdentifier oid ) {
		this.algorithm = algorithm;
		this.hashAlgorithm = hashAlgorithm;
		this.keyType = keyType;
		this.asn1oid = oid;
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
	
	public ASN1ObjectIdentifier getAsn1oid() {
		return asn1oid;
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

	public static int getMaxSignatureSizeBytes( Signature s, PublicKey publicKey ) throws CryptoException {
		// RSA signatures are as long as the RSA public key
		if ( publicKey instanceof RSAPublicKey ) {
			return ((RSAPublicKey)publicKey).getModulus().bitLength() / 8;
		} else if ( publicKey instanceof ECPublicKey ) {
			//http://stackoverflow.com/questions/14953113/ec-signature-size?lq=1
			//https://www.eldos.com/forum/read.php?FID=7&TID=2216
			int asn1overhead = 20;
			return (((ECPublicKey)publicKey).getParams().getCurve().getField().getFieldSize()*2 / 8)+asn1overhead;
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
		if ( privateKey instanceof ECPrivateKey ) {
			keyType = "EC";
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
	
	public static ContentSigner getContentSigner(final PrivateKey privateKey, final SignatureAlgorithm alg) {
		return new ContentSigner() {
			
		    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			@Override
			public byte[] getSignature() {
				
				try {
					Signature signer = alg.getSignature(privateKey);
		            signer.update(outputStream.toByteArray());
		            return signer.sign();
				} catch (CryptoException e) {
					return null;
				} catch (SignatureException e) {
					return null;
				}
			}
			
			@Override
			public OutputStream getOutputStream() {
				return outputStream;
			}
			
			@Override
			public AlgorithmIdentifier getAlgorithmIdentifier() {
				return new AlgorithmIdentifier(alg.getAsn1oid());
			}
		};
	
	}
}
