package org.tdmx.client.crypto.scheme.ecdh;

import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.algorithm.KeyAgreementAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.algorithm.StreamCipherAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.scheme.CryptoContext;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.Encrypter;
import org.tdmx.client.crypto.stream.FileBackedOutputStream;
import org.tdmx.client.crypto.stream.SigningOutputStream;

/**
 * encryption( M, PF, (K-A,K-a), K-B, A-B ) -> E, L
 * {
 * validate A-B must be a 384bit X.509 encoded EC public key. Prior validation to be from B.
 * EC key generate (A-A,A-a), an EC keypair on secp384r1
 * ECDH key agreement (A-a,A-B) => shared secret S
 * PFS := SHA384(PF) - convert the PF into a shared secret.
 * SKe || IVe:= SHA384(A-B||S||PFS), 
 *   where SKe is a 256bit AES encryption key, 
 *   IVe is a 128bit initialization vector for the AES encryption
 * E := AES256/CTR(SKe,IVe,ZLib-compress(M||Sign(K-a,M||byte-len(M))))
 * L := byte-len(M) || A-A
 *   where byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer.
 *   A-A is a X.509 encoded EC public key
 * }
 * 
 * @author Peter
 *
 */
public class PF_ECDHPayloadEncrypter implements Encrypter {

	private TemporaryBufferFactory bufferFactory;
	
	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	private PublicKey sessionKey;
	private KeyPair messageKey;
	private boolean rsaEnabled;
	
	private SecretKeySpec secretKey;
	private IvParameterSpec secretIv;
	
	private FileBackedOutputStream fbos = null;
	private SigningOutputStream sos = null;
	
	private StreamCipherAlgorithm payloadCipher;
	
	public PF_ECDHPayloadEncrypter( KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase, byte[] encodedSessionKey, TemporaryBufferFactory bufferFactory, boolean rsaEnabled, StreamCipherAlgorithm payloadCipher ) throws CryptoException {
		this.bufferFactory = bufferFactory;
		this.rsaEnabled = rsaEnabled;
		
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
	
		this.payloadCipher = payloadCipher;
		
		this.sessionKey = KeyAgreementAlgorithm.ECDH384.decodeX509EncodedKey(encodedSessionKey);
		this.messageKey = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();

		byte[] sharedSecret = KeyAgreementAlgorithm.ECDH384.agreeKey(messageKey, sessionKey);
		
		byte[] passphraseSecret = DigestAlgorithm.SHA_384.kdf(passphrase);
		
		byte[] kdf = DigestAlgorithm.SHA_384.kdf(ByteArray.append(encodedSessionKey,sharedSecret,passphraseSecret));

		byte[] aesKey = ByteArray.subArray(kdf, 0, payloadCipher.getKeyLength());
		byte[] aesIv = ByteArray.subArray(kdf, payloadCipher.getKeyLength(), payloadCipher.getIvLength());
		
		secretKey = new SecretKeySpec(aesKey, payloadCipher.getAlgorithm());
		secretIv =  new IvParameterSpec(aesIv);
		
		System.out.println("AES KEY: " + ByteArray.asHex(aesKey));
		System.out.println("AES IV: " + ByteArray.asHex(aesIv));
	}
	
	
	/* (non-Javadoc)
	 * @see org.tdmx.client.crypto.scheme.Encrypter#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws CryptoException {
		if ( fbos != null ) {
			throw new IllegalStateException();
		}
		fbos = bufferFactory.getOutputStream();
		Cipher c = payloadCipher.getEncrypter(secretKey, secretIv);
		CipherOutputStream cos = new CipherOutputStream(fbos, c);
		
		DeflaterOutputStream zos = new DeflaterOutputStream(cos, new Deflater(Deflater.DEFAULT_COMPRESSION, false), 512, false);
		
		sos = new SigningOutputStream(SignatureAlgorithm.SHA_384_RSA, ownSigningKey.getPrivate(), true, true, zos);
		return sos;
	}


	@Override
	public CryptoContext getResult() throws CryptoException {
		if ( fbos == null ) {
			throw new IllegalStateException();
		}
		if ( !fbos.isClosed() ) {
			throw new IllegalStateException();
		}
		byte[] msgKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(messageKey.getPublic());
		byte[] plaintextLengthBytes = NumberToOctetString.longToBytes(sos.getSize());
		byte[] encryptionContext = null;
		
		if ( rsaEnabled ) {
			AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey( otherSigningKey );
			byte[] encryptedMsgKey = rsa.encrypt(otherSigningKey, msgKey);
			encryptionContext = ByteArray.append(plaintextLengthBytes, encryptedMsgKey);
		} else {
			encryptionContext = ByteArray.append(plaintextLengthBytes, msgKey);
		}
		CryptoContext cc = new CryptoContext(fbos.getInputStream(), encryptionContext, sos.getSize(), fbos.getSize());
		return cc;
	}


}
