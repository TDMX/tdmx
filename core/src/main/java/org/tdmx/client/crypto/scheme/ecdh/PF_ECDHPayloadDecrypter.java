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
 *E := AES256/CTR(SKe,IVe,ZLib-decompress(M||Sign(K-a,M||byte-len(M))))
 *L := byte-len(M) || A-A
 *  where byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer.
 *  A-A is a X.509 encoded EC public key
 *
 *PFS := SHA384(PF) - convert the PF into a shared secret.
 *ECDH key agreement (A-b,A-A) => shared secret S
 *SKe || IVe:= SHA384(A-B||S||PFS), 
 *  
 *M || Sign(K-a,M) := Decompress(byte-len(M), AES256/CTR(SKe,IVe,E))
 *  where decompression fails if invalid stream or if decompressed length > byte-len(M) or stream ends before byte-len(M) bytes are decompressed.
 *verify(K-A, M, Sign(K-a,M)) and fail if signature incorrect.
 *}
 *
 * @author Peter
 *
 */
public class PF_ECDHPayloadDecrypter implements Decrypter {

	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	private KeyPair sessionKey;
	private byte[] passphrase;
	private boolean rsaEnabled;
	private StreamCipherAlgorithm payloadCipher;
	
	public PF_ECDHPayloadDecrypter( KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase, KeyPair sessionKey, boolean rsaEnabled, StreamCipherAlgorithm payloadCipher ) throws CryptoException {
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
		this.sessionKey = sessionKey;
		this.passphrase = passphrase;
		this.rsaEnabled = rsaEnabled;
		this.payloadCipher = payloadCipher;
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
		byte[] lengthBytes = ByteArray.subArray(encryptionContext, 0, 8);
		long plaintextLength = NumberToOctetString.bytesToLong(lengthBytes);
		
		byte[] messageKeyBytes = ByteArray.subArray(encryptionContext, 8, encryptionContext.length - 8);
		if ( rsaEnabled ) {
			AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey( ownSigningKey.getPublic() );
			messageKeyBytes = rsa.decrypt(ownSigningKey.getPrivate(),messageKeyBytes);
		}
		
		PublicKey messageKey = KeyAgreementAlgorithm.ECDH384.decodeX509EncodedKey(messageKeyBytes);
		byte[] sharedSecret = KeyAgreementAlgorithm.ECDH384.agreeKey(sessionKey, messageKey );
		
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(sessionKey.getPublic());
		byte[] passphraseSecret = DigestAlgorithm.SHA_384.kdf(passphrase);
		byte[] kdf = DigestAlgorithm.SHA_384.kdf(ByteArray.append(encodedSessionKey,sharedSecret,passphraseSecret));

		byte[] aesKey = ByteArray.subArray(kdf, 0, payloadCipher.getKeyLength());
		byte[] aesIv = ByteArray.subArray(kdf, payloadCipher.getKeyLength(), payloadCipher.getIvLength());
		
		SecretKeySpec secretKey = new SecretKeySpec(aesKey, payloadCipher.getAlgorithm());
		IvParameterSpec secretIv =  new IvParameterSpec(aesIv);
	
		System.out.println("AES KEY: " + ByteArray.asHex(aesKey));
		System.out.println("AES IV: " + ByteArray.asHex(aesIv));

		Cipher c = payloadCipher.getDecrypter(secretKey, secretIv);
		CipherInputStream cis = new CipherInputStream(encryptedData, c);
		
		InflaterInputStream zis = new InflaterInputStream(cis, new Inflater(false), 512);
		SignatureVerifyingInputStream sis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_384_RSA, otherSigningKey, plaintextLength, true, zis);
		
		return sis;
	}
	
}
