/**
 * 
 */
package org.tdmx.client.crypto.scheme.ecdh;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.algorithm.KeyAgreementAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.algorithm.StreamCipherAlgorithm;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;
import org.tdmx.client.crypto.scheme.Decrypter;
import org.tdmx.client.crypto.stream.SignatureVerifyingInputStream;

/**
 * decryption( PF, (K-B, K-b), (A-B, A-b), K-A, E, L ) -> M
 *{
 * E := AES256/CTR(SKe,IVe,ZLib-compress(M||Sign(K-a,M||long-byte-len(M))))
 * L := long-byte-len(M) || short-byte-len(A-A) || A-A || AES256/CTR(SKk,IVk, SKe1 || IVe1 || SKe2 || IVe2  )
 * PFS := SHA256(PF) - convert the PF into a shared secret.
 * ECDH key agreement (A-b,A-A) => shared secret S
 * SKk || IVk:= SHA384(A-B||S||PFS),
 * SKe1 || IVe1 || SKe2 || IVe2 := AES256/CTR-decrypt(SKk,IVk, AES256/CTR(SKk,IVk, SKe1 || IVe1 || SKe2 || IVe2  ))
 * M || Sign(K-a,M) := Twofish256/CTR-decrypt(SKe1,IVe1,AES256/CTR-decrypt(SKe2,IVe2,ZLib-decompress(byte-len(M),E))
 * where decompression fails if invalid stream or if decompressed length > byte-len(M) or stream ends before byte-len(M) bytes are decompressed.
 * verify(K-A, M, Sign(K-a,M)) and fail if signature incorrect.
 *}
 *
 * @author Peter
 *
 */
public class PF_ECDHContextCascadeDecrypter implements Decrypter {

	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	private KeyPair sessionKey;
	private byte[] passphrase;
	private boolean rsaEnabled;
	private StreamCipherAlgorithm innerPayloadCipher;
	private StreamCipherAlgorithm outerPayloadCipher;
	
	private StreamCipherAlgorithm keyEncryptionCipher;
	
	public PF_ECDHContextCascadeDecrypter( KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase, KeyPair sessionKey, boolean rsaEnabled, StreamCipherAlgorithm keyEncryptionCipher, StreamCipherAlgorithm innerPayloadCipher, StreamCipherAlgorithm outerPayloadCipher ) throws CryptoException {
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
		this.sessionKey = sessionKey;
		this.passphrase = passphrase;
		this.rsaEnabled = rsaEnabled;
		this.keyEncryptionCipher = keyEncryptionCipher;
		this.innerPayloadCipher = innerPayloadCipher;
		this.outerPayloadCipher = outerPayloadCipher;
	}

