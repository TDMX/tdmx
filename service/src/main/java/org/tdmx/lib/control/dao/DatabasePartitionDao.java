package org.tdmx.lib.control.dao;

import java.util.List;

import org.tdmx.lib.control.domain.DatabasePartition;



public interface DatabasePartitionDao {
	
	public void persist( DatabasePartition value );
	
	public void delete( DatabasePartition value );
	
	public void lock( DatabasePartition value );
	
	public DatabasePartition merge( DatabasePartition value );

	public DatabasePartition loadById( String partitionId );

	public List<DatabasePartition> loadAll();
		
}
