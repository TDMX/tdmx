package org.tdmx.console.application.domain;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;

public class AbstractDOTest {

	@Test
	public void testSuperImplicitConstructor() {
		ConnectionTestResultDO o = new ConnectionTestResultDO();
		assertNotNull(o.getId());

		ProblemDO p1 = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		assertNotNull(p1.getId());
		
		HttpProxyDO hp = new HttpProxyDO();
		assertNotNull(hp.getId());
		
		ServiceProviderDO sp = new ServiceProviderDO();
		assertNotNull(sp.getId());
	}
	
	@Test
	public void testGetNextObjectId() {
		ProblemDO p1 = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		ProblemDO p2 = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		
		assertFalse(p1.equals(p2));
		assertFalse(p1.getId().equals(p2.getId()));
		
	}

	@Test
	public void testClassNotEquals() {
		ConnectionTestResultDO c = new ConnectionTestResultDO();
		ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		HttpProxyDO h = new HttpProxyDO();
		ServiceProviderDO s = new ServiceProviderDO();
		
		String id = "1";
		c.setId(id);
		p.setId(id);
		h.setId(id);
		s.setId(id);
		
		assertFalse(c.equals(p));
		assertFalse(c.equals(h));
		assertFalse(c.equals(s));
		assertFalse(p.equals(h));
		assertFalse(p.equals(s));
		assertFalse(h.equals(s));
		
	}

}
