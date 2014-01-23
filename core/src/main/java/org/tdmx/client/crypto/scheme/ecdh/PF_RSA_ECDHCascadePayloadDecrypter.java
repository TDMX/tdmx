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
 * {
 * E := AES256/CTR(SKe-aes,IVe-aes,
 *            Twofish256/CTR(SKe-twofish, IVe-twofish
 * 		ZLib-compress(M||Sign(K-a,M||long-byte-len(M)))
 * 
 * L := long-byte-len(M) 
 * RSA/ECB/OAEPWithSHA1AndMGF1Padding-encrypt( K-B, RS || A-A )
 * 
 * PFS := SHA384(PF) - convert the PF into a shared secret.
 * RS || A-A := RSA/ECB/OAEPWithSHA1AndMGF1Padding-decrypt( K-b, L )
 * 
 * ECDH key agreement (A-b,A-A) => shared secret S
 * ECS := SHA384(A-B||S||PFS) 
 * 
 * CSKM := bytewise interleave ECS + RS in that order
 * 
 * SKk-aes || IVk-aes := first 384 bits of CSKM 
 * SKk-twofish || IVk-twofish := second 384s bit of CSKM 
 * 
 * M || Sign(K-a,M) := AES256/CTR-decrypt(SKe-aes,IVe-aes,
 * 	Twofish256/CTR-decrypt(SKe-twofish,IVe-twofish,
 * ZLib-decompress(byte-len(M),E)))
 *   where decompression fails if invalid stream or if decompressed length > long-byte-len(M) or stream ends before long-byte-len(M) bytes are decompressed.
 * verify(K-A, M, Sign(K-a,M)) and fail if signature incorrect.
 * }
 *
 * @author Peter
 *
 */
public class PF_RSA_ECDHCascadePayloadDecrypter implements Decrypter {

	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	private KeyPair sessionKey;
	private byte[] passphrase;

	private StreamCipherAlgorithm innerPayloadCipher;
	private StreamCipherAlgorithm outerPayloadCipher;
	
	
	public PF_RSA_ECDHCascadePayloadDecrypter( KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase, KeyPair sessionKey, StreamCipherAlgorithm innerPayloadCipher, StreamCipherAlgorithm outerPayloadCipher ) throws CryptoException {
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
		this.sessionKey = sessionKey;
		this.passphrase = passphrase;
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
		byte[] lengthBytes = ByteArray.subArray(encryptionContext, 0, 8);
		long plaintextLength = NumberToOctetString.bytesToLong(lengthBytes);
		
		byte[] encryptedContextBytes = ByteArray.subArray(encryptionContext, 8, encryptionContext.length - 8);
		AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey( ownSigningKey.getPublic() );
		byte[] plaintextContextBytes = rsa.decrypt(ownSigningKey.getPrivate(),encryptedContextBytes);
		byte[] rs = ByteArray.subArray(plaintextContextBytes, 0, 48);
		
		byte[] messageKeyBytes = ByteArray.subArray(plaintextContextBytes,48, plaintextContextBytes.length-48);

		PublicKey messageKey = KeyAgreementAlgorithm.ECDH384.decodeX509EncodedKey(messageKeyBytes);
		byte[] sharedSecret = KeyAgreementAlgorithm.ECDH384.agreeKey(sessionKey, messageKey );
		
		byte[] encodedSessionKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(sessionKey.getPublic());
		byte[] passphraseSecret = DigestAlgorithm.SHA_384.kdf(passphrase);
		byte[] kdf = DigestAlgorithm.SHA_384.kdf(ByteArray.append(encodedSessionKey, sharedSecret,passphraseSecret));

		byte[] combinedKey = ByteArray.interleave(kdf, rs);
		
		byte[] iKey = ByteArray.subArray(combinedKey, 0, innerPayloadCipher.getKeyLength());
		byte[] iIv = ByteArray.subArray(combinedKey, innerPayloadCipher.getKeyLength(), innerPayloadCipher.getIvLength());
		SecretKeySpec innerKey = new SecretKeySpec(iKey, innerPayloadCipher.getAlgorithm());
		IvParameterSpec innerIv =  new IvParameterSpec(iIv);
		
		System.out.println("inner KEY: " + ByteArray.asHex(iKey));
		System.out.println("inner IV: " + ByteArray.asHex(iIv));

		byte[] oKey = ByteArray.subArray(combinedKey, innerPayloadCipher.getKeyLength()+innerPayloadCipher.getIvLength(), outerPayloadCipher.getKeyLength());
		byte[] oIv = ByteArray.subArray(combinedKey, innerPayloadCipher.getKeyLength()+innerPayloadCipher.getIvLength()+outerPayloadCipher.getKeyLength(), outerPayloadCipher.getIvLength());
		SecretKeySpec outerKey = new SecretKeySpec(oKey, outerPayloadCipher.getAlgorithm());
		IvParameterSpec outerIv =  new IvParameterSpec(oIv);
		

		Cipher ic = innerPayloadCipher.getDecrypter(innerKey, innerIv);
		CipherInputStream icis = new CipherInputStream(encryptedData, ic);
		
		Cipher oc = outerPayloadCipher.getDecrypter(outerKey, outerIv);
		CipherInputStream ocis = new CipherInputStream(icis, oc);
		
		InflaterInputStream zis = new InflaterInputStream(ocis, new Inflater(false), 512);
		SignatureVerifyingInputStream sis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_384_RSA, otherSigningKey, plaintextLength, true, zis);
		
		return sis;
	}
	
}
