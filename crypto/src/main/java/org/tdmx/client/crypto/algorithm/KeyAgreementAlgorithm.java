package org.tdmx.client.crypto.algorithm;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;

import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum KeyAgreementAlgorithm {

	ECDH384(384, "EC", "ECDH", "secp384r1");

	static {
		JCAProviderInitializer.init();	
	}
	
	private int keyLength;
	private String keyAlgorithm;
	private String agreementAlgorithm;
	private String parameter;
	
	private KeyAgreementAlgorithm(int keyLength, String keyAlgorithm, String agreementAlgorithm, String parameter ) {
		this.keyLength = keyLength;
		this.keyAlgorithm = keyAlgorithm;
		this.agreementAlgorithm = agreementAlgorithm;
		this.parameter = parameter;
	}

	public KeyPair generateNewKeyPair() throws CryptoException {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
			ECGenParameterSpec ecSpec = new ECGenParameterSpec(parameter);
			keyGen.initialize(ecSpec, EntropySource.getSecureRandom()); // set up // KeyAgreement
			KeyPair kp = keyGen.generateKeyPair();
			return kp;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(CryptoResultCode.ERROR_KA_ALGORITHM_PARAMETER_INVALID, e);
		}
	}

	public byte[] agreeKey( KeyPair ownKeyPair, PublicKey otherKey ) throws CryptoException {
		try {
			KeyAgreement keyAgree = KeyAgreement.getInstance(agreementAlgorithm);
			try {
				keyAgree.init(ownKeyPair.getPrivate());
			} catch (InvalidKeyException e) {
				throw new CryptoException(CryptoResultCode.ERROR_KA_PRIVATE_KEY_SPEC_INVALID, e);
			}
			try {
				keyAgree.doPhase(otherKey, true);
			} catch (InvalidKeyException e) {
				throw new CryptoException(CryptoResultCode.ERROR_KA_PUBLIC_KEY_SPEC_INVALID, e);
			}
	        byte[] sk = keyAgree.generateSecret();
			if ( sk.length != keyLength/8 ) {
				throw new CryptoException(CryptoResultCode.ERROR_KA_SHARED_SECRET_KEY_INVALID);
			}
	        return sk;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_KA_ALGORITHM_MISSING, e);
		}
	}
	
	public PublicKey decodeX509EncodedKey( byte[] publicKeyBytes ) throws CryptoException {
		try {
			KeyFactory kf = KeyFactory.getInstance(keyAlgorithm);
			EncodedKeySpec eks = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey  = kf.generatePublic(eks);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoException(CryptoResultCode.ERROR_KA_PUBLIC_KEY_SPEC_INVALID, e);
		}
	}
	
	public byte[] encodeX509PublicKey( PublicKey publicKey ) throws CryptoException {
		if ( !"X.509".equals(publicKey.getFormat())) {
			throw new CryptoException(CryptoResultCode.ERROR_ENCODED_KEY_FORMAT_INVALID );
		}
		return publicKey.getEncoded();
	}
	
	public int getKeyLength() {
		return keyLength;
	}

	public String getParameter() {
		return parameter;
	}

	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public String getAgreementAlgorithm() {
		return agreementAlgorithm;
	}

}
