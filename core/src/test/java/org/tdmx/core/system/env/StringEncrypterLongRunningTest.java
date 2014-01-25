package org.tdmx.core.system.env;


import org.tdmx.core.system.AsyncTestRunner;
import org.tdmx.core.system.env.StringEncrypter;

import junit.framework.TestCase;

public class StringEncrypterLongRunningTest extends TestCase {

	protected String clearText = "This is a simple string which will be encrypted into some other string - silly";
	protected String cipherText = "xuqqNQPgUw/+XB26DLH1rukbS0BINoJkv4FyKq+aMtUxVfsFN9YP9aW61u6a092gt/QU0+fwrRUAjnnFPz4I5QjhH6WfUevRSj7w3pgE6rI=";

	protected StringEncrypter encrypter = new StringEncrypter("This is a Reused object!!");
    
	public void doTestDecrypt() throws Exception {
		//System.out.println(encrypter.encrypt(clearText));
		assertEquals(clearText, encrypter.decrypt(cipherText));
	}
	
	public void testMultiThreadedDecrypt() throws Exception {
		AsyncTestRunner.runDuration(this, "doTestDecrypt", 5, 60000);
	}
	
	public void testMultipleSingleThreaded() {
	    for( int i = 0; i < 100000; i ++){
			String t1 = encrypter.encrypt(clearText);
			assertEquals(cipherText,t1);
			String t2 = encrypter.decrypt(t1);
			assertEquals(clearText,t2);
	    }
	}
	
	public void testSingleThreadedGuarded() {
	    for( int i = 0; i < 100000; i ++){
	    	synchronized(this){
				String t1 = encrypter.encrypt(clearText);
				assertEquals(cipherText,t1);
	    	}
	    	synchronized(this) {
				String t2 = encrypter.decrypt(cipherText);
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
