package org.tdmx.lib.control.service;

import org.tdmx.lib.control.domain.AccountZone;

public interface AccountZoneService {
	
	public void createOrUpdate( AccountZone accountZone );
	
	public AccountZone findByZoneApex( String zoneApex );
		
	public void delete( AccountZone accountZone );
	
}
