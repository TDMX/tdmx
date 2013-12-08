package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.search.SearchService;


public class DnsResolverServiceImpl implements DnsResolverService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private ObjectRegistry objectRegistry;
	private SearchService searchService;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	

	@Override
	public List<FieldError> createOrUpdate(DnsResolverListDO resolverList) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		List<FieldError> validation = resolverList.validate();
		if ( !validation.isEmpty() ) {
			return validation;
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
			}
		}
		return validation;
	}

	@Override
	public void delete(DnsResolverListDO existing) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		objectRegistry.notifyRemove(existing, holder);
		searchService.update(holder);
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

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
