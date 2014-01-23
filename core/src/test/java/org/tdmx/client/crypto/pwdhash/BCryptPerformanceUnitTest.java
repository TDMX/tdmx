package org.tdmx.client.crypto.pwdhash;

import org.tdmx.client.crypto.pwdhash.BCrypt_v03;

import junit.framework.TestCase;

/**
 * 
 */
public class BCryptPerformanceUnitTest extends TestCase {

	/**
	 * Entry point for unit tests
	 * @param args unused
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(BCryptPerformanceUnitTest.class);
	}

	/**
	 * 
	 */
	public void testCheckPerformance() {
		int MULT = 1;
		for( int i = 4; i < 15; i++) {
			String salt = BCrypt_v03.gensalt(i);
			String hashpw = BCrypt_v03.hashpw("my pwd", salt);
			long startTs = System.currentTimeMillis();
			for( int mult = 0; mult < MULT; mult++) {
				assertTrue(BCrypt_v03.checkpw("my pwd", hashpw));
			}
			long endTs = System.currentTimeMillis();
			System.out.println(""+i+": " + ((endTs-startTs)/MULT));
		}
	}

}
