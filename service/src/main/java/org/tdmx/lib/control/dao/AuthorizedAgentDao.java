package org.tdmx.lib.control.dao;

import org.tdmx.lib.control.domain.AuthorizedAgent;



public interface AuthorizedAgentDao {
	
	public void persist( AuthorizedAgent value );
	
	public void delete( AuthorizedAgent value );
	
	public void lock( AuthorizedAgent value );
	
	public AuthorizedAgent merge( AuthorizedAgent value );

	public AuthorizedAgent loadByFingerprint(String fingerprint);
		
}
