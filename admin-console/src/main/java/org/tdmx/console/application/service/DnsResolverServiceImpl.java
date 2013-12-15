package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.domain.validation.OperationError;
import org.tdmx.console.application.search.SearchService;
import org.tdmx.console.application.util.StringUtils;
import org.xbill.DNS.ResolverConfig;


public class DnsResolverServiceImpl implements DnsResolverService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	public static final String SYSTEM_DNS_RESOLVER_LIST_ID = "system-dns-resolver-list";
	public static final String SYSTEM_DNS_RESOLVER_LIST_NAME = "System";
	
	private ObjectRegistry objectRegistry;
	private SearchService searchService;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public DnsResolverListDO lookup(String id) {
		return objectRegistry.getDnsResolverList(id);
	}

	@Override
	public List<DnsResolverListDO> search(String criteria) {
		if ( StringUtils.hasText(criteria)) {
			List<DnsResolverListDO> result = new ArrayList<>();
			Set<DnsResolverListDO> found = searchService.search(DomainObjectType.DnsResolverList, criteria);
			for( DomainObject o : found ) {
				result.add((DnsResolverListDO)o );
			}
			return result;
		}
		return objectRegistry.getDnsResolverLists();
	}

	@Override
	public void updateSystemResolverList() {
		DnsResolverListDO systemList = objectRegistry.getDnsResolverList(SYSTEM_DNS_RESOLVER_LIST_ID);
		if ( systemList == null ) {
			DomainObjectChangesHolder h = new DomainObjectChangesHolder();
			systemList = new DnsResolverListDO();
			systemList.setId(SYSTEM_DNS_RESOLVER_LIST_ID);
			systemList.setActive(Boolean.TRUE);
			systemList.setName(SYSTEM_DNS_RESOLVER_LIST_NAME);
			systemList.setHostnames(getSystemDnsHostnames());
			objectRegistry.notifyAdd(systemList, h);
			searchService.update(h);
			// TODO audit log 
		} else {
			DnsResolverListDO systemListCopy = new DnsResolverListDO(systemList);
			systemListCopy.setHostnames(getSystemDnsHostnames());
			createOrUpdate(systemListCopy);
		}
	}

	@Override
	public OperationError createOrUpdate(DnsResolverListDO resolverList) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		List<FieldError> validation = resolverList.validate();
		if ( !validation.isEmpty() ) {
			return new OperationError(validation);
		}
		DnsResolverListDO existing = objectRegistry.getDnsResolverList(resolverList.getId());
		if ( existing == null ) {
			objectRegistry.notifyAdd(resolverList, holder);
			searchService.update(holder);
		} else {
			DomainObjectFieldChanges changes = existing.merge(resolverList);
			if ( !changes.isEmpty() ) {
				objectRegistry.notifyModify(changes, holder);
				searchService.update(holder);
				
				//TODO if system's hostnames change then issue audit warning
			}
		}
		return null;
	}

	@Override
	public OperationError delete(DnsResolverListDO existing) {
		// not allowed to delete the "system" DNS resolver list.
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		objectRegistry.notifyRemove(existing, holder);
		searchService.update(holder);
		return null;
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------
	
	private List<String> getSystemDnsHostnames() {
		List<String> hosts= new ArrayList<>();
		
		String[] list = ResolverConfig.getCurrentConfig().servers();
		if ( list != null ) {
			for( String h : list ) {
				hosts.add(h);
			}
		}
		return Collections.unmodifiableList(hosts);
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

}
