package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.domain.Domain;

public interface ObjectRegistry {

	public void notifyRemove( DomainObject obj, DomainObjectChangesHolder holder );
	public void notifyAdd( DomainObject obj, DomainObjectChangesHolder holder );
	public void notifyModify( DomainObjectFieldChanges changes, DomainObjectChangesHolder holder );
	
	public List<Domain> getDomains();
	
	public List<ServiceProviderDO> getServiceProviders();
	
	public List<HttpProxyDO> getHttpProxies();
	public HttpProxyDO getProxy( String id );
	
}
