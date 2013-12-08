package org.tdmx.console.application.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.console.application.dao.ServiceProviderStoreMockImpl;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ObjectRegistryImpl;

public class SearchServiceTest {

	private SearchService service;
	private ObjectRegistry registry;
	private int scale = 10;
	
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
	public void testSuggestion() {
		fail("Not yet implemented");
	}

	@Test
	public void testParse() {
		fail("Not yet implemented");
	}

	@Test
	public void testSearch_ServiceProvider() {
		SearchCriteria c = service.parse("serviceprovider");
		Set<DomainObject> objects = service.search(c);
		
		for( ServiceProviderDO o : registry.getServiceProviders() ) {
			assertTrue( objects.contains(o) );
		}
	}

	@Test
	public void testSearch_ServiceProviderSpecific() {
		SearchCriteria c = service.parse("serviceprovider0");
		Set<DomainObject> objects = service.search(c);
		
		assertEquals( 2, objects.size()); // SP and it's proxy
		for( ServiceProviderDO o : registry.getServiceProviders() ) {
			if ( o.getSubjectIdentifier().indexOf("serviceprovider0") != -1 ) {
				assertTrue( objects.contains(o) );
			} else {
				assertFalse( objects.contains(o));
			}
		}
	}

	@Test
	public void testSearch_ServiceProvider_ByIdentity() {
		SearchCriteria c = service.parse("o=company0");
		Set<DomainObject> objects = service.search(c);
		
		assertEquals( 1, objects.size()); // SP
		for( ServiceProviderDO o : registry.getServiceProviders() ) {
			if ( o.getSubjectIdentifier().indexOf("o=company0") != -1 ) {
				assertTrue( objects.contains(o) );
			} else {
				assertFalse( objects.contains(o));
			}
		}
	}
}
