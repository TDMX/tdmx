package org.tdmx.console.application.service;

import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.SystemProxyDO;
import org.tdmx.console.application.search.SearchService;


public class SystemProxyServiceImpl implements SystemProxyService {

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
	public void update(SystemProxyDO resolverList) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		SystemProxyDO existing = objectRegistry.getSystemProxy();
		DomainObjectFieldChanges changes = existing.merge(resolverList);
		if ( !changes.isEmpty() ) {
			objectRegistry.notifyModify(changes, holder);
			searchService.update(holder);
			//TODO - audit log
		}
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
