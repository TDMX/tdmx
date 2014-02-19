package org.tdmx.lib.zone.service;

import org.tdmx.lib.zone.domain.Zone;

public interface ZoneService {
	
	public void createOrUpdate( Zone zone );
	
	public Zone findByZoneApex( String zoneApex );
		
	public void delete( Zone zone );
	
}
