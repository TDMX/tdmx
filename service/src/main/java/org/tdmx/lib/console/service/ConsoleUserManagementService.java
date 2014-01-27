package org.tdmx.lib.console.service;

import org.tdmx.lib.console.domain.ConsoleUserStatus;
import org.tdmx.lib.console.domain.UserDetails;


public interface ConsoleUserManagementService {
	
	public void createUser( UserDetails user, String password );
	
	public UserDetails findUser( String loginName );
	
	public void modifyInfo( UserDetails user );

	public void modifyPassword( String loginName, String newPassword );

	public void modifyState( String loginName, ConsoleUserStatus newStatus );
	
}
