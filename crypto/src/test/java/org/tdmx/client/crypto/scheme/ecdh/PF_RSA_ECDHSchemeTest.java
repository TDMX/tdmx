package org.tdmx.client.crypto.scheme.ecdh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;

import org.junit.Before;
import org.junit.Test;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.KeyAgreementAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;
import org.tdmx.client.crypto.converters.StringToUtf8;
import org.tdmx.client.crypto.scheme.CryptoContext;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoScheme;
import org.tdmx.client.crypto.scheme.CryptoSchemeFactory;
import org.tdmx.client.crypto.scheme.Decrypter;
import org.tdmx.client.crypto.scheme.Encrypter;
import org.tdmx.client.crypto.scheme.SchemeTester;

public class PF_RSA_ECDHSchemeTest {

	private CryptoSchemeFactory ownFactory;
	private CryptoSchemeFactory otherFactory;
	private TemporaryBufferFactory bufferFactory = new TemporaryFileManagerImpl(1024,1024);
	@Before
	public void setup() throws CryptoException {
		KeyPair ownSigningKeyPair = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		KeyPair otherSigningKeyPair = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		
		ownFactory = new CryptoSchemeFactory(ownSigningKeyPair, otherSigningKeyPair.getPublic(), bufferFactory);
		assertNotNull(ownFactory);
		otherFactory = new CryptoSchemeFactory(otherSigningKeyPair, ownSigningKeyPair.getPublic(), bufferFactory);
		assertNotNull(otherFactory);
	}
	
	@Test
	public void testEncrypt() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey =  KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());
		
		byte[] passphrase = StringToUtf8.toBytes("wtf");
		
		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_AES256plusTWOFISH256, passphrase, encodedSessionKey);
		assertNotNull(e);
		
		OutputStream os = e.getOutputStream();
		os.write((int)101);
		os.flush();
		os.close();
		
		CryptoContext result = e.getResult();
		assertNotNull(result);
		assertEquals(1, result.plaintextLength);
		assertNotNull(result.getEncryptedData());
		assertNotNull(result.getEncryptionContext());
		assertNotNull(result.getCiphertextLength());
	}

	@Test
	public void testEncryptDecrypt_SingleByte() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey =  KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());
		
		byte[] passphrase = StringToUtf8.toBytes("wtf");
		
		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_AES256plusTWOFISH256, passphrase, encodedSessionKey);
		assertNotNull(e);
		
		byte p = 101;
		
		OutputStream os = e.getOutputStream();
		os.write((int)p);
		os.flush();
		os.close();
		
		CryptoContext result = e.getResult();

		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_AES256plusTWOFISH256, passphrase, session);

		InputStream is = d.getInputStream(result.getEncryptedData(), result.getEncryptionContext());
		try {
			byte dp = (byte)is.read();
			
			assertEquals(p, dp);
			
			assertEquals(-1, is.read());
		} finally {
			is.close();
		}
	}

	@Test
	public void testEncryptDecrypt_AesTwofish_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey =  KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());
		
		byte[] passphrase = StringToUtf8.toBytes("wtf");
		
		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_AES256plusTWOFISH256, passphrase, encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_AES256plusTWOFISH256, passphrase, session);
		assertNotNull(d);
		
		SchemeTester.testLargeCompressable(e,d);
	}

	@Test
	public void testEncryptDecrypt_AesSerpent_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey =  KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());
		
		byte[] passphrase = StringToUtf8.toBytes("wtf");
		
		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_AES256plusSERPENT256, passphrase, encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_AES256plusSERPENT256, passphrase, session);
		assertNotNull(d);
		
		SchemeTester.testLargeCompressable(e,d);
	}

	@Test
	public void testEncryptDecrypt_TwofishAes_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey =  KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());
		
		byte[] passphrase = StringToUtf8.toBytes("wtf");
		
		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusAES256, passphrase, encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusAES256, passphrase, session);
		assertNotNull(d);
		
		SchemeTester.testLargeCompressable(e,d);
	}

	@Test
	public void testEncryptDecrypt_TwofishSerpent_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey =  KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());
		
		byte[] passphrase = StringToUtf8.toBytes("wtf");
		
		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusSERPENT256, passphrase, encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_TWOFISH256plusSERPENT256, passphrase, session);
		assertNotNull(d);
		
		SchemeTester.testLargeCompressable(e,d);
	}

	@Test
	public void testEncryptDecrypt_SerpentAes_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey =  KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());
		
		byte[] passphrase = StringToUtf8.toBytes("wtf");
		
		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusAES256, passphrase, encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusAES256, passphrase, session);
		assertNotNull(d);
		
		SchemeTester.testLargeCompressable(e,d);
	}

	@Test
	public void testEncryptDecrypt_SerpentTwofish_LargeContent() throws CryptoException, IOException {
		KeyPair session = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();
		byte[] encodedSessionKey =  KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(session.getPublic());
		
		byte[] passphrase = StringToUtf8.toBytes("wtf");
		
		Encrypter e = ownFactory.getECDHEncrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusTWOFISH256, passphrase, encodedSessionKey);
		assertNotNull(e);
		Decrypter d = otherFactory.getECDHDecrypter(CryptoScheme.RSA_SLASH_PF_RSA_ECDH384_SERPENT256plusTWOFISH256, passphrase, session);
		assertNotNull(d);
		
		SchemeTester.testLargeCompressable(e,d);
	}

}
