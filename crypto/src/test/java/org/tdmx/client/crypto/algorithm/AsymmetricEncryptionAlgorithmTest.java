package org.tdmx.client.crypto.algorithm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public class AsymmetricEncryptionAlgorithmTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testKeyInfos() throws CryptoException {
		KeyPair kp = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		assertEquals( "RSA", kp.getPrivate().getAlgorithm() );
		assertEquals( "PKCS#8", kp.getPrivate().getFormat() );

		assertEquals( "RSA", kp.getPublic().getAlgorithm() );
		assertEquals( "X.509", kp.getPublic().getFormat() );
	}

	@Test
	public void testGetAlgorithmFromKey() throws CryptoException, NoSuchAlgorithmException {
		KeyPair kp2048 = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		
		assertEquals( AsymmetricEncryptionAlgorithm.RSA2048, AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey(kp2048.getPublic()));

	}

	@Test
	public void testEncryptDecrypt() throws CryptoException, NoSuchAlgorithmException {
		KeyPair kp2048 = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		
		byte[] content = EntropySource.getRandomBytes(214);
		byte[] ciphertext = AsymmetricEncryptionAlgorithm.RSA2048.encrypt(kp2048.getPublic(), content);
		assertEquals(AsymmetricEncryptionAlgorithm.RSA2048.getKeyLengthInBytes(), ciphertext.length);
		byte[] plaintext = AsymmetricEncryptionAlgorithm.RSA2048.decrypt(kp2048.getPrivate(), ciphertext);
		assertArrayEquals(content, plaintext);

	}

	@Test
	public void testEncryptTooLong() throws CryptoException, NoSuchAlgorithmException {
		KeyPair kp2048 = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
		
		byte[] content = EntropySource.getRandomBytes(246);
		try {
			AsymmetricEncryptionAlgorithm.RSA2048.encrypt(kp2048.getPublic(), content);
			fail();
		} catch ( CryptoException e ) {
			assertEquals( CryptoResultCode.ERROR_PK_BLOCKSIZE_INVALID, e.getRc());
		}

	}
}
