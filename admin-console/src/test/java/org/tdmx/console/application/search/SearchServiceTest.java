package org.tdmx.console.application.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.tdmx.console.application.dao.ServiceProviderStoreMockImpl;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.HttpProxyDO;
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
	public void testSearch() {
		SearchCriteria c = service.parse("serviceprovider");
		Set<DomainObject> objects = service.search(c);
		
		for( ServiceProviderDO o : registry.getServiceProviders() ) {
			assertTrue( objects.contains(o) );
		}
		for( HttpProxyDO o : registry.getHttpProxies() ) {
			assertTrue( objects.contains(o) );
		}
		assertEquals( registry.getHttpProxies().size()+registry.getServiceProviders().size(), objects.size() );
	}

}