	/* (non-Javadoc)
	 * @see org.tdmx.client.crypto.scheme.Decrypter#getInputStream()
	 */
	@Override
	public InputStream getInputStream( InputStream encryptedData, byte[] encryptionContext ) throws CryptoException {
		if ( encryptionContext == null ) {
			throw new CryptoException(CryptoResultCode.ERROR_ENCRYPTION_CONTEXT_MISSING);
		}
		if ( encryptionContext.length < 8 ) {
			throw new CryptoException(CryptoResultCode.ERROR_ENCRYPTION_CONTEXT_INVALID);
			
		}
		//L := long-byte-len(M) || short-byte-len(A-A) || A-A || AES256/CTR(SKk,IVk, SKe1 || IVe1 ||  SKe2 || IVe2 )
		byte[] lengthBytes = ByteArray.subArray(encryptionContext, 0, 8);
		long plaintextLength = NumberToOctetString.bytesToLong(lengthBytes);
		
		int lengthSessionKey = NumberToOctetString.byteToInt(ByteArray.subArray(encryptionContext, 8, 1));
		
		byte[] messageKeyBytes = ByteArray.subArray(encryptionContext, 9, lengthSessionKey);
		
		byte[] encryptedKeyBytes = ByteArray.subArray(encryptionContext, 9+lengthSessionKey);

		PublicKey messageKey = KeyAgreementAlgorithm.ECDH384.decodeX509EncodedKey(messageKeyBytes);
		byte[] sharedSecret = KeyAgreementAlgorithm.ECDH384.agreeKey(sessionKey, messageKey );
		
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(sessionKey.getPublic());
		byte[] passphraseSecret = DigestAlgorithm.SHA_384.kdf(passphrase);
		byte[] kdf = DigestAlgorithm.SHA_384.kdf(ByteArray.append(encodedSessionKey,sharedSecret,passphraseSecret));

		byte[] keyKey = ByteArray.subArray(kdf, 0, keyEncryptionCipher.getKeyLength());
		byte[] keyIv = ByteArray.subArray(kdf, keyEncryptionCipher.getKeyLength(), keyEncryptionCipher.getIvLength());
		
		SecretKeySpec keyEncryptionKey = new SecretKeySpec(keyKey, keyEncryptionCipher.getAlgorithm());
		IvParameterSpec keyEncryptionIv =  new IvParameterSpec(keyIv);
	
		System.out.println("KeyEncryption KEY: " + ByteArray.asHex(keyKey));
		System.out.println("KeyEncryption IV: " + ByteArray.asHex(keyIv));

		byte[] payloadKeyBytes = keyEncryptionCipher.decrypt(keyEncryptionKey, keyEncryptionIv, encryptedKeyBytes);
		if ( rsaEnabled ) {
			AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey( ownSigningKey.getPublic() );
			payloadKeyBytes = rsa.decrypt(ownSigningKey.getPrivate(),payloadKeyBytes);
		}

		byte[] innerPayloadKey = ByteArray.subArray(payloadKeyBytes, 0, innerPayloadCipher.getKeyLength());
		byte[] innerPayloadIv = ByteArray.subArray(payloadKeyBytes, innerPayloadCipher.getKeyLength(), innerPayloadCipher.getIvLength());
		
		SecretKeySpec innerPayloadSecretKey = new SecretKeySpec(innerPayloadKey, innerPayloadCipher.getAlgorithm());
		IvParameterSpec innerPayloadSecretIv =  new IvParameterSpec(innerPayloadIv);

		System.out.println("Inner Payload KEY: " + ByteArray.asHex(innerPayloadKey));
		System.out.println("Inner Payload IV: " + ByteArray.asHex(innerPayloadIv));

		byte[] outerPayloadKey = ByteArray.subArray(payloadKeyBytes, innerPayloadCipher.getKeyLength()+innerPayloadCipher.getIvLength(), outerPayloadCipher.getKeyLength());
		byte[] outerPayloadIv = ByteArray.subArray(payloadKeyBytes, innerPayloadCipher.getKeyLength()+innerPayloadCipher.getIvLength()+outerPayloadCipher.getKeyLength(), outerPayloadCipher.getIvLength());
		
		SecretKeySpec outerPayloadSecretKey = new SecretKeySpec(outerPayloadKey, outerPayloadCipher.getAlgorithm());
		IvParameterSpec outerPayloadSecretIv =  new IvParameterSpec(outerPayloadIv);

		System.out.println("Outer Payload KEY: " + ByteArray.asHex(outerPayloadKey));
		System.out.println("Outer Payload IV: " + ByteArray.asHex(outerPayloadIv));

		Cipher ic = innerPayloadCipher.getDecrypter(innerPayloadSecretKey, innerPayloadSecretIv);
		CipherInputStream icis = new CipherInputStream(encryptedData, ic);
		
		Cipher oc = outerPayloadCipher.getDecrypter(outerPayloadSecretKey, outerPayloadSecretIv);
		CipherInputStream ocis = new CipherInputStream(icis, oc);
		
		InflaterInputStream zis = new InflaterInputStream(ocis, new Inflater(false), 512);
		SignatureVerifyingInputStream sis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_384_RSA, otherSigningKey, plaintextLength, true, zis);
		
		return sis;
	}
	
}
