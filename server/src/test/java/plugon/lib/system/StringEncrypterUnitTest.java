package plugon.lib.system;

import plugon.lib.system.StringEncrypter;
import junit.framework.TestCase;

/**
 * JUnit unit tests for BCrypt routines
 * @author Damien Miller
 * @version 0.2
 */
public class StringEncrypterUnitTest extends TestCase {

	protected String clearText = "This is a simple string which will be encrypted into some other string - silly";
	protected String cipherText = "PmDt6ocl/NfPlYL2XQtDq3MfnDHRuXP3IxuPzlPbzWHe23AVCcvK2xDGKp4S5gcl+bf5HQW7oddXfFCAlw4eMmi5HA35jTCQGtGpSKkwZkw=";
	protected StringEncrypter staticEncrypter = new StringEncrypter("This is a Reused object!!");
	protected StringEncrypter devEncrypter = new StringEncrypter("W1v3i6vo7F");
    
	/**
	 * Test method for 'BCrypt.hashpw(String, String)'
	 */
	public void testEncrypt() {

        System.out.println();
        System.out.println("+----------------------------------------+");
        System.out.println("|  -- Test Using Pass Phrase Method --   |");
        System.out.println("+----------------------------------------+");
        System.out.println();

        String secretString = "Attack at dawn!";
        String passPhrase   = "My Pass Phrase";

        // Create encrypter/decrypter class
        StringEncrypter desEncrypter = new StringEncrypter(passPhrase);

        // Encrypt the string
        String desEncrypted       = desEncrypter.encrypt(secretString);

        // Decrypt the string
        String desDecrypted       = desEncrypter.decrypt(desEncrypted);

        // Print out values
        System.out.println("PBEWithMD5AndDES Encryption algorithm");
        System.out.println("    Original String  : " + secretString);
        System.out.println("    Encrypted String : " + desEncrypted);
        System.out.println("    Decrypted String : " + desDecrypted);
        System.out.println();
        
        assertEquals( secretString, desDecrypted);
	}
	
	public void testDecrypt() throws Exception {
		assertEquals("secret", devEncrypter.decrypt("TBOyUyE7glM="));
		assertNull(devEncrypter.decrypt("YKU6bg3GcI5Qhu8Z0/hSpTKAReIW2j1zPS/VgOije4o="));
	}
	
	/**
	 * The idea of this test is to verify the re-initialization of the cipher
	 * after an Exception has been thrown
	 */
	public void testReInitCipher() {
		StringEncrypter enc = new StringEncrypter("n8chtway");
		
		//No error
		String plain = enc.decrypt("O8963XdiXNU=");
		assertNotNull(plain);
		//BadPaddingException -> Cipher re-initialization
		plain = enc.decrypt("FilBpgxX4Sc=");
		assertNull(plain);
		//No error
		plain = enc.decrypt("O8963XdiXNU=");
		assertNotNull(plain);
	}
}
