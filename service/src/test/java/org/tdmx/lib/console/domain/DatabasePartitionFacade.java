package org.tdmx.lib.console.domain;

import java.util.Date;

import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

public class DatabasePartitionFacade {

	public static DatabasePartition createDatabasePartition( String id, DatabaseType dbType, String segment ) throws Exception  {
		DatabasePartition p = new DatabasePartition();
		
		p.setPartitionId(id);
		
		p.setDbType(dbType);
		p.setSegment(segment);

		p.setSizeFactor(100);
		p.setUrl("db.url-"+id);
		p.setUsername("username-"+id);
		p.setObfuscatedPassword("OBF"+id); //TODO
		
		p.setActivationTimestamp(new Date()); // currently active
		p.setDeactivationTimestamp(null);
		return p;
	}

	
}
