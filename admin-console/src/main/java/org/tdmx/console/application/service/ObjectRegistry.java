package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.service.ObjectRegistryImpl.OBJECT_OPERATION;
import org.tdmx.console.domain.Domain;

public interface ObjectRegistry {

	public <E extends DomainObject> void notifyObject( OBJECT_OPERATION op, E obj );
	
	public List<Domain> getDomains();
	
	public List<ServiceProviderDO> getServiceProviders();
	
	public List<HttpProxyDO> getHttpProxies();
	public HttpProxyDO getProxy( String id );
	
}
