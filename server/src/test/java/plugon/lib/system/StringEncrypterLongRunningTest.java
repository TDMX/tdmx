package plugon.lib.system;


import junit.framework.TestCase;
import plugon.lib.system.StringEncrypter;
import plugon.lib.system.test.AsyncTestRunner;

public class StringEncrypterLongRunningTest extends TestCase {

	protected String clearText = "This is a simple string which will be encrypted into some other string - silly";
	protected String cipherText = "PmDt6ocl/NfPlYL2XQtDq3MfnDHRuXP3IxuPzlPbzWHe23AVCcvK2xDGKp4S5gcl+bf5HQW7oddXfFCAlw4eMmi5HA35jTCQGtGpSKkwZkw=";
	protected StringEncrypter staticEncrypter = new StringEncrypter("This is a Reused object!!");
	protected StringEncrypter devEncrypter = new StringEncrypter("W1v3i6vo7F");
    
	public void doTestDecrypt() throws Exception {
		assertEquals("secret", devEncrypter.decrypt("TBOyUyE7glM="));
		assertNull(devEncrypter.decrypt("YKU6bg3GcI5Qhu8Z0/hSpTKAReIW2j1zPS/VgOije4o="));
	}
	
	public void testMultiThreadedDecrype() throws Exception {
		AsyncTestRunner.runDuration(this, "doTestDecrypt", 5, 60000);
	}
	
	public void testMultipleSingleThreaded() {
	    for( int i = 0; i < 100000; i ++){
			String t1 = staticEncrypter.encrypt(clearText);
			assertEquals(cipherText,t1);
			String t2 = staticEncrypter.decrypt(t1);
			assertEquals(clearText,t2);
	    }
	}
	
	public void testSingleThreadedGuarded() {
	    for( int i = 0; i < 100000; i ++){
	    	synchronized(this){
				String t1 = staticEncrypter.encrypt(clearText);
				assertEquals(cipherText,t1);
	    	}
	    	synchronized(this) {
				String t2 = staticEncrypter.decrypt(cipherText);
				assertEquals(clearText,t2);
	    	}
	    }
	}
	
	public void testMultiThreadedGuarded() throws Exception {
		AsyncTestRunner.runDuration(this, "testSingleThreadedGuarded", 5, 60000);
	}

	public void testMultiThreaded() throws Exception {
		AsyncTestRunner.runDuration(this, "testMultipleSingleThreaded", 5, 60000);
	}
}
