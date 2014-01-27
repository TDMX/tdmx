package org.tdmx.lib.console.service;

import org.tdmx.lib.console.domain.UserDetails;


public interface ConsoleUserAuthenticationService {
	
	public UserDetails login( String loginName, String password );
	
}
