package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.visit.Traversal;
import org.tdmx.console.application.domain.visit.TraversalContextHolder;
import org.tdmx.console.application.domain.visit.TraversalFunction;
import org.tdmx.console.application.search.SearchService;


public class ProxyServiceImpl implements ProxyService {

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
	public boolean isDeleteWarning(final HttpProxyDO proxy) {
		Boolean found = Traversal.traverse( objectRegistry.getServiceProviders(), Boolean.FALSE, new TraversalFunction<ServiceProviderDO, Boolean>() {

			@Override
			public void visit(ServiceProviderDO object,
					TraversalContextHolder<Boolean> holder) {
				if ( object.getMasProxy() == proxy || object.getMrsProxy() == proxy || object.getMosProxy() == proxy || object.getMdsProxy() == proxy ) {
					holder.setResult(Boolean.TRUE);
					holder.stop();
				}					
			}
		});
		return found;
	}
	
	
	@Override
	public List<ERROR> create(HttpProxyDO proxy) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();

		List<ERROR> validation = proxy.validate();
		if ( validation.isEmpty() ) {
			objectRegistry.notifyAdd(proxy, holder);
			searchService.update(holder);
		}
		return validation;
	}

	@Override
	public List<ERROR> modify(HttpProxyDO proxy, HttpProxyDO existing) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();

		List<ERROR> validation = proxy.validate();
		if ( validation.isEmpty() ) {
			DomainObjectFieldChanges changes = existing.merge(proxy);
			if ( !changes.isEmpty() ) {
				objectRegistry.notifyModify(changes, holder);
				searchService.update(holder);
			}
		}
		return validation;
	}

	@Override
	public void delete(HttpProxyDO existing) {
		DomainObjectChangesHolder holder = new DomainObjectChangesHolder();
		for( ServiceProviderDO sp : objectRegistry.getServiceProviders() ) {
			boolean changedSp = false;
			ServiceProviderDO clonedSP = sp.copy();
			if ( clonedSP.getMasProxy() == existing ) {
				changedSp = true;
				clonedSP.setMasProxy(null);
			}
			if ( clonedSP.getMrsProxy() == existing ) {
				changedSp = true;
				clonedSP.setMrsProxy(null);
			}
			if ( clonedSP.getMosProxy() == existing ) {
				changedSp = true;
				clonedSP.setMosProxy(null);
			}
			if ( clonedSP.getMdsProxy() == existing ) {
				changedSp = true;
				clonedSP.setMdsProxy(null);
			}
			if ( changedSp ) {
				DomainObjectFieldChanges changes = sp.merge(clonedSP);
				if ( !changes.isEmpty() ) {
					objectRegistry.notifyModify(changes, holder);
				}
			}
		}
		objectRegistry.notifyRemove(existing, holder);
		searchService.update(holder);
	}

	@Override
	public List<String> getProxyTypes() {
		return HttpProxyDO.proxyTypes;
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
