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
package org.tdmx.console.application.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.console.application.dao.ServiceProviderStoreMockImpl;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ObjectRegistryImpl;

public class SearchServiceTest {

	private SearchService service;
	private ObjectRegistry registry;
	private final int scale = 10;

	@Before
	public void setUp() throws Exception {
		ServiceProviderStoreMockImpl store = new ServiceProviderStoreMockImpl(scale);

		ObjectRegistryImpl r = new ObjectRegistryImpl();
		r.initContent(store.load());

		SearchServiceImpl s = new SearchServiceImpl();
		s.setObjectRegistry(r);
		s.initialize();
		service = s;
		registry = r;
	}

	@Test
	public void testParse() {
		// TODO
	}

	@Test
	public void testSearch_ServiceProvider() {
		Set<DomainObject> objects = service.search(DomainObjectType.ServiceProvider, "serviceprovider");

		for (ServiceProviderDO o : registry.getServiceProviders()) {
			assertTrue(objects.contains(o));
		}
	}

	@Test
	public void testSearch_ServiceProviderSpecific() {
		Set<DomainObject> objects = service.search(DomainObjectType.ServiceProvider, "serviceprovider0");

		assertEquals(1, objects.size()); // SP and it's proxy
		for (ServiceProviderDO o : registry.getServiceProviders()) {
			if (o.getSubjectIdentifier().indexOf("serviceprovider0") != -1) {
				assertTrue(objects.contains(o));
			} else {
				assertFalse(objects.contains(o));
			}
		}
	}

	@Test
	public void testSearch_ServiceProvider_ByIdentity() {
		Set<DomainObject> objects = service.search(DomainObjectType.ServiceProvider, "o=company0");

		assertEquals(1, objects.size()); // SP
		for (ServiceProviderDO o : registry.getServiceProviders()) {
			if (o.getSubjectIdentifier().indexOf("o=company0") != -1) {
				assertTrue(objects.contains(o));
			} else {
				assertFalse(objects.contains(o));
			}
		}
	}
}
