package org.tdmx.lib.control.service;

import java.util.List;

import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

/**
 * The DatabasePartitionService provides a cache of slowly changing DatabasePartition records.
 * 
 * Cache is a simple time based cache, refreshing all entities ( of which a small finite number
 * are expected ).
 * 
 * @author Peter
 *
 */
public interface DatabasePartitionService {
	
	public void createOrUpdate( DatabasePartition partition );
	
	// a partition can only be deleted if it is not yet activated.
	public void delete( DatabasePartition partition );
	
	public DatabasePartition findById( String partitionId );
	
	public List<DatabasePartition> findByTypeAndSegment( DatabaseType type, String segment );
	
	public List<DatabasePartition> findByType( DatabaseType type );
	
}
