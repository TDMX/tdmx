package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.HttpProxyDO;

public interface ProxyService {

	public static enum ERROR {
		HOSTNAME_INVALID, 
		PORT_INVALID
	}
	
	public boolean isDeleteable( HttpProxyDO proxy );
	public List<ERROR> create( HttpProxyDO proxy );
	public List<ERROR> modify( HttpProxyDO proxy );
	public void delete( HttpProxyDO proxy );
}
