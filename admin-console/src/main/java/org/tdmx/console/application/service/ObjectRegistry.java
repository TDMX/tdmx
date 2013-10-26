package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.domain.Domain;

public interface ObjectRegistry {

	public List<Domain> getDomains();
	
	public List<HttpProxyDO> getHttpProxies();
	public HttpProxyDO getProxy( String id );
	
}
