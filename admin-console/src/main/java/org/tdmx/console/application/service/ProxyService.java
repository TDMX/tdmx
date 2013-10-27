package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.HttpProxyDO;

public interface ProxyService {

	public static enum ERROR {
		HOSTNAME_INVALID,
		HOSTNAME_MISSING,
		PORT_INVALID,
		PORT_MISSING,
		TYPE_INVALID,
		TYPE_MISSING,
		USERNAME_OR_PASSWORD_MISSING,
	}
	
	public List<String> getProxyTypes();
	public boolean isDeleteWarning( HttpProxyDO proxy );
	public List<ERROR> create( HttpProxyDO proxy );
	public List<ERROR> modify( HttpProxyDO proxy, HttpProxyDO existing );
	public void delete( HttpProxyDO proxy );
}
