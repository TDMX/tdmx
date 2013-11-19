package org.tdmx.client.crypto.pwdhash;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PwdHashUnitTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHashpw_static() {
		String pwd = "mycrappypwd";
		
		String pwdHash = PwdHashImpl.hashpw(pwd);
		assertNotNull(pwdHash);
		assertTrue(PwdHashImpl.checkpw(pwd, pwdHash ));
	}

	@Test
	public void testCheckpw() {
		String pwd = "another!e09r.c.ejflmCrappyPwd";
		
		PwdHash h = new PwdHashImpl();
		String pwdHash = h.hash(pwd);
		assertNotNull(pwdHash);
		assertTrue(h.check(pwd, pwdHash ));
	}

}
