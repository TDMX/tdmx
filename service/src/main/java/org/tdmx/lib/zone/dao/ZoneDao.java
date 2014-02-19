package org.tdmx.lib.zone.dao;

import org.tdmx.lib.zone.domain.Zone;



public interface ZoneDao {
	
	public void persist( Zone value );
	
	public void delete( Zone value );
	
	public void lock( Zone value );
	
	public Zone merge( Zone value );

	public Zone loadById(String id);
		
}
