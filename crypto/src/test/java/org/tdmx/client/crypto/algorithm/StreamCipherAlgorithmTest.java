package org.tdmx.client.crypto.algorithm;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Before;
import org.junit.Test;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoException;
import static org.junit.Assert.assertArrayEquals;

public class StreamCipherAlgorithmTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAes_Stream() throws CryptoException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKey = StreamCipherAlgorithm.Aes256_CTR.generateNewKey();
		IvParameterSpec secretIv = StreamCipherAlgorithm.Aes256_CTR.generateNewIv();
		
		byte[] plaintextOriginal = EntropySource.getRandomBytes(9999);
		Cipher c = StreamCipherAlgorithm.Aes256_CTR.getEncrypter(secretKey, secretIv);
		byte[] ciphertext = c.doFinal(plaintextOriginal);
		
		Cipher d = StreamCipherAlgorithm.Aes256_CTR.getDecrypter(secretKey, secretIv);
		byte[] plaintext = d.doFinal(ciphertext);
		
		assertArrayEquals(plaintextOriginal, plaintext);
	}

	@Test
	public void testAes_Block() throws CryptoException {
		SecretKeySpec secretKey = StreamCipherAlgorithm.Aes256_CTR.generateNewKey();
		IvParameterSpec secretIv = StreamCipherAlgorithm.Aes256_CTR.generateNewIv();
		
		byte[] plaintextOriginal = EntropySource.getRandomBytes(9999);
		byte[] ciphertext = StreamCipherAlgorithm.Aes256_CTR.encrypt(secretKey, secretIv, plaintextOriginal);
		
		byte[] plaintext = StreamCipherAlgorithm.Aes256_CTR.decrypt(secretKey, secretIv, ciphertext);
		
		assertArrayEquals(plaintextOriginal, plaintext);
	}

	@Test
	public void testTwofish_Stream() throws CryptoException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKey = StreamCipherAlgorithm.Twofish256_CTR.generateNewKey();
		IvParameterSpec secretIv = StreamCipherAlgorithm.Twofish256_CTR.generateNewIv();
		
		byte[] plaintextOriginal = EntropySource.getRandomBytes(9999);
		Cipher c = StreamCipherAlgorithm.Twofish256_CTR.getEncrypter(secretKey, secretIv);
		byte[] ciphertext = c.doFinal(plaintextOriginal);
		
		Cipher d = StreamCipherAlgorithm.Twofish256_CTR.getDecrypter(secretKey, secretIv);
		byte[] plaintext = d.doFinal(ciphertext);
		
		assertArrayEquals(plaintextOriginal, plaintext);
	}

	@Test
	public void testTwofish_Block() throws CryptoException {
		SecretKeySpec secretKey = StreamCipherAlgorithm.Twofish256_CTR.generateNewKey();
		IvParameterSpec secretIv = StreamCipherAlgorithm.Twofish256_CTR.generateNewIv();
		
		byte[] plaintextOriginal = EntropySource.getRandomBytes(9999);
		byte[] ciphertext = StreamCipherAlgorithm.Twofish256_CTR.encrypt(secretKey, secretIv, plaintextOriginal);
		
		byte[] plaintext = StreamCipherAlgorithm.Twofish256_CTR.decrypt(secretKey, secretIv, ciphertext);
		
		assertArrayEquals(plaintextOriginal, plaintext);
	}

	@Test
	public void testSerpent_Stream() throws CryptoException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKey = StreamCipherAlgorithm.Serpent256_CTR.generateNewKey();
		IvParameterSpec secretIv = StreamCipherAlgorithm.Serpent256_CTR.generateNewIv();
		
		byte[] plaintextOriginal = EntropySource.getRandomBytes(9999);
		Cipher c = StreamCipherAlgorithm.Serpent256_CTR.getEncrypter(secretKey, secretIv);
		byte[] ciphertext = c.doFinal(plaintextOriginal);
		
		Cipher d = StreamCipherAlgorithm.Serpent256_CTR.getDecrypter(secretKey, secretIv);
		byte[] plaintext = d.doFinal(ciphertext);
		
		assertArrayEquals(plaintextOriginal, plaintext);
	}

	@Test
	public void testSerpent_Block() throws CryptoException {
		SecretKeySpec secretKey = StreamCipherAlgorithm.Serpent256_CTR.generateNewKey();
		IvParameterSpec secretIv = StreamCipherAlgorithm.Serpent256_CTR.generateNewIv();
		
		byte[] plaintextOriginal = EntropySource.getRandomBytes(9999);
		byte[] ciphertext = StreamCipherAlgorithm.Serpent256_CTR.encrypt(secretKey, secretIv, plaintextOriginal);
		
		byte[] plaintext = StreamCipherAlgorithm.Serpent256_CTR.decrypt(secretKey, secretIv, ciphertext);
		
		assertArrayEquals(plaintextOriginal, plaintext);
	}

}
