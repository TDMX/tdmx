/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.console.application.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;

public class AbstractDOTest {

	@Test
	public void testSuperImplicitConstructor() {
		ProblemDO p1 = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		assertNotNull(p1.getId());

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
		ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		ServiceProviderDO s = new ServiceProviderDO();

		String id = "1";
		p.setId(id);
		s.setId(id);

		assertFalse(p.equals(s));

	}

}
