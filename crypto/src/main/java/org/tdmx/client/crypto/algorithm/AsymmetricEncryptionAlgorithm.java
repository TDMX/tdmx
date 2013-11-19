package org.tdmx.client.crypto.algorithm;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum AsymmetricEncryptionAlgorithm {

	RSA2048(2048, "RSA", "RSA/ECB/OAEPWithSHA1AndMGF1Padding"),
	RSA4096(4096, "RSA", "RSA/ECB/OAEPWithSHA1AndMGF1Padding"); 

	static {
		JCAProviderInitializer.init();	
	}
	
	private int keyLength;
	private String algorithm;
	private String transformation;
	
	private AsymmetricEncryptionAlgorithm(int keyLength, String algorithm, String transformation ) {
		this.keyLength = keyLength;
		this.algorithm = algorithm;
		this.transformation = transformation;
	}

	public KeyPair generateNewKeyPair() throws CryptoException {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
			kpg.initialize(keyLength, EntropySource.getSecureRandom());
			KeyPair kp = kpg.generateKeyPair();
			return kp;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		}
	}

	public PrivateKey decodePKCS8EncodedKey( byte[] privateKeyBytes ) throws CryptoException {
		try {
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey  = kf.generatePrivate(privateKeySpec);
			if ( privateKey instanceof RSAPrivateKey ) {
				int bitLen = ((RSAPrivateKey)privateKey).getModulus().bitLength();
				if ( bitLen != keyLength ) {
					throw new CryptoException(CryptoResultCode.ERROR_PK_PRIVATE_KEY_SPEC_INVALID);
				}
			} else {
				throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISMATCH);
			}
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_PRIVATE_KEY_SPEC_INVALID, e);
		}
	}
	
	public PublicKey decodeX509EncodedKey( byte[] publicKeyBytes ) throws CryptoException {
		try {
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			EncodedKeySpec eks = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey  = kf.generatePublic(eks);
			if ( publicKey instanceof RSAPublicKey ) {
				int bitLen = ((RSAPublicKey)publicKey).getModulus().bitLength();
				if ( bitLen != keyLength ) {
					throw new CryptoException(CryptoResultCode.ERROR_PK_PRIVATE_KEY_SPEC_INVALID);
				}
			} else {
				throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISMATCH);
			}
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_PUBLIC_KEY_SPEC_INVALID, e);
		}
	}
	
	public byte[] encodeX509PublicKey( PublicKey publicKey ) throws CryptoException {
		if ( !"X.509".equals(publicKey.getFormat())) {
			throw new CryptoException(CryptoResultCode.ERROR_ENCODED_KEY_FORMAT_INVALID );
		}
		return publicKey.getEncoded();
	}
	
	public byte[] encrypt( PublicKey publicKey, byte[] plaintext ) throws CryptoException {
		try {
			Cipher c = Cipher.getInstance(transformation);
	        c.init(Cipher.ENCRYPT_MODE, publicKey);
	        return c.doFinal(plaintext);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (NoSuchPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_PADDING_MISSING, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_KEY_INVALID, e);
		} catch (IllegalBlockSizeException e) { 
			throw new CryptoException(CryptoResultCode.ERROR_PK_BLOCKSIZE_INVALID, e);
		} catch (BadPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_PADDING_INVALID, e);
		}
	}
	
	public byte[] decrypt( PrivateKey privateKey, byte[] ciphertext ) throws CryptoException {
        Cipher c;
		try {
			c = Cipher.getInstance(transformation);
	        c.init(Cipher.DECRYPT_MODE, privateKey);
	        return c.doFinal(ciphertext);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING, e);
		} catch (NoSuchPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_PADDING_MISSING, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_KEY_INVALID, e);
		} catch (IllegalBlockSizeException e) { 
			throw new CryptoException(CryptoResultCode.ERROR_PK_BLOCKSIZE_INVALID, e);
		} catch (BadPaddingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_PK_PADDING_INVALID, e);
		}
	}
	
	public static AsymmetricEncryptionAlgorithm getAlgorithmMatchingKey( PublicKey k ) throws CryptoException {
		if ( k instanceof RSAPublicKey ) {
			int bitLen = ((RSAPublicKey)k).getModulus().bitLength();
			switch ( bitLen ) {
			case 2048:
				return AsymmetricEncryptionAlgorithm.RSA2048;
			case 4096:
				return AsymmetricEncryptionAlgorithm.RSA4096;
			}
		}
		throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING);
	}
	
	public static AsymmetricEncryptionAlgorithm getAlgorithmMatchingKey( PrivateKey k ) throws CryptoException {
		if ( k instanceof RSAPrivateKey ) {
			int bitLen = ((RSAPrivateKey)k).getModulus().bitLength();
			switch ( bitLen ) {
			case 2048:
				return AsymmetricEncryptionAlgorithm.RSA2048;
			case 4096:
				return AsymmetricEncryptionAlgorithm.RSA4096;
			}
		}
		throw new CryptoException(CryptoResultCode.ERROR_PK_ALGORITHM_MISSING);
	}
	
	public int getKeyLength() {
		return keyLength;
	}

	public int getKeyLengthInBytes() {
		return keyLength / 8;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getTransformation() {
		return transformation;
	}
}
