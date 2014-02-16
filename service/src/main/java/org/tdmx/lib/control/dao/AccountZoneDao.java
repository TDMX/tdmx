package org.tdmx.lib.control.dao;

import org.tdmx.lib.control.domain.AccountZone;



public interface AccountZoneDao {
	
	public void persist( AccountZone value );
	
	public void delete( AccountZone value );
	
	public void lock( AccountZone value );
	
	public AccountZone merge( AccountZone value );

	public AccountZone loadById(String id);
		
}
