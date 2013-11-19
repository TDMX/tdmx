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
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoContext;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.Encrypter;
import org.tdmx.client.crypto.stream.FileBackedOutputStream;
import org.tdmx.client.crypto.stream.SigningOutputStream;

/**
 * encryption( M, PF, (K-A,K-a), K-B, A-B ) -> E, L
 * {
 * This scheme passes the secret keys used to decrypt the message payload to the 
 * destination in encrypted form in the encryption-context. 
 * 
 * validate A-B must be a 384bit X.509 encoded EC sessionKey.
 * EC key generate (A-A,A-a), an EC keypair on secp384r1
 * ECDH key agreement (A-a,A-B) => shared secret S
 * PFS := SHA384(PF) - convert the PF into a shared secret.
 * SKk || IVk:= SHA384(A-B||S||PFS), 
 *   where SKk is a 256bit AES encryption key, 
 *   IVk is a 128bit initialization vector for the AES encryption
 * SKe := PRNG(256bit)
 * IVe := PRNG(128bit)
 * E := AES256/CTR(SKe,IVe,ZLib-compress(M||Sign(K-a,M||long-byte-len(M))))
 * L := long-byte-len(M) || short-byte-len(A-A) || A-A || AES256/CTR(SKk,IVk, SKe || IVe )
 *   where long-byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer, and short-byte-len is a single byte representing the length of A-A in bytes.
 *   A-A is a X.509 encoded EC public key - aka the sender’s messageKey
 * }
 * 
 * @author Peter
 *
 */
public class PF_ECDHContextEncrypter implements Encrypter {

	private TemporaryBufferFactory bufferFactory;
	
	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	private PublicKey sessionKey;
	private KeyPair messageKey;
	private boolean rsaEnabled;
	
	private StreamCipherAlgorithm keyEncryptionCipher;
	private SecretKeySpec keyEncryptionKey;
	private IvParameterSpec keyEncryptionIv;
	
	private FileBackedOutputStream fbos = null;
	private SigningOutputStream sos = null;
	
	private StreamCipherAlgorithm payloadCipher;
	private byte[] payloadSecretKey;
	private byte[] payloadSecretIv;
	
	public PF_ECDHContextEncrypter( KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase, byte[] encodedSessionKey, TemporaryBufferFactory bufferFactory, boolean rsaEnabled, StreamCipherAlgorithm keyEncryptionCipher, StreamCipherAlgorithm payloadCipher ) throws CryptoException {
		this.bufferFactory = bufferFactory;
		this.rsaEnabled = rsaEnabled;
		
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
	
		this.keyEncryptionCipher = keyEncryptionCipher;
		this.payloadCipher = payloadCipher;
		
		this.sessionKey = KeyAgreementAlgorithm.ECDH384.decodeX509EncodedKey(encodedSessionKey);
		this.messageKey = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();

		byte[] sharedSecret = KeyAgreementAlgorithm.ECDH384.agreeKey(messageKey, sessionKey);
		
		byte[] passphraseSecret = DigestAlgorithm.SHA_384.kdf(passphrase);
		
		byte[] kdf = DigestAlgorithm.SHA_384.kdf(ByteArray.append(encodedSessionKey, sharedSecret,passphraseSecret));

		byte[] aesKey = ByteArray.subArray(kdf, 0, payloadCipher.getKeyLength());
		byte[] aesIv = ByteArray.subArray(kdf, payloadCipher.getKeyLength(), payloadCipher.getIvLength());
		
		keyEncryptionKey = new SecretKeySpec(aesKey, keyEncryptionCipher.getAlgorithm());
		keyEncryptionIv =  new IvParameterSpec(aesIv);
		
		System.out.println("KeyEncryption KEY: " + ByteArray.asHex(aesKey));
		System.out.println("KeyEncryption IV: " + ByteArray.asHex(aesIv));
		
		payloadSecretKey = EntropySource.getRandomBytes(payloadCipher.getKeyLength());
		payloadSecretIv = EntropySource.getRandomBytes(payloadCipher.getIvLength());

		System.out.println("Payload KEY: " + ByteArray.asHex(payloadSecretKey));
		System.out.println("Payload IV: " + ByteArray.asHex(payloadSecretIv));
		
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
		
		SecretKeySpec payloadKey = payloadCipher.convertKey(payloadSecretKey);
		IvParameterSpec payloadIv = payloadCipher.convertIv(payloadSecretIv);
		
		Cipher c = payloadCipher.getEncrypter(payloadKey, payloadIv);
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
		byte[] msgKeyLen = NumberToOctetString.intToByte(msgKey.length);
		byte[] plaintextLengthBytes = NumberToOctetString.longToBytes(sos.getSize());
		byte[] payloadKeyBytes = ByteArray.append(payloadSecretKey, payloadSecretIv);
		
		if ( rsaEnabled ) {
			AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey( otherSigningKey );
			payloadKeyBytes = rsa.encrypt(otherSigningKey, payloadKeyBytes);
		}
		byte[] encryptedKey = keyEncryptionCipher.encrypt(keyEncryptionKey, keyEncryptionIv, payloadKeyBytes);
		byte[] encryptionContext = ByteArray.append(plaintextLengthBytes, msgKeyLen, msgKey, encryptedKey);

		CryptoContext cc = new CryptoContext(fbos.getInputStream(), encryptionContext, sos.getSize(), fbos.getSize());
		return cc;
	}


}
